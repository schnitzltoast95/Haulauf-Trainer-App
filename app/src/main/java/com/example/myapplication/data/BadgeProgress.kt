package com.example.myapplication.data

data class BadgeProgress(val move: MoveStat, val count: Int) {
    val earnedLevels: Int get() = BADGE_THRESHOLDS.count { count >= it }
    val target: Int get() = BADGE_THRESHOLDS.getOrElse(earnedLevels) { BADGE_THRESHOLDS.last() }
    val progress: Float get() = (count.toFloat() / target).coerceIn(0f, 1f)
    val titleIndex: Int get() = (earnedLevels - 1).coerceAtLeast(0)
    fun visibleTitles(german: Boolean): List<String> =
        badgeTitles(move.id, move.name, german).mapIndexed { index, title ->
            val start = if (index == 0) 0 else BADGE_THRESHOLDS[index - 1]
            if (count > start) title else "???"
        }

    val complete: Boolean get() = earnedLevels == BADGE_THRESHOLDS.size
}

fun StatisticsSnapshot.rankedBadges(): List<BadgeProgress> = moves
    .map { BadgeProgress(it, (badgeCounts[it.id] ?: it.count).coerceAtLeast(0)) }
    .sortedWith(compareByDescending<BadgeProgress> { it.earnedLevels }
        .thenByDescending { it.progress }
        .thenBy { it.move.name }
        .thenBy { it.move.id })
