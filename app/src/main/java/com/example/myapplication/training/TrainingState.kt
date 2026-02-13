package com.example.myapplication.training

import com.example.myapplication.data.TrainingPreset

sealed class TrainingState {
    data object Idle : TrainingState()
    data class StartCountdown(val count: Int) : TrainingState()
    data class Calling(val move: String, val unit: Int, val round: Int, val totalUnits: Int, val totalRounds: Int) : TrainingState()
    data class WaitingWindow(val move: String, val unit: Int, val round: Int, val totalUnits: Int, val totalRounds: Int, val elapsedMs: Long, val windowMs: Long) : TrainingState()
    data class RoundPause(val round: Int, val totalRounds: Int, val countdown: Int) : TrainingState()
    data class Paused(val current: CallingState) : TrainingState()
    data object Finished : TrainingState()

    sealed class CallingState {
        data object Countdown : CallingState()
        data class InCall(val move: String, val unit: Int, val round: Int, val totalUnits: Int, val totalRounds: Int) : CallingState()
        data class InWindow(val move: String, val unit: Int, val round: Int, val totalUnits: Int, val totalRounds: Int, val elapsedMs: Long, val windowMs: Long) : CallingState()
        data class InRoundPause(val round: Int, val totalRounds: Int) : CallingState()
    }
}
