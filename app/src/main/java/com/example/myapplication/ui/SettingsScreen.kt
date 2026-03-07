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
    ttsSpeechRate: Float,
    onTtsSpeechRateChange: (Float) -> Unit,
    uiLanguage: String,
    onUiLanguageChange: (String) -> Unit,
    onPresetChange: (TrainingPreset) -> Unit,
    onOpenCustomAudio: () -> Unit,
    onAddMove: (String, MoveCategory) -> Unit,
    onRemoveMove: (String) -> Unit,
    onClose: () -> Unit
) {
    val s = LocalStrings.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.settings) },
                navigationIcon = {},
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = s.close,
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
                text = s.audio,
                style = MaterialTheme.typography.labelSmall,
                color = HaulaufTextSecondary
            )

            // Call volume
            SettingsAudioCard(
                icon = Icons.Filled.Settings,
                title = s.callVolume,
                subtitle = "${(preset.volumeCall * 100).toInt()}%",
                value = preset.volumeCall,
                onValueChange = { onPresetChange(preset.copy(volumeCall = it)) }
            )

            // End beep volume + toggle
            SettingsAudioCard(
                icon = Icons.Filled.Notifications,
                title = s.endBeep,
                subtitle = "${(preset.volumeBeep * 100).toInt()}%",
                value = preset.volumeBeep,
                onValueChange = { onPresetChange(preset.copy(volumeBeep = it)) },
                enabled = preset.endBeepEnabled,
                onToggleEnabled = { onPresetChange(preset.copy(endBeepEnabled = it)) }
            )

            // Metronome global audio only (per-training behavior is configured in Advanced settings)
            SettingsMetronomeCard(
                preset = preset,
                onPresetChange = onPresetChange
            )

            // TTS Speed
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardDefaults.shape,
                color = HaulaufCard
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = s.ttsSpeed,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = s.ttsSpeedDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = HaulaufTextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = ttsSpeechRate.coerceIn(0.5f, 2f),
                        onValueChange = { onTtsSpeechRateChange(it) },
                        valueRange = 0.5f..2f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = HaulaufGoldLight,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )
                    Text(
                        text = "%.2f".format(ttsSpeechRate),
                        style = MaterialTheme.typography.bodySmall,
                        color = HaulaufTextSecondary
                    )
                }
            }

            // UI Language
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardDefaults.shape,
                color = HaulaufCard
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = s.language,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { onUiLanguageChange("de") },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (uiLanguage == "de") HaulaufGoldLight else HaulaufTextSecondary
                            )
                        ) { Text(s.languageDe) }
                        TextButton(
                            onClick = { onUiLanguageChange("en") },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (uiLanguage == "en") HaulaufGoldLight else HaulaufTextSecondary
                            )
                        ) { Text(s.languageEn) }
                    }
                }
            }

            Text(
                text = s.manageMoves,
                style = MaterialTheme.typography.labelSmall,
                color = HaulaufTextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            SettingsAddMoveCard(onAddMove = onAddMove, strings = s)

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
                            Text(s.remove, color = HaulaufGoldLight)
                        }
                    }
                }
            }

            Text(
                text = s.moveDetails,
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
                        text = s.customizeMoveDetails,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = s.customizeMoveDetailsDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = HaulaufTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsAddMoveCard(
    onAddMove: (String, MoveCategory) -> Unit,
    strings: Strings
) {
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
                label = { Text(strings.newMoveName, color = HaulaufTextSecondary) },
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
                Text(strings.category + ":", style = MaterialTheme.typography.bodySmall, color = HaulaufTextSecondary)
                TextButton(
                    onClick = { category = MoveCategory.HAU },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (category == MoveCategory.HAU) HaulaufGoldLight else HaulaufTextSecondary
                    )
                ) { Text(strings.hau) }
                TextButton(
                    onClick = { category = MoveCategory.HUT },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (category == MoveCategory.HUT) HaulaufGoldLight else HaulaufTextSecondary
                    )
                ) { Text(strings.hut) }
                TextButton(
                    onClick = { category = MoveCategory.STICH },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (category == MoveCategory.STICH) HaulaufGoldLight else HaulaufTextSecondary
                    )
                ) { Text(strings.stich) }
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
                Text(strings.addMove, color = HaulaufGoldLight)
            }
        }
    }
}

@Composable
private fun SettingsMetronomeCard(
    preset: TrainingPreset,
    onPresetChange: (TrainingPreset) -> Unit
) {
    val s = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardDefaults.shape,
        color = HaulaufCard
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
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
                        text = s.metronome,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(s.tickVolume, style = MaterialTheme.typography.bodySmall, color = HaulaufTextSecondary)
            Slider(
                value = preset.volumeTick.coerceIn(0f, 1f),
                onValueChange = { onPresetChange(preset.copy(volumeTick = it.coerceIn(0f, 1f))) },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = HaulaufGoldLight,
                    inactiveTrackColor = Color.DarkGray
                )
            )
            Text("${(preset.volumeTick * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = HaulaufTextSecondary)
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


