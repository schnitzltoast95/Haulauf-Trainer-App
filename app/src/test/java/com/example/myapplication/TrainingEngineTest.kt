package com.example.myapplication

import com.example.myapplication.data.DEFAULT_MOVES
import com.example.myapplication.data.Move
import com.example.myapplication.data.TrainingPreset
import com.example.myapplication.training.TrainingEngine
import com.example.myapplication.training.TrainingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingEngineTest {
    @Test
    fun completesRequestedCallsWithoutImmediateRepetition() = runBlocking {
        val calls = mutableListOf<String>()
        var finishSignals = 0
        val preset = TrainingPreset(
            rounds = 1,
            unitsPerRound = 6,
            reactionIntervalMs = 1,
            initialCountdownMs = 1000,
            selectedMoveIds = setOf("oberhau", "unterhau"),
            noImmediateRepetition = true,
            endBeepEnabled = false
        )
        val engine = createEngine(
            scope = this,
            preset = preset,
            onMove = { calls += it.id },
            onFinish = { finishSignals++ }
        )

        engine.start()
        withTimeout(7000) { engine.state.first { it is TrainingState.Finished } }

        assertEquals(6, calls.size)
        assertTrue(calls.zipWithNext().all { (previous, next) -> previous != next })
        assertEquals(1, finishSignals)
    }

    @Test
    fun pauseHoldsCountdownUntilResumed() = runBlocking {
        val preset = TrainingPreset(
            rounds = 1,
            unitsPerRound = 1,
            reactionIntervalMs = 1,
            initialCountdownMs = 1000,
            selectedMoveIds = setOf("oberhau"),
            endBeepEnabled = false
        )
        val engine = createEngine(this, preset)

        engine.start()
        withTimeout(2000) { engine.state.first { it is TrainingState.StartCountdown } }
        engine.pause()
        assertTrue(engine.state.value is TrainingState.Paused)
        delay(1200)
        assertTrue(engine.state.value is TrainingState.Paused)

        engine.resume()
        withTimeout(5000) { engine.state.first { it is TrainingState.Finished } }
        Unit
    }

    @Test
    fun cancellingCountdownNeverStartsCallsOrFinishSignal() = runBlocking {
        var calls = 0
        var finishes = 0
        val engine = createEngine(this,
            TrainingPreset(initialCountdownMs = 1000, selectedMoveIds = setOf("oberhau")),
            onMove = { calls++ }, onFinish = { finishes++ })
        engine.start()
        withTimeout(2000) { engine.state.first { it is TrainingState.StartCountdown } }
        engine.stop()
        delay(1200)
        assertEquals(TrainingState.Idle, engine.state.value)
        assertEquals(0, calls)
        assertEquals(0, finishes)
    }

    private fun createEngine(
        scope: CoroutineScope,
        preset: TrainingPreset,
        onMove: (Move) -> Unit = {},
        onFinish: () -> Unit = {}
    ) = TrainingEngine(
        scope = scope,
        preset = preset,
        availableMoves = DEFAULT_MOVES,
        onPlayMove = onMove,
        onPlayEndBeep = {},
        onPlayCountdownBeep = {},
        onPlayRoundFinishedSignal = {},
        onPlayTrainingFinishedSignal = onFinish,
        onPlayMetronomeTick = {},
        onStopMetronome = {}
    )
}
