package com.example.myapplication.ui

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Move
import com.example.myapplication.data.TrainingPreset
import com.example.myapplication.ui.theme.HaulaufCard
import com.example.myapplication.ui.theme.HaulaufGoldDark
import com.example.myapplication.ui.theme.HaulaufGoldLight
import com.example.myapplication.ui.theme.HaulaufTextSecondary

@Composable
fun HomeScreen(
    preset: TrainingPreset,
    allMoves: List<Move>,
    onQuickstart: () -> Unit,
    onConfigureTraining: () -> Unit,
    onEditCurrentSession: () -> Unit,
    onOpenMoveDetails: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPresets: () -> Unit,
) {
    val s = LocalStrings.current
    val selectedMoveNames = allMoves
        .filter { it.id in preset.selectedMoveIds }
        .map { it.displayName }
        .sorted()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val viewportHeight = (maxHeight - 56.dp).coerceAtLeast(0.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = viewportHeight)
                    .padding(horizontal = 24.dp).padding(vertical = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LogoSection()
                Spacer(Modifier.height(12.dp))
                Text(s.appName, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(s.trainer, style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(32.dp))
                CurrentSessionCard(
                    preset = preset,
                    selectedMoveNames = selectedMoveNames,
                    onClick = onEditCurrentSession,
                    onStart = onQuickstart
                )
                Spacer(Modifier.height(24.dp))
                LibraryButtonsRow(
                    onOpenPresets = onOpenPresets,
                    onOpenMoveDetails = onOpenMoveDetails
                )
                Text(s.historicalSubtitle,
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

            }
        }
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 16.dp)
        ) {
            Icon(Icons.Filled.Settings, contentDescription = s.settings)
        }

    }
}

@Composable
private fun LibraryButtonsRow(
    onOpenPresets: () -> Unit,
    onOpenMoveDetails: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val buttonWidth = if (maxWidth < 340.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f) maxWidth else (maxWidth - 12.dp) / 2
        WrappingControls {
        OutlinedButton(
            onClick = onOpenPresets,
            modifier = Modifier
                .width(buttonWidth)
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = null
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LocalStrings.current.trainingsShort,
                style = MaterialTheme.typography.titleMedium
            )
        }
        OutlinedButton(
            onClick = onOpenMoveDetails,
            modifier = Modifier
                .width(buttonWidth)
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = null
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LocalStrings.current.movesShort,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
    }
}

@Composable
private fun LogoSection() {
    val logoStroke = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, HaulaufGoldDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(48.dp)) {
            val strokeWidth = 4f
            val centerX = size.width / 2
            val centerY = size.height / 2
            val halfLen = size.minDimension / 3
            drawLine(
                color = logoStroke,
                start = Offset(centerX - halfLen, centerY + halfLen),
                end = Offset(centerX + halfLen, centerY - halfLen),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = logoStroke,
                start = Offset(centerX - halfLen, centerY - halfLen),
                end = Offset(centerX + halfLen, centerY + halfLen),
                strokeWidth = strokeWidth
            )
        }
    }

}

@Composable
private fun QuickstartButton(
    onClick: () -> Unit,
    selectedMoveNames: List<String>
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, HaulaufGoldDark)
                    ),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (LocalStrings.current === StringsDe) "Training starten" else "Start training",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun CurrentSessionCard(
    preset: TrainingPreset,
    selectedMoveNames: List<String>,
    onClick: () -> Unit,
    onStart: () -> Unit
) {
    val s = LocalStrings.current
    val de = s === StringsDe
    val seconds = preset.estimatedDurationSeconds()
    val duration = "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
    Column(Modifier.fillMaxWidth()) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (de) "Schnellstart" else "Quick start",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    if (maxWidth < 280.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.2f) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SessionStat(preset.rounds.toString(), s.rounds)
                            SessionStat(preset.unitsPerRound.toString(), s.callsRound)
                            SessionStat(duration, if (de) "Dauer (ca.)" else "Duration (approx.)")
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SessionStat(preset.rounds.toString(), s.rounds, Modifier.weight(1f))
                            SessionStat(preset.unitsPerRound.toString(), s.callsRound, Modifier.weight(1.5f))
                            SessionStat(duration, if (de) "Dauer (ca.)" else "Duration (approx.)", Modifier.weight(1.2f))
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(if (de) "Ausgewählte Übungen (${selectedMoveNames.size})"
                    else "Selected exercises (${selectedMoveNames.size})",
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (selectedMoveNames.isEmpty()) {
                    Text(if (de) "Übungen auswählen" else "Select exercises", color = MaterialTheme.colorScheme.error)
                } else {
                    Text(selectedMoveNames.joinToString(", "),
                        modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(24.dp))
                QuickstartButton(onStart, selectedMoveNames)
            }
        }
    }
}

@Composable
private fun SessionStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}m ${seconds}s"
}
