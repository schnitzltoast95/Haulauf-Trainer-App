package com.example.myapplication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.myapplication.data.*
import com.example.myapplication.ui.StatisticsScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test

class StatisticsScreenDeviceTest {
    @get:Rule val compose = createComposeRule()
    private fun scrollTo(text: String) { compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(text)) }
    private val rows = listOf(
        MoveStat("oberhau","Oberhau",MoveCategory.HAU,10000,false),
        MoveStat("krumphau","Krumphau",MoveCategory.HAU,1000,false),
        MoveStat("ochs","Ochs",MoveCategory.HUT,24,false),
        MoveStat("removed","Custom",MoveCategory.HAU,2,true))

    @Test fun threeHighlightsOpenDetailsAndFullCollection() {
        compose.setContent { MyApplicationTheme(darkTheme=true) {
            StatisticsScreen(StatisticsSnapshot(totalCalls=11026,totalCuts=11002,finishedTrainings=3,
                moves=rows,challenge=WeeklyChallenge(0,1000,30)),true,{})
        } }
        compose.onNodeWithText("Dein Training auf einen Blick").assertIsDisplayed()
        scrollTo("Zwergenschlächter"); compose.onNodeWithText("Zwergenschlächter").performClick()
        compose.onNodeWithText("10000 Aufrufe · Erreicht").assertExists()
        compose.onNodeWithContentDescription("Schließen").performClick()
        scrollTo("Hornträger"); compose.onNodeWithText("Hornträger").assertIsDisplayed()
        compose.onNodeWithText("Custom (gelöscht)").assertDoesNotExist()
        compose.onNodeWithText("Alle Abzeichen").assertDoesNotExist(); compose.onNodeWithContentDescription("Alle Abzeichen ansehen").performClick()
        compose.onNodeWithText("6 von 16 Titeln erreicht").assertIsDisplayed()
        scrollTo("Custom (gelöscht)"); compose.onNodeWithText("Custom (gelöscht)").assertIsDisplayed()
        compose.onNodeWithContentDescription("Zurück").performClick()
        compose.onNodeWithText("Dein Training auf einen Blick").assertIsDisplayed()
    }

    @Test fun startedBadgeRevealsOnlyItsCurrentTitleInDetails() {
        compose.setContent { MyApplicationTheme(darkTheme=true) {
            StatisticsScreen(StatisticsSnapshot(moves=listOf(MoveStat("schielhau","Schielhau",MoveCategory.HAU,1,false))),true,{})
        } }
        scrollTo("Schneidenwender"); compose.onNodeWithText("Schneidenwender").performClick()
        compose.onAllNodesWithText("Schneidenwender").onLast().assertIsDisplayed()
        compose.onNodeWithText("Gegenhaukenner").assertDoesNotExist()
        compose.onNodeWithText("Meister der kurzen Schneide").assertDoesNotExist()
        compose.onNodeWithText("Kurz und schneidig").assertDoesNotExist()
        compose.onNodeWithContentDescription("Schließen").performClick()
    }

    @Test fun emptyProgressAndEnglishLabelsShowLockedLevels() {
        compose.setContent { MyApplicationTheme(darkTheme=true) {
            StatisticsScreen(StatisticsSnapshot(moves=listOf(MoveStat("oberhau","Oberhau",MoveCategory.HAU,0,false))),false,{})
        } }
        compose.onNodeWithText("Weekly challenge").assertIsDisplayed()
        scrollTo("???")
        compose.onNodeWithText("Oberhau · Apprentice").assertDoesNotExist()
        compose.onNodeWithText("???").performClick()
        compose.onNodeWithText("250 calls · Locked").assertExists()
        compose.onNodeWithText("Oberhau · Apprentice").assertDoesNotExist()
        compose.onNodeWithText("Oberhau · Adept").assertDoesNotExist()
        compose.onNodeWithContentDescription("Close").performClick()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0); compose.onNodeWithContentDescription("View all badges").performClick()
        compose.onNodeWithText("0 of 4 titles earned").assertIsDisplayed()
        scrollTo("???")
        compose.onNodeWithText("Oberhau · Apprentice").assertDoesNotExist()
    }
}
