package com.example.myapplication.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.myapplication.data.*

@Composable
fun TrainingEditor(
    initial: TrainingPreset,
    saved: SavedPreset?,
    allMoves: List<Move>,
    onStart: (TrainingPreset) -> Unit,
    onSave: (String, String?, TrainingPreset, () -> Unit, () -> Unit) -> Unit,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    var draft by rememberSaveable { mutableStateOf(initial) }
    var choosing by rememberSaveable { mutableStateOf(false) }
    var selection by rememberSaveable { mutableStateOf(ArrayList(initial.selectedMoveIds)) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmExit by remember { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf(saved?.name.orEmpty()) }
    var error by remember { mutableStateOf(false) }
    var pendingId by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var attached by remember { mutableStateOf(true) }
    DisposableEffect(Unit) { onDispose { attached = false } }
    val currentOnSaved by rememberUpdatedState(onSaved)
    val de = LocalStrings.current === StringsDe
    val back: () -> Unit = { if (!busy) { if (draft != initial) confirmExit = true else onBack() } }
    fun save(title: String, id: String?) {
        if (!busy && title.isNotBlank()) {
            pendingId = id
            error = false
            name = title.trim()
            confirmExit = false
            busy = true
            onSave(name, id, draft,
                { if (attached && busy) { busy = false; currentOnSaved() } },
                { if (attached && busy) { busy = false; error = true } })
        }
    }
    if (choosing) {
        val detail = allMoves.find { it.id == detailId }
        BackHandler { if (detailId != null) detailId = null else choosing = false }
        if (detail != null) ExerciseDetailScreen(detail, onEdit = null, onBack = { detailId = null })
        else MoveAudioListScreen(allMoves, emptyMap(), onAddMove = { _, _ -> }, onTestMove = {},
            onOpenMoveDetails = { detailId = it }, onBack = { choosing = false },
            selectedIds = selection.toSet(), onToggle = { id -> selection = ArrayList(if (id in selection) selection - id else selection + id) },
            onApplySelection = {
                draft = draft.copy(selectedMoveIds = selection.toSet(), movePriorities = draft.movePriorities.filterKeys { it in selection })
                choosing = false
            })
        return
    }
    BackHandler(onBack = back)
    TrainingSetupScreen(
        preset = draft, allMoves = allMoves,
        onPresetChange = { if (!busy) draft = it },
        onStart = { if (!busy) onStart(draft) },
        onSavePreset = { save(it, saved?.id) },
        onBack = back,
        savedName = saved?.name,
        onBrowseExercises = { selection = ArrayList(draft.selectedMoveIds); choosing = true },
        onSaveCopy = { save(it, null) }
    )
    if (busy) AlertDialog(
        onDismissRequest = {},
        title = { Text(if (de) "Training wird gespeichert" else "Saving training") },
        text = { CircularProgressIndicator() },
        confirmButton = {},
        properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
    if (error) AlertDialog(onDismissRequest = { error = false }, title = { Text(if (de) "Speichern fehlgeschlagen" else "Save failed") },
        text = { Text(if (de) "Deine Änderungen bleiben im Editor. Bitte erneut versuchen." else "Your changes remain in the editor. Please retry.") },
        confirmButton = { TextButton(onClick = { save(name, pendingId) }) { Text(if (de) "Erneut versuchen" else "Retry") } },
        dismissButton = { TextButton(onClick = { error = false }) { Text(if (de) "Weiter bearbeiten" else "Continue editing") } })
    if (confirmExit) AlertDialog(
        onDismissRequest = { confirmExit = false },
        title = { Text(if (de) "Änderungen speichern?" else "Save changes?") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true,
            label = { Text(if (de) "Trainingsname" else "Training name") }) },
        confirmButton = { TextButton(enabled = name.isNotBlank() && !busy, onClick = { save(name, saved?.id) }) {
            Text(if (de) "Speichern" else "Save")
        } },
        dismissButton = { TextButton(enabled = !busy, onClick = backDiscard@{ if (!busy) onBack() }) { Text(if (de) "Verwerfen" else "Discard") } }
    )
}
