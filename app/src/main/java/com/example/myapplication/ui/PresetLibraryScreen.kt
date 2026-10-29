package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Move
import com.example.myapplication.data.SavedPreset
import com.example.myapplication.ui.theme.HaulaufCard
import com.example.myapplication.ui.theme.HaulaufGoldDark
import com.example.myapplication.ui.theme.HaulaufGoldLight
import com.example.myapplication.ui.theme.HaulaufTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetLibraryScreen(
    savedPresets: List<SavedPreset>,
    allMoves: List<Move>,
    showSavedFeedback: Boolean = false,
    onCreateNew: () -> Unit,
    onStartPreset: (SavedPreset) -> Unit,
    onEditPreset: (SavedPreset) -> Unit,
    onDeletePreset: (SavedPreset) -> Unit,
    onBack: () -> Unit,
    onCopyPreset: (SavedPreset) -> Unit = {},
    onRenamePreset: (String, String, () -> Unit, () -> Unit) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<SavedPreset?>(null) }
    val s = LocalStrings.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val de = s === StringsDe
    var renameId by rememberSaveable { mutableStateOf<String?>(null) }
    var renameName by rememberSaveable { mutableStateOf("") }
    var renaming by remember { mutableStateOf(false) }
    var renameError by remember { mutableStateOf(false) }

    LaunchedEffect(showSavedFeedback) {
        if (showSavedFeedback) {
            snackbarHostState.showSnackbar(message = s.presetSaved)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(s.trainingLibraryTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
                actions = {
                    IconButton(onClick = onCreateNew) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = s.newTraining
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (savedPresets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = s.noSavedPresets,
                        style = MaterialTheme.typography.titleMedium,
                        color = HaulaufTextSecondary
                    )
                    Button(onClick = onCreateNew) { Text(s.newTraining) }
                    Text(
                        text = s.noSavedPresetsHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = HaulaufTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(savedPresets, key = { it.id }) { preset ->
                    PresetCard(
                        preset = preset,
                        allMoves = allMoves,
                        onStart = { onStartPreset(preset) },
                        onEdit = { onEditPreset(preset) },
                        onDelete = { showDeleteDialog = preset },
                        onCopy = { onCopyPreset(preset) },
                        onRename = { renameName = preset.name; renameError = false; renameId = preset.id }
                    )
                }
            }
        }
    }

    renameId?.let { id ->
        AlertDialog(
            onDismissRequest = { if (!renaming) renameId = null },
            title = { Text(if (de) "Training umbenennen" else "Rename training") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = renameName,
                        onValueChange = { renameName = it; renameError = false },
                        enabled = !renaming,
                        label = { Text(s.presetName) },
                        singleLine = true,
                        isError = renameError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (renameError) Text(
                        if (de) "Umbenennen fehlgeschlagen. Bitte erneut versuchen. Dein eingegebener Name bleibt erhalten."
                        else "Could not rename. Please try again. Your entered name has been kept.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(enabled = renameName.isNotBlank() && !renaming, onClick = {
                    renaming = true
                    onRenamePreset(id, renameName.trim(), {
                        renaming = false
                        renameId = null
                        scope.launch { snackbarHostState.showSnackbar(if (de) "Training umbenannt" else "Training renamed") }
                    }, { renaming = false; renameError = true })
                }) { Text(if (renaming) { if (de) "Wird gespeichert …" else "Saving …" } else s.save) }
            },
            dismissButton = {
                TextButton(enabled = !renaming, onClick = { renameId = null }) { Text(s.cancel) }
            }
        )
    }

    // Delete confirmation dialog
    val strings = LocalStrings.current
    showDeleteDialog?.let { preset ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(strings.deletePreset) },
            text = { Text(strings.deletePresetConfirm.format(preset.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePreset(preset)
                        showDeleteDialog = null
                    }
                ) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun PresetCard(
    preset: SavedPreset,
    allMoves: List<Move>,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onRename: () -> Unit
) {
    val s = LocalStrings.current
    var menu by remember { mutableStateOf(false) }
    val de = s === StringsDe
    val selectedMoves = allMoves.filter { it.id in preset.preset.selectedMoveIds }
    val durationStr = formatDuration(preset.preset.estimatedDurationSeconds())

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HaulaufCard,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "~$durationStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = HaulaufTextSecondary
                )
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = if (de) "Weitere Aktionen" else "More actions") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text(if (de) "Kopie erstellen" else "Create copy") }, onClick = { menu = false; onCopy() })
                        DropdownMenuItem(text = { Text(if (de) "Umbenennen" else "Rename") }, onClick = { menu = false; onRename() })
                        DropdownMenuItem(text = { Text(s.delete) }, onClick = { menu = false; onDelete() })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PresetStat(
                    label = s.rounds,
                    value = preset.preset.rounds.toString()
                )
                PresetStat(
                    label = s.callsRound,
                    value = preset.preset.unitsPerRound.toString()
                )
                PresetStat(
                    label = if (de) "Übungen" else "Exercises",
                    value = selectedMoves.size.toString()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${if (de) "Übungen" else "Exercises"}: ${selectedMoves.joinToString(", ") { it.displayName }}",
                style = MaterialTheme.typography.bodySmall,
                color = HaulaufTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${s.reactionInterval}: ${
                    if (preset.preset.reactionIntervalMinMs != null) {
                        "%.2f-%.2fs".format(
                            preset.preset.reactionIntervalMinMs / 1000f,
                            preset.preset.reactionIntervalMaxMs!! / 1000f
                        )
                    } else {
                        "%.2fs".format(preset.preset.reactionIntervalMs / 1000f)
                    }
                }",
                style = MaterialTheme.typography.bodySmall,
                color = HaulaufTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${s.pauseBetweenRounds}: ${preset.preset.pauseBetweenRoundsMs / 1000}s",
                style = MaterialTheme.typography.bodySmall,
                color = HaulaufTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = if (selectedMoves.isEmpty()) onEdit else onStart,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HaulaufGoldDark
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (selectedMoves.isEmpty()) { if (de) "Übungen wählen" else "Select exercises" } else s.start)
                }
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(s.edit, maxLines = 1)
                }

            }
        }
    }
}

@Composable
private fun PresetStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = HaulaufGoldLight,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = HaulaufTextSecondary
        )
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}m ${seconds}s"
}
