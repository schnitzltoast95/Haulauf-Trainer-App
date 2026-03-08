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
import kotlin.random.Random

class TrainingEngine(
    private val scope: CoroutineScope,
    private val preset: TrainingPreset,
    private val availableMoves: List<Move>,
    private val onPlayMove: suspend (Move) -> Unit,
    private val onPlayEndBeep: () -> Unit,
    private val onPlayCountdownBeep: (Int) -> Unit,
    private val onPlayRoundFinishedSignal: () -> Unit,
    private val onPlayTrainingFinishedSignal: () -> Unit,
    private val onPlayMetronomeTick: () -> Unit,
    private val onStopMetronome: () -> Unit,
) {
    companion object {
        private const val COUNTDOWN_BEEP_WINDOW_SEC = 5
        private const val AFTER_ROUND_SIGNAL_GAP_MS = 800L
    }

    private val _state = MutableStateFlow<TrainingState>(TrainingState.Idle)
    val state: StateFlow<TrainingState> = _state.asStateFlow()

    private var engineJob: Job? = null
    private var lastCalledMoveId: String? = null
    private var currentMoveId: String? = null
    @Volatile
    private var isPaused: Boolean = false
    @Volatile
    private var restartCurrentUnitAfterResume: Boolean = false
    @Volatile
    private var replayMoveIdAfterResume: String? = null
    @Volatile
    private var replayPendingAfterResume: Boolean = false

    fun start() {
        engineJob?.cancel()
        isPaused = false
        restartCurrentUnitAfterResume = false
        replayMoveIdAfterResume = null
        replayPendingAfterResume = false
        val selectedMoves = availableMoves.filter { it.id in preset.selectedMoveIds }
        if (selectedMoves.isEmpty()) return

        engineJob = scope.launch {
            countdown()
            if (!scope.isActive) return@launch

            for (round in 1..preset.rounds) {
                for (unit in 1..preset.unitsPerRound) {
                    var restartUnit = false
                    do {
                        restartUnit = false
                        val move = nextMove(selectedMoves)
                        currentMoveId = move.id
                        val windowMs = getWindowMs()
                        val globalUnit = (round - 1) * preset.unitsPerRound + unit

                        _state.value = TrainingState.Calling(move.displayName, globalUnit, round, preset.totalUnits, preset.rounds)
                        onPlayMove(move)

                        if (!scope.isActive) return@launch
                        onStopMetronome()
                        val metronomeBeatCount = preset.metronomeBeatIntervalMs.coerceIn(1L, 32L).toInt()
                        val metronomeTickIntervalMs = if (preset.metronomeEnabled) {
                            // Place beats strictly between move call and end tone:
                            // 1 beat -> at 1/2, 2 beats -> at 1/3 and 2/3, etc.
                            (windowMs / (metronomeBeatCount + 1).toDouble()).toLong().coerceAtLeast(1L)
                        } else {
                            Long.MAX_VALUE
                        }
                        var nextTickAtMs = metronomeTickIntervalMs
                        var elapsed = 0L
                        while (scope.isActive) {
                            if (waitIfPaused()) {
                                onStopMetronome()
                                if (restartCurrentUnitAfterResume) {
                                    restartCurrentUnitAfterResume = false
                                    restartUnit = true
                                    break
                                }
                                continue
                            }
                            _state.value = TrainingState.WaitingWindow(move.displayName, globalUnit, round, preset.totalUnits, preset.rounds, elapsed, windowMs)

                            if (preset.metronomeEnabled && elapsed < windowMs) {
                                while (elapsed >= nextTickAtMs && nextTickAtMs < windowMs) {
                                    onPlayMetronomeTick()
                                    nextTickAtMs += metronomeTickIntervalMs
                                }
                            }

                            if (elapsed >= windowMs) {
                                onStopMetronome()
                                if (preset.endBeepEnabled) onPlayEndBeep()
                                lastCalledMoveId = move.id
                                break
                            }
                            val stepMs = minOf(50L, windowMs - elapsed)
                            pauseAwareDelay(stepMs)
                            elapsed += stepMs
                        }
                        if (restartUnit) continue

                        pauseAwareDelay(300) // gap before next
                    } while (restartUnit && scope.isActive)
                    currentMoveId = null
                    if (!scope.isActive) return@launch
                }

                if (round < preset.rounds) {
                    onPlayRoundFinishedSignal()
                    pauseAwareDelay(AFTER_ROUND_SIGNAL_GAP_MS)
                }

                if (round < preset.rounds && preset.pauseBetweenRoundsMs > 0) {
                    for (cd in (preset.pauseBetweenRoundsMs / 1000).toInt() downTo 1) {
                        waitIfPaused()
                        if (!scope.isActive) return@launch
                        _state.value = TrainingState.RoundPause(round, preset.rounds, cd)
                        if (cd in 1..COUNTDOWN_BEEP_WINDOW_SEC) onPlayCountdownBeep(cd)
                        pauseAwareDelay(1000)
                    }
                }
            }

            if (!scope.isActive) return@launch
            onPlayTrainingFinishedSignal()
            _state.value = TrainingState.Finished
        }
    }

    private suspend fun countdown() {
        val startCountdownSec = (preset.initialCountdownMs / 1000).toInt().coerceIn(1, 30)
        for (c in startCountdownSec downTo 1) {
            waitIfPaused()
            if (!scope.isActive) return
            _state.value = TrainingState.StartCountdown(c)
            if (c in 1..COUNTDOWN_BEEP_WINDOW_SEC) onPlayCountdownBeep(c)
            pauseAwareDelay(1000)
        }
    }

    private fun pickMove(moves: List<Move>): Move {
        val filtered = if (preset.noImmediateRepetition && lastCalledMoveId != null && moves.size > 1) {
            moves.filter { it.id != lastCalledMoveId }
        } else {
            moves
        }
        val weightedMoves = filtered.map { move ->
            move to (preset.movePriorities[move.id] ?: 1).coerceIn(1, 3)
        }
        val totalWeight = weightedMoves.sumOf { it.second }
        if (totalWeight <= 0) return filtered.random()

        var roll = Random.nextInt(totalWeight)
        for ((move, weight) in weightedMoves) {
            if (roll < weight) return move
            roll -= weight
        }
        return filtered.last()
    }

    private fun getWindowMs(): Long =
        when {
            preset.reactionIntervalMinMs != null && preset.reactionIntervalMaxMs != null -> {
                (preset.reactionIntervalMinMs..preset.reactionIntervalMaxMs).random().toLong()
            }
            else -> preset.reactionIntervalMs
        }

    fun pause() {
        if (engineJob?.isActive != true || isPaused) return
        val s = _state.value
        val current = when (s) {
            is TrainingState.Calling -> TrainingState.CallingState.InCall(s.move, s.unit, s.round, s.totalUnits, s.totalRounds)
            is TrainingState.WaitingWindow -> TrainingState.CallingState.InWindow(s.move, s.unit, s.round, s.totalUnits, s.totalRounds, s.elapsedMs, s.windowMs)
            is TrainingState.RoundPause -> TrainingState.CallingState.InRoundPause(s.round, s.totalRounds)
            is TrainingState.StartCountdown -> TrainingState.CallingState.Countdown
            else -> return
        }
        val moveToReplay = currentMoveId ?: lastCalledMoveId
        replayMoveIdAfterResume = moveToReplay
        replayPendingAfterResume = moveToReplay != null
        restartCurrentUnitAfterResume = s is TrainingState.Calling || s is TrainingState.WaitingWindow
        isPaused = true
        onStopMetronome()
        _state.value = TrainingState.Paused(current)
    }

    fun resume() {
        _state.value as? TrainingState.Paused ?: return
        if (!isPaused) return
        isPaused = false
    }

    fun stop() {
        engineJob?.cancel()
        isPaused = false
        restartCurrentUnitAfterResume = false
        replayMoveIdAfterResume = null
        replayPendingAfterResume = false
        currentMoveId = null
        onStopMetronome()
        _state.value = TrainingState.Idle
    }

    private suspend fun waitIfPaused(): Boolean {
        var wasPaused = false
        while (scope.isActive && isPaused) {
            wasPaused = true
            delay(50)
        }
        if (wasPaused && scope.isActive) {
            countdown()
        }
        return wasPaused
    }

    private suspend fun pauseAwareDelay(totalMs: Long) {
        var remainingMs = totalMs
        while (scope.isActive && remainingMs > 0) {
            waitIfPaused()
            if (!scope.isActive) return
            val stepMs = minOf(remainingMs, 50L)
            delay(stepMs)
            remainingMs -= stepMs
        }
    }

    private fun nextMove(moves: List<Move>): Move {
        if (replayPendingAfterResume) {
            replayPendingAfterResume = false
            val replayId = replayMoveIdAfterResume
            replayMoveIdAfterResume = null
            if (replayId != null) {
                moves.firstOrNull { it.id == replayId }?.let { return it }
            }
        }
        return pickMove(moves)
    }
}
