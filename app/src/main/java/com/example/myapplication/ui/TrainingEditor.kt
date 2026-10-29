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
    var confirmExit by remember { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf(saved?.name.orEmpty()) }
    var error by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val de = LocalStrings.current === StringsDe
    val back: () -> Unit = { if (draft != initial) confirmExit = true else onBack() }
    fun save(title: String, id: String?) {
        if (!busy && title.isNotBlank()) {
            busy = true
            onSave(title.trim(), id, draft, { busy = false; onSaved() }, { busy = false; error = true })
        }
    }
    BackHandler(onBack = back)
    TrainingSetupScreen(
        preset = draft, allMoves = allMoves,
        onPresetChange = { draft = it },
        onStart = { if (!busy) onStart(draft) },
        onSavePreset = { save(it, saved?.id) },
        onBack = back,
        savedName = saved?.name,
        onSaveCopy = { save(it, null) }
    )
    if (error) AlertDialog(onDismissRequest = { error = false }, title = { Text(if (de) "Speichern fehlgeschlagen" else "Save failed") },
        text = { Text(if (de) "Deine Änderungen bleiben im Editor. Bitte erneut versuchen." else "Your changes remain in the editor. Please retry.") },
        confirmButton = { TextButton(onClick = { error = false }) { Text("OK") } })
    if (confirmExit) AlertDialog(
        onDismissRequest = { confirmExit = false },
        title = { Text(if (de) "Änderungen speichern?" else "Save changes?") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true,
            label = { Text(if (de) "Trainingsname" else "Training name") }) },
        confirmButton = { TextButton(enabled = name.isNotBlank() && !busy, onClick = { save(name, saved?.id) }) {
            Text(if (de) "Speichern" else "Save")
        } },
        dismissButton = { TextButton(onClick = onBack) { Text(if (de) "Verwerfen" else "Discard") } }
    )
}
