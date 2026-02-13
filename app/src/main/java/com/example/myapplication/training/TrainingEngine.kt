package com.example.myapplication.training

import com.example.myapplication.data.Move
import com.example.myapplication.data.TrainingPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TrainingEngine(
    private val scope: CoroutineScope,
    private val preset: TrainingPreset,
    private val availableMoves: List<Move>,
    private val onPlayMove: suspend (Move) -> Unit,
    private val onPlayEndBeep: () -> Unit,
    private val onPlayCountdownBeep: () -> Unit,
    private val onPlayMetronomeTick: () -> Unit,
    private val onStopMetronome: () -> Unit,
) {
    private val _state = MutableStateFlow<TrainingState>(TrainingState.Idle)
    val state: StateFlow<TrainingState> = _state.asStateFlow()

    private var engineJob: Job? = null
    private var metronomeJob: Job? = null
    private var lastCalledMoveId: String? = null

    fun start() {
        engineJob?.cancel()
        val selectedMoves = availableMoves.filter { it.id in preset.selectedMoveIds }
        if (selectedMoves.isEmpty()) return

        engineJob = scope.launch {
            countdown()
            if (!scope.isActive) return@launch

            for (round in 1..preset.rounds) {
                for (unit in 1..preset.unitsPerRound) {
                    val move = pickMove(selectedMoves)
                    val windowMs = getWindowMs()
                    val globalUnit = (round - 1) * preset.unitsPerRound + unit

                    _state.value = TrainingState.Calling(move.displayName, globalUnit, round, preset.totalUnits, preset.rounds)
                    onPlayMove(move)

                    if (!scope.isActive) return@launch
                    onStopMetronome()
                    metronomeJob?.cancel()
                    metronomeJob = if (preset.metronomeEnabled) {
                        scope.launch {
                            delay(preset.metronomeBeatIntervalMs)
                            while (scope.isActive) {
                                onPlayMetronomeTick()
                                delay(preset.metronomeBeatIntervalMs)
                            }
                        }
                    } else null

                    val startTime = System.currentTimeMillis()
                    while (scope.isActive) {
                        val elapsed = System.currentTimeMillis() - startTime
                        _state.value = TrainingState.WaitingWindow(move.displayName, globalUnit, round, preset.totalUnits, preset.rounds, elapsed, windowMs)

                        if (elapsed >= windowMs) {
                            metronomeJob?.cancel()
                            metronomeJob = null
                            onStopMetronome()
                            if (preset.endBeepEnabled) onPlayEndBeep()
                            lastCalledMoveId = move.id
                            break
                        }
                        delay(50)
                    }
                    delay(300) // gap before next
                    if (!scope.isActive) return@launch
                }

                if (round < preset.rounds && preset.pauseBetweenRoundsMs > 0) {
                    for (cd in (preset.pauseBetweenRoundsMs / 1000).toInt() downTo 1) {
                        if (!scope.isActive) return@launch
                        _state.value = TrainingState.RoundPause(round, preset.rounds, cd)
                        if (cd in 1..3) onPlayCountdownBeep()
                        delay(1000)
                    }
                }
            }

            _state.value = TrainingState.Finished
        }
    }

    private suspend fun countdown() {
        for (c in 3 downTo 1) {
            _state.value = TrainingState.StartCountdown(c)
            onPlayCountdownBeep()
            delay(1000)
        }
    }

    private fun pickMove(moves: List<Move>): Move {
        val filtered = if (preset.noImmediateRepetition && lastCalledMoveId != null && moves.size > 1) {
            moves.filter { it.id != lastCalledMoveId }
        } else {
            moves
        }
        return filtered.random()
    }

    private fun getWindowMs(): Long =
        when {
            preset.reactionIntervalMinMs != null && preset.reactionIntervalMaxMs != null -> {
                (preset.reactionIntervalMinMs..preset.reactionIntervalMaxMs).random().toLong()
            }
            else -> preset.reactionIntervalMs
        }

    fun pause() {
        val s = _state.value
        val current = when (s) {
            is TrainingState.Calling -> TrainingState.CallingState.InCall(s.move, s.unit, s.round, s.totalUnits, s.totalRounds)
            is TrainingState.WaitingWindow -> TrainingState.CallingState.InWindow(s.move, s.unit, s.round, s.totalUnits, s.totalRounds, s.elapsedMs, s.windowMs)
            is TrainingState.RoundPause -> TrainingState.CallingState.InRoundPause(s.round, s.totalRounds)
            is TrainingState.StartCountdown -> TrainingState.CallingState.Countdown
            else -> return
        }
        engineJob?.cancel()
        metronomeJob?.cancel()
        metronomeJob = null
        onStopMetronome()
        _state.value = TrainingState.Paused(current)
    }

    fun resume() {
        val s = _state.value as? TrainingState.Paused ?: return
        // Simplified: resume restarts from current position; full implementation would track exact state
        start()
    }

    fun stop() {
        engineJob?.cancel()
        metronomeJob?.cancel()
        metronomeJob = null
        onStopMetronome()
        _state.value = TrainingState.Idle
    }
}
