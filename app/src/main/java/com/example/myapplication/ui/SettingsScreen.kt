package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.MoveCategory
import com.example.myapplication.data.TrainingPreset
import com.example.myapplication.ui.theme.HaulaufCard
import com.example.myapplication.ui.theme.HaulaufGoldDark
import com.example.myapplication.ui.theme.HaulaufGoldLight
import com.example.myapplication.ui.theme.HaulaufTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preset: TrainingPreset,
    customMoves: List<com.example.myapplication.data.Move>,
    onPresetChange: (TrainingPreset) -> Unit,
    onOpenCustomAudio: () -> Unit,
    onAddMove: (String, MoveCategory) -> Unit,
    onRemoveMove: (String) -> Unit,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {},
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "AUDIO",
                style = MaterialTheme.typography.labelSmall,
                color = HaulaufTextSecondary
            )

            // Call volume
            SettingsAudioCard(
                icon = Icons.Filled.Settings,
                title = "Call Volume",
                subtitle = "${(preset.volumeCall * 100).toInt()}%",
                value = preset.volumeCall,
                onValueChange = { onPresetChange(preset.copy(volumeCall = it)) }
            )

            // End beep volume + toggle
            SettingsAudioCard(
                icon = Icons.Filled.Notifications,
                title = "End-of-Window Beep",
                subtitle = "${(preset.volumeBeep * 100).toInt()}%",
                value = preset.volumeBeep,
                onValueChange = { onPresetChange(preset.copy(volumeBeep = it)) },
                enabled = preset.endBeepEnabled,
                onToggleEnabled = { onPresetChange(preset.copy(endBeepEnabled = it)) }
            )

            // Metronome toggle + tick volume + beat interval
            SettingsMetronomeCard(
                preset = preset,
                onPresetChange = onPresetChange
            )

            Text(
                text = "TRAINING BEHAVIOR",
                style = MaterialTheme.typography.labelSmall,
                color = HaulaufTextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            BehaviorCard(
                title = "No Immediate Repetition",
                description = "Prevents the same move from being called twice in a row",
                checked = preset.noImmediateRepetition,
                onCheckedChange = { onPresetChange(preset.copy(noImmediateRepetition = it)) }
            )

            Text(
                text = "MANAGE MOVES",
                style = MaterialTheme.typography.labelSmall,
                color = HaulaufTextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            SettingsAddMoveCard(onAddMove = onAddMove)

            customMoves.forEach { move ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardDefaults.shape,
                    color = HaulaufCard
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = move.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        TextButton(
                            onClick = { onRemoveMove(move.id) }
                        ) {
                            Text("Remove", color = HaulaufGoldLight)
                        }
                    }
                }
            }

            Text(
                text = "MOVE DETAILS",
                style = MaterialTheme.typography.labelSmall,
                color = HaulaufTextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenCustomAudio),
                shape = CardDefaults.shape,
                color = HaulaufCard,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Customize Move Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Edit audio, images, and descriptions for each move",
                        style = MaterialTheme.typography.bodySmall,
                        color = HaulaufTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsAddMoveCard(onAddMove: (String, MoveCategory) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MoveCategory.HAU) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardDefaults.shape,
        color = HaulaufCard
    ) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("New move name", color = HaulaufTextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = HaulaufGoldLight,
                    unfocusedBorderColor = HaulaufTextSecondary,
                    cursorColor = HaulaufGoldLight,
                    focusedLabelColor = HaulaufGoldLight,
                    unfocusedLabelColor = HaulaufTextSecondary
                )
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Kategorie:", style = MaterialTheme.typography.bodySmall, color = HaulaufTextSecondary)
                TextButton(
                    onClick = { category = MoveCategory.HAU },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (category == MoveCategory.HAU) HaulaufGoldLight else HaulaufTextSecondary
                    )
                ) { Text("Hau") }
                TextButton(
                    onClick = { category = MoveCategory.HUT },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (category == MoveCategory.HUT) HaulaufGoldLight else HaulaufTextSecondary
                    )
                ) { Text("Hut") }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isNotEmpty()) {
                        onAddMove(trimmed, category)
                        name = ""
                    }
                }
            ) {
                Text("Add Move", color = HaulaufGoldLight)
            }
        }
    }
}

@Composable
private fun SettingsMetronomeCard(
    preset: TrainingPreset,
    onPresetChange: (TrainingPreset) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardDefaults.shape,
        color = HaulaufCard
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = HaulaufGoldLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Metronome",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                Switch(
                    checked = preset.metronomeEnabled,
                    onCheckedChange = { onPresetChange(preset.copy(metronomeEnabled = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = HaulaufGoldLight
                    )
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("Tick Volume", style = MaterialTheme.typography.bodySmall, color = HaulaufTextSecondary)
            Slider(
                value = preset.volumeTick.coerceIn(0f, 1f),
                onValueChange = { onPresetChange(preset.copy(volumeTick = it.coerceIn(0f, 1f))) },
                enabled = preset.metronomeEnabled,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = HaulaufGoldLight,
                    inactiveTrackColor = Color.DarkGray
                )
            )
            Text("${(preset.volumeTick * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = HaulaufTextSecondary)
            Spacer(Modifier.height(8.dp))
            Text("Beat Interval", style = MaterialTheme.typography.bodySmall, color = HaulaufTextSecondary)
            val intervalSec = (preset.metronomeBeatIntervalMs / 1000f).coerceIn(0.2f, 2f)
            Slider(
                value = intervalSec,
                onValueChange = { onPresetChange(preset.copy(metronomeBeatIntervalMs = (it * 1000).toLong())) },
                enabled = preset.metronomeEnabled,
                valueRange = 0.2f..2f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = HaulaufGoldLight,
                    inactiveTrackColor = Color.DarkGray
                )
            )
            Text("%.1fs per beat".format(intervalSec), style = MaterialTheme.typography.bodySmall, color = HaulaufTextSecondary)
        }
    }
}

@Composable
private fun SettingsAudioCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean = true,
    onToggleEnabled: ((Boolean) -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardDefaults.shape,
        color = HaulaufCard
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = HaulaufGoldLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                if (onToggleEnabled != null) {
                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggleEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HaulaufGoldLight
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Column {
                Slider(
                    value = value.coerceIn(0f, 1f),
                    onValueChange = { onValueChange(it.coerceIn(0f, 1f)) },
                    enabled = enabled,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = HaulaufGoldLight,
                        inactiveTrackColor = Color.DarkGray
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = HaulaufTextSecondary
                )
            }
        }
    }
}

@Composable
private fun BehaviorCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardDefaults.shape,
        color = HaulaufCard
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = HaulaufTextSecondary
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = HaulaufGoldLight
                )
            )
        }
    }
}

