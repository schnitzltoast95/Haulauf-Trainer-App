package com.example.myapplication

import com.example.myapplication.data.*
import org.junit.Assert.*
import org.junit.Test

class BadgeProgressTest {
    private fun move(id: String, count: Int) = MoveStat(id, id, MoveCategory.HAU, count, false)

    @Test fun earnedLevelsSortBeforeFractionAndPreviewIsLimitedToThree() {
        val snapshot = StatisticsSnapshot(moves=listOf(move("nearly",249), move("earned",250),
            move("higher",1000),move("farther",900),move("done",12000)))
        assertEquals(listOf("done","higher","farther"), snapshot.rankedBadges().take(3).map { it.move.id })
        assertEquals(listOf("done","higher","farther","earned","nearly"), snapshot.rankedBadges().map { it.move.id })
    }

    @Test fun variantsUseBadgeCountAndUnlockedTitleNeverRegressesAtBoundary() {
        val badge = StatisticsSnapshot(moves=listOf(move("oberhau",4)),badgeCounts=mapOf("oberhau" to 1000))
            .rankedBadges().single()
        assertEquals(1000,badge.count)
        assertEquals(2,badge.earnedLevels)
        assertEquals(1,badge.titleIndex)
        assertEquals(3000,badge.target)
        assertEquals(1000f/3000,badge.progress,0.001f)
    }

    @Test fun everyThresholdUnlocksExactlyOneLevel() {
        BADGE_THRESHOLDS.forEachIndexed { index, threshold ->
            assertEquals(index, BadgeProgress(move("test", threshold - 1), threshold - 1).earnedLevels)
            assertEquals(index + 1, BadgeProgress(move("test", threshold), threshold).earnedLevels)
        }
    }

    @Test fun titlesAreDiscoveredOnlyWhenTheirLevelHasStarted() {
        val titles = badgeTitles("oberhau", "Oberhau", true)
        assertEquals(List(4) { "???" }, BadgeProgress(move("oberhau",0),0).visibleTitles(true))
        assertEquals(listOf(titles[0], "???", "???", "???"),
            BadgeProgress(move("oberhau",1),1).visibleTitles(true))
        BADGE_THRESHOLDS.dropLast(1).forEachIndexed { index, threshold ->
            val atBoundary = BadgeProgress(move("oberhau",threshold),threshold).visibleTitles(true)
            assertEquals(titles.take(index + 1), atBoundary.take(index + 1))
            assertEquals("???", atBoundary[index + 1])
            assertEquals(titles[index + 1],
                BadgeProgress(move("oberhau",threshold + 1),threshold + 1).visibleTitles(true)[index + 1])
        }
        assertEquals(titles, BadgeProgress(move("oberhau",10000),10000).visibleTitles(true))
    }

    @Test fun zeroAndFinalLevelHaveValidProgressAndTitle() {
        val zero = BadgeProgress(move("zero",0),0)
        assertEquals(0,zero.earnedLevels)
        assertEquals(250,zero.target)
        assertEquals(0,zero.titleIndex)
        val complete = BadgeProgress(move("max",14000),14000)
        assertTrue(complete.complete)
        assertEquals(3,complete.titleIndex)
        assertEquals(1f,complete.progress,0f)
    }
}
