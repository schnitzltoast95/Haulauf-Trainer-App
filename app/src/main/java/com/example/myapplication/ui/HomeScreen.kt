package com.example.myapplication.ui

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = s.settings,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LogoSection()
                Spacer(modifier = Modifier.height(32.dp))
                QuickstartButton(
                    onClick = onQuickstart,
                    selectedMoveNames = selectedMoveNames
                )
                Spacer(modifier = Modifier.height(20.dp))
                CurrentSessionCard(
                    preset = preset,
                    selectedMoveNames = selectedMoveNames,
                    onClick = onEditCurrentSession
                )
                Spacer(modifier = Modifier.height(20.dp))
                ConfigureTrainingButton(onClick = onConfigureTraining)
                Spacer(modifier = Modifier.height(12.dp))
                LibraryButtonsRow(
                    onOpenPresets = onOpenPresets,
                    onOpenMoveDetails = onOpenMoveDetails
                )
            }
            Text(
                text = s.historicalSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = LocalStrings.current.appName,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        ),
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = LocalStrings.current.trainer,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
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
                    text = LocalStrings.current.quickstart,
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
    onClick: () -> Unit
) {
    val durationStr = formatDuration(preset.estimatedDurationSeconds())

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            WrappingControls {
                Text(
                    text = LocalStrings.current.currentSession,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "~$durationStr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            WrappingControls {
                SessionStat(value = preset.rounds.toString(), label = LocalStrings.current.rounds)
                SessionStat(value = preset.unitsPerRound.toString(), label = LocalStrings.current.callsRound)
                SessionStat(value = preset.selectedMoveIds.size.toString(), label = LocalStrings.current.moves)
            }
            Text((if (LocalStrings.current === StringsDe) "Start in " else "Starts in ") + "${preset.initialCountdownMs / 1000}s", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (selectedMoveNames.isEmpty()) Text(if (LocalStrings.current === StringsDe) "Übungen auswählen" else "Select exercises", color = MaterialTheme.colorScheme.error)
            if (selectedMoveNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${LocalStrings.current.moves}: ${selectedMoveNames.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SessionStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ConfigureTrainingButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = null
    ) {
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = LocalStrings.current.configureTraining,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}m ${seconds}s"
}
