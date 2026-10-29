package com.example.myapplication



import androidx.compose.runtime.*

import androidx.compose.foundation.layout.*

import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalDensity

import androidx.compose.ui.unit.Density

import androidx.compose.ui.unit.dp

import androidx.test.platform.app.InstrumentationRegistry

import org.junit.Assert.*

import android.graphics.Bitmap

import java.io.File

import androidx.compose.ui.test.*

import androidx.compose.ui.test.junit4.createComposeRule

import com.example.myapplication.data.*

import com.example.myapplication.ui.*

import com.example.myapplication.ui.theme.MyApplicationTheme

import org.junit.Rule

import org.junit.Test



class AuditRegressionTest {

    @get:Rule val compose = createComposeRule()



    @Test fun largeExerciseLibraryComposition() {

        val moves = (1..300).map { Move("custom_audit_$it", "Audit exercise $it", MoveCategory.HAU) }

        val start = android.os.SystemClock.elapsedRealtime()

        compose.setContent { CompositionLocalProvider(LocalStrings provides StringsDe) {

            MyApplicationTheme(darkTheme = true) {

                MoveAudioListScreen(moves, emptyMap(), { _, _ -> }, {}, {}, {})

            }

        } }

        compose.waitForIdle()

        val count = compose.onAllNodesWithText("Audit exercise", substring = true).fetchSemanticsNodes().size

        assertTrue("Only visible rows should be composed: $count", count in 1..20)

        println("AUDIT_LIBRARY composedNames=$count initialIdleMs=${android.os.SystemClock.elapsedRealtime()-start}")

        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(300)

        compose.onNodeWithText("Audit exercise 300").assertIsDisplayed()

    }



    private fun capture(name: String) {

        compose.waitForIdle()

        compose.mainClock.advanceTimeBy(1000)

        compose.waitForIdle()

        val instrumentation = InstrumentationRegistry.getInstrumentation()

        instrumentation.waitForIdleSync()

        android.os.SystemClock.sleep(800)

        val image = instrumentation.uiAutomation.takeScreenshot()

        File(instrumentation.targetContext.getExternalFilesDir(null), "$name.png").outputStream().use {

            image.compress(Bitmap.CompressFormat.PNG, 100, it)

        }

        image.recycle()

    }



    @Test fun savingBlocksBackRetriesSameDraftAndIgnoresLateCallbacks() {

        var show by mutableStateOf(true)

        var success: () -> Unit = {}

        var failure: () -> Unit = {}

        var navigations = 0

        var saves = 0

        val titles = mutableListOf<String>()

        val initial = TrainingPreset(rounds = 3)

        compose.setContent { MyApplicationTheme(darkTheme = true) {

            CompositionLocalProvider(LocalStrings provides StringsDe) {

                if (show) TrainingEditor(initial, null, emptyList(), {}, { title, _, preset, ok, fail ->

                    assertEquals(initial.rounds, preset.rounds)

                    titles.add(title); saves++; success = ok; failure = fail

                }, { navigations++ }, { navigations++ })

            }

        } }

        compose.onNodeWithText(StringsDe.savePreset).performClick()

        compose.onNodeWithText(StringsDe.presetName).performTextReplacement("Mein Entwurf")

        compose.onAllNodesWithText(StringsDe.save).onLast().performClick()

        compose.onNodeWithText("Training wird gespeichert").assertIsDisplayed()

        androidx.test.espresso.Espresso.pressBack()

        compose.runOnIdle { assertEquals(0, navigations); assertEquals(1, saves); failure() }

        compose.onNodeWithText("Erneut versuchen").performClick()

        compose.runOnIdle { assertEquals(listOf("Mein Entwurf", "Mein Entwurf"), titles); success() }

        compose.runOnIdle { assertEquals(1, navigations); success(); assertEquals(1, navigations); show = false }

        compose.waitForIdle()

        compose.runOnIdle { success(); failure(); assertEquals(1, navigations) }

    }



    @Test fun detachedEditorIgnoresPendingSave() {

        var show by mutableStateOf(true)

        var success: () -> Unit = {}

        var navigations = 0

        compose.setContent { MyApplicationTheme(darkTheme = true) {

            CompositionLocalProvider(LocalStrings provides StringsDe) {

                if (show) TrainingEditor(TrainingPreset(), null, emptyList(), {}, { _, _, _, ok, _ -> success = ok }, { navigations++ }, {})

            }

        } }

        compose.onNodeWithText(StringsDe.savePreset).performClick()

        compose.onNodeWithText(StringsDe.presetName).performTextReplacement("Später")

        compose.onAllNodesWithText(StringsDe.save).onLast().performClick()

        compose.runOnIdle { show = false }

        compose.waitForIdle()

        compose.runOnIdle { success(); assertEquals(0, navigations) }

    }



    @Test fun narrowLargeTextLibraryAndCategoryDialog() {

        compose.setContent { MyApplicationTheme(darkTheme = true) {

            CompositionLocalProvider(LocalStrings provides StringsDe, LocalDensity provides Density(LocalDensity.current.density, 2f)) {

                Box(Modifier.width(320.dp).fillMaxHeight()) {

                    MoveAudioListScreen(listOf(Move("test", "Oberhau", MoveCategory.HAU)), emptyMap(), { _, _ -> }, {}, {}, {})

                }

            }

        } }

        compose.onNodeWithText(StringsDe.stiche).assertIsDisplayed().performClick()

        compose.onNodeWithText("Keine passenden Übungen.").performScrollTo().assertIsDisplayed()

        capture("audit-library-large")

        compose.onNodeWithContentDescription(StringsDe.addMove).performClick()

        compose.onNodeWithText(StringsDe.stich).assertIsDisplayed().performClick()

        capture("audit-add-large")

    }



    @Test fun narrowLargeTextStepperHasNamedActions() {

        var value by mutableStateOf(3)

        compose.setContent { MyApplicationTheme(darkTheme = true) {

            CompositionLocalProvider(LocalStrings provides StringsDe, LocalDensity provides Density(LocalDensity.current.density, 2f)) {

                Box(Modifier.width(280.dp)) { StepperCard("Trainingsrunden", value, { value = it }) }

            }

        } }

        compose.onNodeWithContentDescription("Trainingsrunden erhöhen").assertIsDisplayed().performClick()

        compose.onNodeWithContentDescription("Trainingsrunden").assertTextContains("4")

        compose.onNodeWithContentDescription("Trainingsrunden verringern").assertIsDisplayed().performClick()

        compose.onNodeWithContentDescription("Trainingsrunden").assertTextContains("3")

        capture("audit-stepper-large")

    }

    @Test fun homeLargeTextKeepsBothLibrariesReachable() {
        compose.setContent { MyApplicationTheme(darkTheme = true) {
            CompositionLocalProvider(LocalStrings provides StringsDe, LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                Box(Modifier.width(320.dp).fillMaxHeight()) { HomeScreen(TrainingPreset(), emptyList(), {}, {}, {}, {}, {}, {}) }
            }
        } }
        compose.onNodeWithText(StringsDe.trainingsShort).performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText(StringsDe.movesShort).onLast().performScrollTo().assertIsDisplayed()
        capture("audit-home-large")
    }

    @Test fun settingsSlidersHaveContextAndValue() {
        compose.setContent { MyApplicationTheme(darkTheme = true) {
            CompositionLocalProvider(LocalStrings provides StringsDe) {
                SettingsScreen(TrainingPreset(), 1.4f, {}, null, {}, "de", {}, {}, {})
            }
        } }
        compose.onNodeWithContentDescription(StringsDe.tickVolume).performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription(StringsDe.ttsSpeed).performScrollTo().assertIsDisplayed()
        capture("audit-settings")
    }

    @Test fun configurationLargeTextAndShortWindowRemainScrollable() {
        var short by mutableStateOf(false)
        compose.setContent { MyApplicationTheme(darkTheme = true) {
            CompositionLocalProvider(LocalStrings provides StringsDe, LocalDensity provides Density(LocalDensity.current.density, if (short) 1f else 2f)) {
                Box(Modifier.width(320.dp).height(if (short) 360.dp else 800.dp)) {
                    TrainingEditor(TrainingPreset(), null, emptyList(), {}, { _, _, _, _, _ -> }, {}, {})
                }
            }
        } }
        compose.onNodeWithContentDescription(StringsDe.reactionInterval).performScrollTo().assertIsDisplayed()
        capture("audit-config-large")
        compose.runOnIdle { short = true }
        compose.onNodeWithContentDescription(StringsDe.reactionInterval).performScrollTo().assertIsDisplayed()
        capture("audit-config-short")
    }

}
