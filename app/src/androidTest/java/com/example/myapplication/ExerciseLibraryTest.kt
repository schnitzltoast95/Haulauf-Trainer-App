package com.example.myapplication

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.*
import com.example.myapplication.ui.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class ExerciseLibraryTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun cardsOpenReadOnlyDetailsAndSelectionDoesNotOpenEditor() {
        var opened: String? = null
        var chosen = emptySet<String>()
        compose.setContent {
            var selection by remember { mutableStateOf(emptySet<String>()) }
            MyApplicationTheme(darkTheme = true) {
                MoveAudioListScreen(DEFAULT_MOVES, emptyMap(), { _, _ -> }, {}, { opened = it }, {},
                    selectedIds = selection, onToggle = { id ->
                        selection = if (id in selection) selection - id else selection + id
                        chosen = selection
                    }, onApplySelection = {})
            }
        }
        compose.onNodeWithText("Übung suchen").performTextInput("Oberhau")
        compose.onNodeWithContentDescription("Oberhau fürs Training auswählen").performClick()
        compose.runOnIdle { assertEquals(setOf("oberhau"), chosen); assertNull(opened) }
        compose.onNode(hasText("Oberhau") and !hasSetTextAction()).performClick()
        compose.runOnIdle { assertEquals("oberhau", opened) }
        compose.onNodeWithText("Übung suchen").performTextClearance()
        compose.onNodeWithText("Huten", substring = false).performClick()
        compose.onNodeWithText("Oberhau", substring = false).assertDoesNotExist()
        compose.onNodeWithText("Ochs", substring = false).assertExists()
    }

    @Test fun defaultEditorProtectsOriginalAndSavesNotes() {
        val original = DEFAULT_MOVES.first()
        var saved: Move? = null
        compose.setContent { MyApplicationTheme(darkTheme = true) {
            ExerciseEditor(original.id, listOf(original), null, emptyList(), section = ExerciseSection.NOTES,
                onSave = { move, _, success, _ -> saved = move; success() }, onPreview = { _, _ -> }, onDelete = { _, _, _ -> }, onBack = {})
        } }
        compose.onNodeWithText(StringsDe.descriptionFieldLabel).assertDoesNotExist()
        compose.onNodeWithText("Notizen").performTextInput("Schritt beachten | ü")
        compose.onNodeWithText(StringsDe.save, substring = false).performClick()
        compose.runOnIdle {
            assertEquals(original.description, saved!!.description)
            assertEquals(original.historicalTextSources, saved!!.historicalTextSources)
            assertEquals("Schritt beachten | ü", saved!!.notes)
        }
    }

    @Test fun librarySelectionIsAppliedToTrainingOnlyOnConfirmation() {
        var started: TrainingPreset? = null
        compose.setContent { MyApplicationTheme(darkTheme = true) {
            TrainingEditor(TrainingPreset(selectedMoveIds = setOf("oberhau", "unterhau")), null, DEFAULT_MOVES,
                onStart = { started = it }, onSave = { _, _, _, _, _ -> }, onSaved = {}, onBack = {})
        } }
        compose.onNodeWithText("Übungen auswählen (2)").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Oberhau fürs Training auswählen").performClick()
        compose.onNodeWithContentDescription(StringsDe.back).performClick()
        compose.onNodeWithText("Übungen auswählen (2)").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Oberhau fürs Training auswählen").assertIsOn().performClick()
        compose.onNodeWithText("1 Übung übernehmen").performClick()
        compose.onNodeWithText("Übungen auswählen (1)").assertExists()
        compose.onNodeWithText(StringsDe.startTraining).performClick()
        compose.runOnIdle { assertEquals(setOf("unterhau"), started!!.selectedMoveIds) }
    }

    @Test fun discardedNotesNeverReachPersistenceCallback() {
        val original = DEFAULT_MOVES.first()
        var saves = 0
        var left = false
        compose.setContent { MyApplicationTheme(darkTheme = true) {
            ExerciseEditor(original.id, listOf(original), null, emptyList(), section = ExerciseSection.NOTES,
                onSave = { _, _, _, _ -> saves++ }, onPreview = { _, _ -> }, onDelete = { _, _, _ -> }, onBack = { left = true })
        } }
        compose.onNodeWithText("Notizen").performTextInput("Unsaved")
        compose.onNodeWithContentDescription(StringsDe.back).performClick()
        compose.onNodeWithText("Verwerfen").performClick()
        compose.runOnIdle { assertEquals(0, saves); assertTrue(left) }
    }

    @Test fun savingOneSectionPreservesNewerChangesInOtherSections() = runBlocking {
        val repo = PreferencesRepository(context)
        val move = repo.addCustomMove("Section isolation", MoveCategory.HAU)
        try {
            val stale = move.copy(notes = "Old note", media = listOf(ExerciseMedia("/old/image")))
            repo.saveExercise(stale, "content://old/audio")
            repo.saveExercise(move.copy(displayName = "New title", description = "New description"), null, ExerciseSection.DATA)
            repo.saveExercise(stale.copy(media = listOf(ExerciseMedia("/new/video", ExerciseMediaKind.VIDEO))), null, ExerciseSection.MEDIA)
            repo.saveExercise(stale.copy(notes = "New note"), null, ExerciseSection.NOTES)
            assertEquals("New title", repo.customMoves.first().first { it.id == move.id }.displayName)
            assertEquals("New description", repo.getMoveInfo()[move.id]?.first)
            assertEquals("New note", repo.exerciseAdditions.first().getValue(move.id).notes)
            assertEquals("/new/video", repo.exerciseAdditions.first().getValue(move.id).media.single().path)
            assertEquals("content://old/audio", repo.getMoveAudioOverrides()[move.id])
            repo.saveExercise(stale, "content://new/audio", ExerciseSection.AUDIO)
            assertEquals("New note", repo.exerciseAdditions.first().getValue(move.id).notes)
            assertEquals("/new/video", repo.exerciseAdditions.first().getValue(move.id).media.single().path)
            assertEquals("content://new/audio", repo.getMoveAudioOverrides()[move.id])
        } finally { repo.removeCustomMove(move.id) }
    }

    @Test fun addMediaOpensPickerDirectlyAndKeepsImportAsDraft() {
        val source = File(context.cacheDir, "direct-media-test.png")
        val image = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        source.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
        var saves = 0
        var left = false
        androidx.test.espresso.intent.Intents.init()
        try {
            androidx.test.espresso.intent.Intents.intending(androidx.test.espresso.intent.matcher.IntentMatchers.hasAction(android.content.Intent.ACTION_OPEN_DOCUMENT))
                .respondWith(android.app.Instrumentation.ActivityResult(android.app.Activity.RESULT_OK, android.content.Intent().setData(Uri.fromFile(source))))
            val move = DEFAULT_MOVES.first()
            compose.setContent { MyApplicationTheme(darkTheme = true) {
                ExerciseEditor(move.id, listOf(move), null, emptyList(), section = ExerciseSection.MEDIA, addMediaOnOpen = true,
                    onSave = { _, _, _, _ -> saves++ }, onPreview = { _, _ -> }, onDelete = { _, _, _ -> }, onBack = { left = true })
            } }
            compose.waitUntil(5000) { compose.onAllNodesWithText("Ausgewählt – noch nicht gespeichert.").fetchSemanticsNodes().isNotEmpty() }
            androidx.test.espresso.intent.Intents.intended(androidx.test.espresso.intent.matcher.IntentMatchers.hasAction(android.content.Intent.ACTION_OPEN_DOCUMENT))
            compose.onNodeWithText("Medium 1 entfernen").assertExists()
            compose.onNodeWithContentDescription(StringsDe.back).performClick()
            compose.onNodeWithText("Verwerfen").performClick()
            compose.runOnIdle { assertTrue(left); assertEquals(0, saves) }
        } finally { androidx.test.espresso.intent.Intents.release(); source.delete() }
    }

    @Test fun audioResetIsPreviewableAndCanBeDiscarded() {
        var preview: String? = "unset"
        var saved = false
        var left = false
        val move = DEFAULT_MOVES.first()
        compose.setContent { MyApplicationTheme(darkTheme = true) {
            ExerciseEditor(move.id, listOf(move), "content://original/audio", emptyList(), section = ExerciseSection.AUDIO,
                onSave = { _, _, _, _ -> saved = true }, onPreview = { _, uri -> preview = uri }, onDelete = { _, _, _ -> }, onBack = { left = true })
        } }
        compose.onNodeWithText("Ansage anhören").performClick()
        compose.runOnIdle { assertEquals("content://original/audio", preview) }
        compose.onNodeWithText("App-Stimme verwenden").performClick()
        compose.onNodeWithText("Ansage anhören").performClick()
        compose.runOnIdle { assertNull(preview) }
        compose.onNodeWithContentDescription(StringsDe.back).performClick()
        compose.onNodeWithText("Verwerfen").performClick()
        compose.runOnIdle { assertTrue(left); assertFalse(saved) }
    }

    @Test fun sectionEditorOffersContinueEditingAndReturnsAfterSave() {
        val move = DEFAULT_MOVES.first()
        var left = false
        var saved: Move? = null
        compose.setContent { MyApplicationTheme(darkTheme = true) {
            ExerciseEditor(move.id, listOf(move), null, emptyList(), section = ExerciseSection.NOTES,
                onSave = { value, _, done, _ -> saved = value; done() }, onPreview = { _, _ -> }, onDelete = { _, _, _ -> }, onBack = { left = true })
        } }
        compose.onNodeWithText("Notizen").performTextInput("Keep this note")
        compose.onNodeWithContentDescription(StringsDe.back).performClick()
        compose.onNodeWithText("Weiter bearbeiten").performClick()
        compose.runOnIdle { assertFalse(left); assertNull(saved) }
        compose.onNodeWithText("Keep this note").assertExists()
        compose.onNodeWithText(StringsDe.save).performClick()
        compose.runOnIdle { assertTrue(left); assertEquals("Keep this note", saved!!.notes) }
    }

    @Test fun detailActionsOpenSpecificEditorsAndKeepScrollPosition() {
        val repo = PreferencesRepository(context)
        val move = runBlocking { repo.addCustomMove("Workflow test", MoveCategory.HAU) }
        try {
            val vm = HaulaufViewModel(context.applicationContext as android.app.Application)
            compose.setContent { MyApplicationTheme(darkTheme = true) { HaulaufApp(vm) } }
            compose.onAllNodesWithText(StringsDe.movesShort).onLast().performScrollTo().performClick()
            compose.onNodeWithText("Übung suchen").performTextInput(move.displayName)
            compose.waitUntil(5000) { compose.onAllNodes(hasText(move.displayName) and !hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNode(hasText(move.displayName) and !hasSetTextAction()).performClick()
            compose.onNodeWithContentDescription("Übung bearbeiten").performClick()
            compose.onNodeWithText("Ansage bearbeiten").performClick()
            compose.onNodeWithText(StringsDe.chooseFile).assertExists()
            compose.onNodeWithText("Notizen").assertDoesNotExist()
            compose.onNodeWithContentDescription(StringsDe.back).performClick()
            compose.onNodeWithContentDescription("Übung bearbeiten").performClick()
            compose.onNodeWithText("Notizen bearbeiten").performClick()
            compose.onNodeWithText(StringsDe.chooseFile).assertDoesNotExist()
            compose.onNodeWithText(StringsDe.descriptionFieldLabel).assertDoesNotExist()
            compose.onNodeWithText("Notizen").performTextInput("Saved independently")
            compose.onNodeWithText(StringsDe.save).performClick()
            compose.waitUntil(5000) { runBlocking { repo.exerciseAdditions.first()[move.id]?.notes == "Saved independently" } }
            compose.onNodeWithText("Saved independently").assertIsDisplayed()
            compose.onNodeWithContentDescription("Übung bearbeiten").assertIsDisplayed()
        } finally { runBlocking { repo.removeCustomMove(move.id) } }
    }

    @Test fun standardDetailMenuOffersSectionsButProtectsDescription() {
        var edited: ExerciseSection? = null
        compose.setContent { MyApplicationTheme(darkTheme = true) {
            ExerciseDetailScreen(DEFAULT_MOVES.first(), onEdit = { edited = it }, onBack = {}, onPreview = {})
        } }
        compose.onNodeWithContentDescription("Ansage anhören").assertExists()
        compose.onNodeWithContentDescription("Medien hinzufügen").assertDoesNotExist()
        compose.onNodeWithContentDescription("Übung bearbeiten").performClick()
        compose.onNodeWithText("Beschreibung bearbeiten").assertIsNotEnabled()
        compose.onNodeWithText("Medien bearbeiten").performClick()
        compose.runOnIdle { assertEquals(ExerciseSection.MEDIA, edited) }
        compose.onNodeWithContentDescription("Übung bearbeiten").performClick()
        compose.onNodeWithText("Notizen bearbeiten").performClick()
        compose.runOnIdle { assertEquals(ExerciseSection.NOTES, edited) }
    }

    @Test fun compactReportCanExpandMinimizeAndDockWithoutSubmitting() {
        org.junit.Assume.assumeTrue(context.getString(com.example.myapplication.R.string.bug_report_token).isNotBlank())
        val prefs = context.getSharedPreferences("bug_report_button", android.content.Context.MODE_PRIVATE)
        val before = prefs.all
        prefs.edit().putBoolean("compact", true).putString("edge", "right").putBoolean("right", true).putFloat("vertical_fraction", 0.5f).commit()
        try {
            compose.setContent { MyApplicationTheme(darkTheme = true) { BugReportOverlay({ "move_details/test" }, "de") } }
            compose.onNodeWithText("Fehler melden").assertDoesNotExist()
            compose.onNodeWithContentDescription("Fehler melden. Zum Verschieben ziehen.").performClick()
            compose.onNodeWithText("Meldebutton vergrößern").performClick()
            compose.onNodeWithText("Abbrechen").performClick()
            compose.onNodeWithContentDescription("Meldebutton minimieren").performClick()
            compose.onNodeWithText("Fehler melden").assertDoesNotExist()
            compose.runOnIdle { assertTrue(prefs.getBoolean("compact", false)) }
            val root = compose.onRoot().fetchSemanticsNode().boundsInRoot
            listOf(
                "top" to androidx.compose.ui.geometry.Offset(root.width / 2, 2f),
                "bottom" to androidx.compose.ui.geometry.Offset(root.width / 2, root.height - 2f),
                "left" to androidx.compose.ui.geometry.Offset(2f, root.height / 2),
                "right" to androidx.compose.ui.geometry.Offset(root.width - 2f, root.height / 2)
            ).forEach { (edge, destination) ->
                val start = compose.onNodeWithContentDescription("Fehler melden. Zum Verschieben ziehen.").fetchSemanticsNode().boundsInRoot.center
                compose.onRoot().performTouchInput { swipe(start, destination, 600) }
                compose.waitForIdle()
                compose.runOnIdle { assertEquals(edge, prefs.getString("edge", "")) }
            }
        } finally {
            val editor = prefs.edit().clear()
            before.forEach { (key, value) -> when(value) { is Boolean -> editor.putBoolean(key, value); is Float -> editor.putFloat(key, value); is String -> editor.putString(key, value) } }
            editor.commit()
        }
    }

    @Test fun replacingResettingAndDeletingAudioCleansOnlyOwnedFiles() = runBlocking {
        val repo = PreferencesRepository(context)
        val move = repo.addCustomMove("Audio cleanup test", MoveCategory.HAU)
        val directory = File(context.filesDir, "exercise-media").apply { mkdirs() }
        val files = (1..3).map { File.createTempFile("cleanup", ".m4a", directory).apply { writeText("fixture") } }
        val external = File.createTempFile("external", ".m4a", context.cacheDir)
        try {
            repo.saveExercise(move, Uri.fromFile(files[0]).toString())
            repo.saveExercise(move, Uri.fromFile(files[1]).toString())
            assertFalse(files[0].exists()); assertTrue(files[1].exists())
            repo.saveExercise(move, null)
            assertFalse(files[1].exists())
            repo.saveExercise(move, Uri.fromFile(external).toString())
            repo.saveExercise(move, Uri.fromFile(files[2]).toString())
            assertTrue(external.exists())
            repo.removeCustomMove(move.id)
            assertFalse(files[2].exists())
        } finally { repo.removeCustomMove(move.id); files.forEach { it.delete() }; external.delete() }
    }

    @Test fun deletingFromDetailEditorReturnsToLibrary() {
        val repo = PreferencesRepository(context)
        val move = runBlocking { repo.addCustomMove("Navigation deletion test", MoveCategory.HAU) }
        try {
            val vm = HaulaufViewModel(context.applicationContext as android.app.Application)
            compose.setContent { MyApplicationTheme(darkTheme = true) { HaulaufApp(vm) } }
            compose.onAllNodesWithText(StringsDe.movesShort).onLast().performScrollTo().performClick()
            compose.onNodeWithText("Übung suchen").performTextInput(move.displayName)
            compose.waitUntil(5000) { compose.onAllNodes(hasText(move.displayName) and !hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNode(hasText(move.displayName) and !hasSetTextAction()).performClick()
            compose.onNodeWithContentDescription("Übung bearbeiten").performClick()
            compose.onNodeWithText("Beschreibung bearbeiten").performClick()
            compose.onNodeWithText(StringsDe.deleteMoveAction).performScrollTo().performClick()
            compose.onNodeWithText(StringsDe.delete, substring = false).performClick()
            compose.waitUntil(5000) { compose.onAllNodesWithText("Übung suchen").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithContentDescription(StringsDe.addMove).assertExists()
            compose.onNode(hasText(move.displayName) and !hasSetTextAction()).assertDoesNotExist()
        } finally { runBlocking { repo.removeCustomMove(move.id) } }
    }

    @Test fun importsSurviveSourceRemovalAndPersistWithNotes() = runBlocking {
        val repo = PreferencesRepository(context)
        val move = repo.addCustomMove("Media persistence test", MoveCategory.HAU)
        val source = File(context.cacheDir, "library-source.png")
        val imported = mutableListOf<File>()
        try {
            Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).also { bitmap ->
                source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
            val image = importExerciseMedia(context, Uri.fromFile(source), true)
            imported.add(File(image.path))
            source.writeBytes(android.util.Base64.decode("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7", android.util.Base64.DEFAULT))
            val gif = importExerciseMedia(context, Uri.fromFile(source), true)
            imported.add(File(gif.path))
            source.delete()
            repo.saveExercise(move.copy(description = "Editable", notes = "Notiz | ü\nzweite Zeile", media = listOf(image, gif)), null)
            val reread = PreferencesRepository(context).exerciseAdditions.first().getValue(move.id)
            assertEquals("Notiz | ü\nzweite Zeile", reread.notes)
            assertEquals(listOf(image, gif), reread.media)
            assertTrue(reread.media.all { File(it.path).length() > 0 })
            source.writeText("not an image or a video")
            val before = File(context.filesDir, "exercise-media").listFiles()!!.map { it.name }.toSet()
            assertTrue(runCatching { importExerciseMedia(context, Uri.fromFile(source), true) }.isFailure)
            assertEquals(before, File(context.filesDir, "exercise-media").listFiles()!!.map { it.name }.toSet())
            repo.removeCustomMove(move.id)
            assertFalse(repo.exerciseAdditions.first().containsKey(move.id))
        } finally { repo.removeCustomMove(move.id); source.delete(); imported.forEach { it.delete() } }
    }
}
