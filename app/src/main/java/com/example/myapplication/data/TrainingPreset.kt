package com.example.myapplication.data

data class TrainingPreset(
    val rounds: Int = 5,
    val unitsPerRound: Int = 25,
    val reactionIntervalMs: Long = 1200,
    val reactionIntervalMinMs: Long? = null,
    val reactionIntervalMaxMs: Long? = null,
    val pauseBetweenRoundsMs: Long = 10000,
    val selectedMoveIds: Set<String> = DEFAULT_MOVES.map { it.id }.toSet(),
    val endBeepEnabled: Boolean = true,
    val metronomeEnabled: Boolean = false,
    val metronomeBeatIntervalMs: Long = 500,
    val noImmediateRepetition: Boolean = false,
    val initialCountdownMs: Long = 5000,
    val movePriorities: Map<String, Int> = emptyMap(),
    val volumeCall: Float = 1f,
    val volumeBeep: Float = 0.75f,
    val volumeTick: Float = 0.25f,
) {
    val totalUnits: Int get() = rounds * unitsPerRound

    fun estimatedDurationSeconds(): Long {
        val avgIntervalMs = when {
            reactionIntervalMinMs != null && reactionIntervalMaxMs != null ->
                (reactionIntervalMinMs + reactionIntervalMaxMs) / 2
            else -> reactionIntervalMs
        }
        val callTime = 1500L // approx time for call playback
        val totalCallTime = totalUnits * (avgIntervalMs + callTime)
        val roundPauses = (rounds - 1).coerceAtLeast(0) * pauseBetweenRoundsMs
        return (totalCallTime + roundPauses + initialCountdownMs) / 1000
    }
}
