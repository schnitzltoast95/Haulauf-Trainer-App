package com.example.myapplication

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.*
import com.example.myapplication.ui.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.UUID

class LibraryPersistenceTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun deletionCleansEveryReferenceAndPreservesEmptyTraining() = runBlocking {
        val repo = PreferencesRepository(context)
        val move = repo.addCustomMove("Löschtest", MoveCategory.HAU)
        val id = UUID.randomUUID().toString()
        val old = repo.getLastUsedPreset()
        try {
            val value = TrainingPreset(selectedMoveIds = setOf(move.id), movePriorities = mapOf(move.id to 3))
            repo.savePreset(SavedPreset(id, "Deletion test", value))
            repo.saveLastUsedPreset(value)
            repo.saveExercise(move.copy(description = "Own note | punctuation", imagePath = "/test/image"), "file:///test/audio")
            assertEquals("Own note | punctuation", repo.getMoveInfo()[move.id]?.first)
            repo.removeCustomMove(move.id)
            val saved = repo.getSavedPresets().first { it.id == id }
            assertTrue(saved.preset.selectedMoveIds.isEmpty())
            assertTrue(saved.preset.movePriorities.isEmpty())
            assertTrue(repo.getLastUsedPreset()!!.selectedMoveIds.isEmpty())
            assertFalse(repo.getMoveInfo().containsKey(move.id))
            assertFalse(repo.getMoveAudioOverrides().containsKey(move.id))
        } finally {
            repo.deletePreset(id)
            repo.removeCustomMove(move.id)
            repo.saveLastUsedPreset(old ?: TrainingPreset())
        }
    }

    @Test fun globalLevelsDoNotOverwriteLegacyTemplates() = runBlocking {
        val repo = PreferencesRepository(context)
        val id = UUID.randomUUID().toString()
        val previous = repo.globalAudio.first()
        try {
            val legacy = TrainingPreset(volumeCall = .31f, volumeBeep = .42f, volumeTick = .13f)
            repo.savePreset(SavedPreset(id, "Legacy levels", legacy))
            repo.saveGlobalAudio(AudioLevels(.8f, .7f, .6f))
            assertEquals(legacy, repo.getSavedPresets().first { it.id == id }.preset)
            assertEquals(.8f, repo.globalAudio.first()!!.applyTo(legacy).volumeCall)
            repo.saveGlobalAudio(null)
            assertNull(repo.globalAudio.first())
            assertEquals(.31f, repo.getSavedPresets().first { it.id == id }.preset.volumeCall)
        } finally { repo.deletePreset(id); repo.saveGlobalAudio(previous) }
    }
}

class LibraryFlowTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun showApp() {
        val vm = HaulaufViewModel(context.applicationContext as Application)
        compose.setContent { MyApplicationTheme(darkTheme = true) { HaulaufApp(vm) } }
    }
    private fun capture(name: String) {
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(1000)
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        android.os.SystemClock.sleep(800)
        val shot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(context.getExternalFilesDir(null), "$name.png").outputStream().use { shot.compress(Bitmap.CompressFormat.PNG, 100, it) }
        shot.recycle()
    }
    @Test fun homeSettingsCanBeOpenedAgainAfterReturning() {
        showApp()
        compose.onNodeWithContentDescription(StringsDe.settings).performClick()
        compose.onNodeWithText(StringsDe.settings).assertIsDisplayed()
        compose.onNodeWithContentDescription(StringsDe.close).performClick()
        compose.onNodeWithContentDescription(StringsDe.settings).assertIsDisplayed().performClick()
        compose.onNodeWithText(StringsDe.settings).assertIsDisplayed()
    }

    @Test fun editRetainsIdentityCopyGetsNewIdentityAndCancelKeepsSavedValue() {
        val repo = PreferencesRepository(context)
        val id = UUID.randomUUID().toString()
        runBlocking { repo.savePreset(SavedPreset(id, "Prüftraining", TrainingPreset(rounds = 3, unitsPerRound = 10))) }
        try {
            showApp()
            compose.onNodeWithText(StringsDe.trainingsShort).performScrollTo().performClick()
            compose.waitUntil(5000) { compose.onAllNodesWithText("Prüftraining").fetchSemanticsNodes().isNotEmpty() }
            capture("library")
            compose.onNodeWithContentDescription("Weitere Aktionen").performClick()
            compose.onNodeWithText("Umbenennen").performClick()
            compose.onNodeWithText(StringsDe.presetName).performTextReplacement("Nicht übernehmen")
            compose.onNodeWithText(StringsDe.cancel).performClick()
            assertEquals("Prüftraining", runBlocking { repo.getSavedPresets().first { it.id == id }.name })
            compose.onNodeWithContentDescription("Weitere Aktionen").performClick()
            compose.onNodeWithText("Umbenennen").performClick()
            compose.onNodeWithText(StringsDe.presetName).performTextReplacement("   ")
            compose.onNodeWithText(StringsDe.save).assertIsNotEnabled()
            compose.onNodeWithText(StringsDe.presetName).performTextReplacement("Direkt umbenannt")
            capture("library-rename")
            compose.onNodeWithText(StringsDe.save).performClick()
            compose.waitUntil(5000) { runBlocking { repo.getSavedPresets().any { it.id == id && it.name == "Direkt umbenannt" } } }
            assertEquals(TrainingPreset(rounds = 3, unitsPerRound = 10), runBlocking { repo.getSavedPresets().first { it.id == id }.preset })
            compose.onAllNodesWithText(StringsDe.edit).onFirst().performClick()
            compose.onNodeWithText("Direkt umbenannt").assertIsDisplayed()
            capture("setup")
            compose.onNodeWithText("Übungen auswählen (19)").performClick()
            compose.onNodeWithText(StringsDe.selectNone).performScrollTo().performClick()
            compose.onNodeWithText("Nur diesmal starten").assertIsNotEnabled()
            compose.onNodeWithText(StringsDe.selectAll).performScrollTo().performClick()
            compose.onNodeWithText("Nur diesmal starten").assertIsEnabled()
            compose.onNodeWithText("Auswahl schließen (19)").performScrollTo()
            capture("setup-selection")
            compose.onNodeWithText("Auswahl schließen (19)").performClick()
            compose.onNodeWithText(StringsDe.advancedSettings).performScrollTo().performClick()
            compose.onNodeWithText("5").performScrollTo()
            capture("setup-advanced")
            compose.onNodeWithText("Erweitert schließen").performScrollTo().performClick()
            compose.onAllNodesWithText(StringsDe.save).onLast().performClick()
            compose.onNodeWithText(StringsDe.presetName).performTextReplacement("Umbenannt")
            compose.onAllNodesWithText(StringsDe.save).onLast().performClick()
            compose.waitUntil(5000) { runBlocking { repo.getSavedPresets().any { it.id == id && it.name == "Umbenannt" } } }
            assertEquals(1, runBlocking { repo.getSavedPresets().count { it.id == id } })
            compose.onNodeWithContentDescription("Weitere Aktionen").performClick()
            compose.onNodeWithText("Kopie erstellen").performClick()
            compose.onNodeWithText(StringsDe.savePreset).performClick()
            compose.onNodeWithText(StringsDe.presetName).performTextInput("Prüfkopie")
            compose.onAllNodesWithText(StringsDe.save).onLast().performClick()
            compose.waitUntil(5000) { runBlocking { repo.getSavedPresets().any { it.name == "Prüfkopie" && it.id != id } } }
        } finally {
            runBlocking { repo.getSavedPresets().filter { it.id == id || it.name == "Prüfkopie" }.forEach { repo.deletePreset(it.id) } }
        }
    }

    @Test fun renameFailureKeepsNameAndAllowsRetry() {
        val saved = SavedPreset("rename-test", "Original", TrainingPreset())
        var attempts = 0
        compose.setContent {
            CompositionLocalProvider(LocalStrings provides StringsDe) {
                MyApplicationTheme(darkTheme = true) {
                    PresetLibraryScreen(listOf(saved), DEFAULT_MOVES,
                        onCreateNew = {}, onStartPreset = {}, onEditPreset = {}, onDeletePreset = {}, onBack = {},
                        onRenamePreset = { id, title, done, failed ->
                            assertEquals(saved.id, id)
                            assertEquals("Neuer Name", title)
                            attempts++
                            if (attempts == 1) failed() else done()
                        })
                }
            }
        }
        compose.onNodeWithContentDescription("Weitere Aktionen").performClick()
        compose.onNodeWithText("Umbenennen").performClick()
        compose.onNodeWithText(StringsDe.presetName).performTextReplacement("Neuer Name")
        compose.onNodeWithText(StringsDe.save).performClick()
        compose.onNodeWithText("Neuer Name").assertExists()
        compose.onNodeWithText("Umbenennen fehlgeschlagen. Bitte erneut versuchen. Dein eingegebener Name bleibt erhalten.").assertExists()
        capture("library-rename-error")
        compose.onNodeWithText(StringsDe.save).performClick()
        compose.onNodeWithText("Training umbenennen").assertDoesNotExist()
        compose.runOnIdle { assertEquals(2, attempts) }
    }

    @Test fun creatingExerciseOpensEditorAndCanChangeCategory() {
        val repo = PreferencesRepository(context)
        try {
            showApp()
            capture("home")
            compose.onAllNodesWithText(StringsDe.movesShort).onLast().performScrollTo().performClick()
            capture("exercise-library")
            compose.onAllNodesWithText(StringsDe.addMove).onFirst().performScrollTo().performClick()
            compose.onNodeWithText(StringsDe.newMoveName).performTextInput("Neu angelegte Prüfhaltung")
            compose.onNodeWithText(StringsDe.hut).performClick()
            compose.onAllNodesWithText(StringsDe.addMove).onLast().performClick()
            compose.waitUntil(5000) { compose.onAllNodesWithText("Eigene Übung").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Neu angelegte Prüfhaltung").assertExists()
            compose.onNodeWithText(StringsDe.stich).performClick()
            compose.onNodeWithText(StringsDe.save).performClick()
            compose.waitUntil(5000) { runBlocking { repo.customMoves.first().any { it.displayName == "Neu angelegte Prüfhaltung" && it.category == MoveCategory.STICH } } }
            compose.waitUntil(5000) { compose.onAllNodesWithText("Wird gespeichert …").fetchSemanticsNodes().isEmpty() }
            capture("exercise-new")
        } finally { runBlocking { repo.customMoves.first().filter { it.displayName == "Neu angelegte Prüfhaltung" }.forEach { repo.removeCustomMove(it.id) } } }
    }

    @Test fun gloveControlsExposeStopOnlyAfterPause() {
        val state = kotlinx.coroutines.flow.MutableStateFlow<com.example.myapplication.training.TrainingState>(
            com.example.myapplication.training.TrainingState.WaitingWindow("Twerhau", 2, 1, 10, 3, 400, 1200))
        val paused = com.example.myapplication.training.TrainingState.Paused(com.example.myapplication.training.TrainingState.CallingState.InCall("Twerhau", 2, 1, 10, 3))
        var stopped = false
        compose.setContent { CompositionLocalProvider(LocalStrings provides StringsDe) { MyApplicationTheme(darkTheme = true) {
            androidx.compose.material3.Surface { TrainingScreen(TrainingPreset(), state, { state.value = paused },
                { state.value = com.example.myapplication.training.TrainingState.StartCountdown(5) }, {}, { stopped = true }) }
        } } }
        compose.onNodeWithText(StringsDe.stop).assertDoesNotExist()
        capture("training")
        compose.onNodeWithText(StringsDe.pause).performScrollTo().performClick()
        compose.onNodeWithText(StringsDe.resume).assertIsDisplayed()
        capture("training-paused")
        compose.onNodeWithText(StringsDe.stop).performScrollTo().performClick()
        compose.runOnIdle { assertTrue(stopped) }
        compose.runOnIdle { stopped = false }
        androidx.test.espresso.Espresso.pressBack()
        compose.runOnIdle { assertTrue(stopped) }
    }

    @Test fun exerciseDraftDiscardsThenSavesTogetherAndShowsAffectedTrainings() {
        val repo = PreferencesRepository(context)
        val exercise = runBlocking { repo.addCustomMove("Eigene Testübung", MoveCategory.HAU) }
        val templateId = UUID.randomUUID().toString()
        runBlocking { repo.savePreset(SavedPreset(templateId, "Verwendet Testübung", TrainingPreset(selectedMoveIds = setOf(exercise.id)))) }
        var exited = false
        try {
            compose.setContent {
                CompositionLocalProvider(LocalStrings provides StringsDe) {
                    MyApplicationTheme(darkTheme = true) {
                        ExerciseEditor(exercise.id, listOf(exercise), null,
                            listOf(SavedPreset(templateId, "Verwendet Testübung", TrainingPreset(selectedMoveIds = setOf(exercise.id)))),
                            onSave = { move, audio, done, _ -> runBlocking { repo.saveExercise(move, audio) }; done() },
                            onPreview = { _, _ -> }, onDelete = { _, _, _ -> }, onBack = { exited = true })
                    }
                }
            }
            compose.onNodeWithText(StringsDe.save).assertIsNotEnabled()
            compose.onNodeWithContentDescription(StringsDe.back).performClick()
            compose.onNodeWithText("Änderungen speichern?").assertDoesNotExist()
            compose.runOnIdle { assertTrue(exited); exited = false }
            compose.onNodeWithText(StringsDe.descriptionFieldLabel).performTextInput("Neue Beschreibung")
            assertFalse(runBlocking { repo.getMoveInfo().containsKey(exercise.id) })
            compose.onNodeWithContentDescription(StringsDe.back).performClick()
            compose.onNodeWithText("Änderungen speichern?").assertIsDisplayed()
            compose.onNodeWithText("Verwerfen").assertIsDisplayed()
            compose.onNodeWithText("Verwerfen").performClick()
            compose.runOnIdle { assertTrue(exited) }
            assertFalse(runBlocking { repo.getMoveInfo().containsKey(exercise.id) })
            compose.onAllNodesWithText(StringsDe.save).onLast().performClick()
            compose.waitUntil(3000) { runBlocking { repo.getMoveInfo()[exercise.id]?.first == "Neue Beschreibung" } }
            compose.onNodeWithText(StringsDe.descriptionFieldLabel).performScrollTo()
            capture("exercise-editor")
            compose.onNodeWithText(StringsDe.deleteMoveAction).performScrollTo().performClick()
            compose.onNodeWithText("Wird auch aus diesen Trainings entfernt: Verwendet Testübung").assertIsDisplayed()
            capture("exercise-delete")
        } finally { runBlocking { repo.removeCustomMove(exercise.id); repo.deletePreset(templateId) } }
    }

    @Test fun exerciseErrorsPreserveDraftAndRetryWithoutStaleSuccess() {
        val exercise = Move("custom_harden", "Prüfübung", MoveCategory.HAU)
        var saves = 0
        var deletes = 0
        var exited = false
        var deleteSuccess: (() -> Unit)? = null
        compose.setContent { CompositionLocalProvider(LocalStrings provides StringsDe) { MyApplicationTheme(darkTheme = true) {
            ExerciseEditor(exercise.id, listOf(exercise), null, emptyList(),
                onSave = { _, _, done, failed -> saves++; if (saves == 1) failed() else done() },
                onPreview = { _, _ -> }, onDelete = { _, done, failed -> deletes++; if (deletes == 1) failed() else deleteSuccess = done },
                onBack = { exited = true })
        } } }
        val longName = "Lange Übung – äöü ⚔ 日本語 فارسی ".repeat(5)
        compose.onNodeWithText(StringsDe.newMoveName).performTextReplacement("   ")
        compose.onNodeWithText("Bitte gib der Übung einen Namen.").assertExists()
        compose.onNodeWithText(StringsDe.save).assertIsNotEnabled()
        compose.onNodeWithText(StringsDe.newMoveName).performTextReplacement(longName)
        compose.onNodeWithContentDescription(StringsDe.back).performClick()
        compose.onAllNodesWithText(StringsDe.save).onLast().performClick()
        compose.onNodeWithText("Änderungen speichern?").assertDoesNotExist()
        compose.onNodeWithText("Speichern fehlgeschlagen. Deine Änderungen bleiben im Editor. Bitte erneut speichern.").performScrollTo()
        capture("editor-save-error")
        compose.onNodeWithText(longName).assertExists()
        compose.runOnIdle { assertFalse(exited) }
        compose.onNodeWithText(StringsDe.save).performClick()
        compose.onNodeWithText("Änderungen gespeichert.").assertExists()
        compose.onNodeWithText(StringsDe.save).assertIsNotEnabled()
        compose.onNodeWithText(StringsDe.descriptionFieldLabel).performScrollTo().performTextInput("Weitere Änderung")
        compose.onNodeWithText("Änderungen gespeichert.").assertDoesNotExist()
        compose.onNodeWithText(StringsDe.save).assertIsEnabled()
        compose.onNodeWithText(StringsDe.deleteMoveAction).performScrollTo().performClick()
        compose.onNodeWithText(StringsDe.delete).performClick()
        compose.onNodeWithText("Löschen fehlgeschlagen. Bitte erneut versuchen.").assertIsDisplayed()
        capture("editor-delete-error")
        compose.runOnIdle { assertFalse(exited) }
        compose.onNodeWithText(StringsDe.delete).performClick()
        compose.onNodeWithText("Wird gelöscht …").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(2, deletes); assertEquals(2, saves); deleteSuccess!!() }
        compose.runOnIdle { assertTrue(exited) }
    }

    @Test fun invalidMediaKeepsDraftAndValidImageCanBeSaved() {
        val exercise = Move("custom_media", "Medientest", MoveCategory.HUT)
        val file = File(context.cacheDir, "editor-import-test.png")
        var savedImage: String? = null
        var saved = false
        androidx.test.espresso.intent.Intents.init()
        try {
            file.writeText("Not an image or audio file")
            androidx.test.espresso.intent.Intents.intending(androidx.test.espresso.intent.matcher.IntentMatchers.hasAction(android.content.Intent.ACTION_OPEN_DOCUMENT))
                .respondWith(android.app.Instrumentation.ActivityResult(android.app.Activity.RESULT_OK, android.content.Intent().setData(android.net.Uri.fromFile(file))))
            compose.setContent { CompositionLocalProvider(LocalStrings provides StringsDe) { MyApplicationTheme(darkTheme = true) {
                ExerciseEditor(exercise.id, listOf(exercise), null, emptyList(),
                    onSave = { move, _, done, _ -> savedImage = move.imagePath; saved = true; done() },
                    onPreview = { _, _ -> }, onDelete = { _, _, _ -> }, onBack = {})
            } } }
            compose.onNodeWithText("Audio und Bild anpassen").performScrollTo().performClick()
            compose.onNodeWithText(StringsDe.chooseImage).performScrollTo().performClick()
            val error = "Datei nicht lesbar oder Format nicht unterstützt. Bitte eine andere Datei wählen."
            compose.waitUntil(5000) { compose.onAllNodesWithText(error).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(StringsDe.save).assertIsNotEnabled()
            compose.onNodeWithText(StringsDe.chooseFile).performScrollTo().performClick()
            compose.waitUntil(5000) { compose.onAllNodesWithText("Datei wird geprüft …").fetchSemanticsNodes().isEmpty() }
            compose.onNodeWithText(error).performScrollTo()
            capture("editor-import-error")
            compose.onNodeWithText(StringsDe.save).assertIsNotEnabled()
            val bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.YELLOW)
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            compose.onNodeWithText(StringsDe.chooseImage).performScrollTo().performClick()
            compose.waitUntil(5000) { compose.onAllNodesWithText("Ausgewählt – noch nicht gespeichert.").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(error).assertDoesNotExist()
            compose.onNodeWithText(StringsDe.save).performClick()
            compose.runOnIdle { assertTrue(saved); assertNotNull(savedImage); assertTrue(File(savedImage!!).exists()) }
        } finally {
            androidx.test.espresso.intent.Intents.release()
            file.delete()
            savedImage?.let { File(it).delete() }
        }
    }
}
