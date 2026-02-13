package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onStartPreset: (SavedPreset) -> Unit,
    onEditPreset: (SavedPreset) -> Unit,
    onDeletePreset: (SavedPreset) -> Unit,
    onBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<SavedPreset?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Training Library") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                        text = "No saved presets",
                        style = MaterialTheme.typography.titleMedium,
                        color = HaulaufTextSecondary
                    )
                    Text(
                        text = "Save presets from the Configure Training screen",
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedPresets) { preset ->
                    PresetCard(
                        preset = preset,
                        allMoves = allMoves,
                        onStart = { onStartPreset(preset) },
                        onEdit = { onEditPreset(preset) },
                        onDelete = { showDeleteDialog = preset }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { preset ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Preset") },
            text = { Text("Are you sure you want to delete \"${preset.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePreset(preset)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
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
    onDelete: () -> Unit
) {
    val selectedMoves = allMoves.filter { it.id in preset.preset.selectedMoveIds }
    val durationStr = formatDuration(preset.preset.estimatedDurationSeconds())

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HaulaufCard,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with name
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
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PresetStat(
                    label = "Rounds",
                    value = preset.preset.rounds.toString()
                )
                PresetStat(
                    label = "Calls/Round",
                    value = preset.preset.unitsPerRound.toString()
                )
                PresetStat(
                    label = "Interval",
                    value = if (preset.preset.reactionIntervalMinMs != null) {
                        "${preset.preset.reactionIntervalMinMs / 1000}-${preset.preset.reactionIntervalMaxMs!! / 1000}s"
                    } else {
                        "${preset.preset.reactionIntervalMs / 1000}s"
                    }
                )
                PresetStat(
                    label = "Pause",
                    value = "${preset.preset.pauseBetweenRoundsMs / 1000}s"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Moves summary
            Text(
                text = "${selectedMoves.size} moves: ${selectedMoves.take(3).joinToString(", ") { it.displayName }}${if (selectedMoves.size > 3) "..." else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = HaulaufTextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Duration
            Text(
                text = "Duration: ~$durationStr",
                style = MaterialTheme.typography.bodySmall,
                color = HaulaufTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStart,
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
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start")
                }
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
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
