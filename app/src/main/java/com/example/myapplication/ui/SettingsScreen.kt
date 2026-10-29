package com.example.myapplication.ui

import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice

import androidx.compose.ui.semantics.*
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.TrainingPreset
import com.example.myapplication.ui.theme.HaulaufCard
import com.example.myapplication.ui.theme.HaulaufGoldDark
import com.example.myapplication.ui.theme.HaulaufGoldLight
import com.example.myapplication.ui.theme.HaulaufTextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

private const val JONAS_VOICE_NAME = "de-de-x-deb-local"

private fun voiceLabel(name: String, german: Boolean): String = when (name) {
    JONAS_VOICE_NAME -> if (german) "Jonas · männlich" else "Jonas · male"
    else -> name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preset: TrainingPreset,
    ttsSpeechRate: Float,
    onTtsSpeechRateChange: (Float) -> Unit,
    ttsVoiceName: String?,
    onTtsVoiceChange: (String?) -> Unit,
    uiLanguage: String,
    onUiLanguageChange: (String) -> Unit,
    onPresetChange: (TrainingPreset) -> Unit,
    onClose: () -> Unit,
    legacyAudio: Boolean = false,
    onUseLegacyAudio: () -> Unit = {}
) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val german = s === StringsDe
    var availableVoices by remember { mutableStateOf<List<Voice>>(emptyList()) }
    var voicePickerOpen by remember { mutableStateOf(false) }
    var previewTts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val engine = TextToSpeech(context) { status ->
            Handler(Looper.getMainLooper()).post {
                if (status == TextToSpeech.SUCCESS) {
                    availableVoices = previewTts?.voices
                        ?.filter { it.name == JONAS_VOICE_NAME && !it.isNetworkConnectionRequired }
                        .orEmpty()
                }
            }
        }
        previewTts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            previewTts = null
        }
    }
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
                            tint = MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(if (legacyAudio) {
                if (german) "Bisherige Lautstärken bleiben je Training erhalten. Eine Änderung hier setzt gemeinsame Pegel für alle Trainings." else "Existing per-training levels are preserved. Changing a slider sets shared levels for all trainings."
            } else {
                if (german) "Diese Lautstärken gelten für alle Trainings." else "These levels apply to all trainings."
            }, style = MaterialTheme.typography.bodySmall)
            if (!legacyAudio) TextButton(onClick = onUseLegacyAudio) {
                Text(if (german) "Gespeicherte Trainingspegel verwenden" else "Use saved training levels")
            }
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
                enabled = true
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
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        if (german) "Ansagestimme" else "Announcement voice",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (german) "Wähle zwischen der bisherigen Standardstimme und Jonas. Importierte Aufnahmen behalten ihre eigene Stimme."
                        else "Choose the original system voice or Jonas. Imported recordings keep their own voice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        ttsVoiceName?.let { voiceLabel(it, german) } ?: if (german) "Standardstimme" else "Default voice",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { voicePickerOpen = true }) {
                        Text(if (german) "Stimme auswählen / anhören" else "Choose / preview voice")
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardDefaults.shape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = s.ttsSpeed,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = s.ttsSpeedDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        modifier = Modifier.semantics { contentDescription = s.ttsSpeed; stateDescription = "%.2f".format(ttsSpeechRate) },
                        value = ttsSpeechRate.coerceIn(0.5f, 2f),
                        onValueChange = { onTtsSpeechRateChange(it) },
                        valueRange = 0.5f..2f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.onSurface,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Text(
                        text = "%.2f".format(ttsSpeechRate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // UI Language
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardDefaults.shape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = s.language,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { onUiLanguageChange("de") },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (uiLanguage == "de") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) { Text(s.languageDe) }
                        TextButton(
                            onClick = { onUiLanguageChange("en") },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (uiLanguage == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) { Text(s.languageEn) }
                    }
                }
            }

        }
    }

    if (voicePickerOpen) {
        AlertDialog(
            onDismissRequest = { voicePickerOpen = false },
            title = { Text(if (german) "Deutsche Stimmen" else "German voices") },
            text = {
                Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        if (german) "Jonas ist der App-Name der auf diesem Gerät geprüften männlichen Stimme. Hör die Probe vor der Auswahl an."
                        else "Jonas is the app name for the male voice tested on this device. Preview it before choosing.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { onTtsVoiceChange(null); voicePickerOpen = false }, modifier = Modifier.weight(1f)) {
                            Text(if (german) "Standardstimme" else "Default voice")
                        }
                        TextButton(onClick = {
                            previewTts?.apply {
                                language = Locale.GERMAN
                                setSpeechRate(ttsSpeechRate)
                                speak("Oberhau. Unterhau. Ochs.", TextToSpeech.QUEUE_FLUSH, null, "voice_preview_default")
                            }
                        }) {
                            Text(if (german) "Probe" else "Preview")
                        }
                    }
                    if (availableVoices.isEmpty()) {
                        Text(if (german) "Jonas ist auf diesem Gerät nicht verfügbar." else "Jonas is not available on this device.")
                    }
                    availableVoices.forEach { voice ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { onTtsVoiceChange(voice.name); voicePickerOpen = false }, modifier = Modifier.weight(1f)) {
                                Text(voiceLabel(voice.name, german))
                            }
                            TextButton(onClick = {
                                previewTts?.apply {
                                    this.voice = voice
                                    setSpeechRate(ttsSpeechRate)
                                    speak("Oberhau. Unterhau. Ochs.", TextToSpeech.QUEUE_FLUSH, null, "voice_preview")
                                }
                            }) {
                                Text(if (german) "Probe" else "Preview")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { voicePickerOpen = false }) {
                    Text(if (german) "Schließen" else "Close")
                }
            }
        )
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
        color = MaterialTheme.colorScheme.surfaceVariant
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = s.metronome,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(s.tickVolume, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(
                modifier = Modifier.semantics { contentDescription = s.tickVolume; stateDescription = "${(preset.volumeTick * 100).toInt()}%" },
                        value = preset.volumeTick.coerceIn(0f, 1f),
                onValueChange = { onPresetChange(preset.copy(volumeTick = it.coerceIn(0f, 1f))) },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onSurface,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Text("${(preset.volumeTick * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        color = MaterialTheme.colorScheme.surfaceVariant
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (onToggleEnabled != null) {
                    Switch(
                        modifier = Modifier.semantics { contentDescription = title },
                        checked = enabled,
                        onCheckedChange = onToggleEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Column {
                Slider(
                    modifier = Modifier.semantics { contentDescription = title; stateDescription = subtitle },
                        value = value.coerceIn(0f, 1f),
                    onValueChange = { onValueChange(it.coerceIn(0f, 1f)) },
                    enabled = enabled,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


