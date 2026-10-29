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
            ExerciseEditor(original.id, listOf(original), null, emptyList(),
                onSave = { move, _, success, _ -> saved = move; success() }, onPreview = { _, _ -> }, onDelete = { _, _, _ -> }, onBack = {})
        } }
        compose.onNodeWithText(StringsDe.descriptionFieldLabel).assertDoesNotExist()
        compose.onNodeWithText("Meine Notizen").performTextInput("Schritt beachten | ü")
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
            ExerciseEditor(original.id, listOf(original), null, emptyList(),
                onSave = { _, _, _, _ -> saves++ }, onPreview = { _, _ -> }, onDelete = { _, _, _ -> }, onBack = { left = true })
        } }
        compose.onNodeWithText("Meine Notizen").performTextInput("Unsaved")
        compose.onNodeWithContentDescription(StringsDe.back).performClick()
        compose.onNodeWithText("Verwerfen").performClick()
        compose.runOnIdle { assertEquals(0, saves); assertTrue(left) }
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
            compose.onNodeWithText(StringsDe.editMove).performClick()
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
