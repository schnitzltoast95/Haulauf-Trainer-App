package com.example.myapplication.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    onOpenSettings: () -> Unit,
) {
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
                .padding(top = 16.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
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
                    selectedMoveNames = selectedMoveNames
                )
                Spacer(modifier = Modifier.height(20.dp))
                ConfigureTrainingButton(onClick = onConfigureTraining)
            }
            Text(
                text = "Historical European Martial Arts Training",
                style = MaterialTheme.typography.bodySmall,
                color = HaulaufTextSecondary
            )
        }
    }
}

@Composable
private fun LogoSection() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(HaulaufGoldLight, HaulaufGoldDark)
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
                color = Color.White,
                start = Offset(centerX - halfLen, centerY + halfLen),
                end = Offset(centerX + halfLen, centerY - halfLen),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White,
                start = Offset(centerX - halfLen, centerY - halfLen),
                end = Offset(centerX + halfLen, centerY + halfLen),
                strokeWidth = strokeWidth
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Haulauf",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        ),
        color = Color.White
    )
    Text(
        text = "Trainer",
        style = MaterialTheme.typography.titleMedium,
        color = HaulaufGoldLight
    )
}

@Composable
private fun QuickstartButton(
    onClick: () -> Unit,
    selectedMoveNames: List<String>
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
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
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(HaulaufGoldLight, HaulaufGoldDark)
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
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quickstart",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
        if (selectedMoveNames.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Moves: ${selectedMoveNames.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = HaulaufTextSecondary
            )
        }
    }
}

@Composable
private fun CurrentSessionCard(
    preset: TrainingPreset,
    selectedMoveNames: List<String>
) {
    val durationStr = formatDuration(preset.estimatedDurationSeconds())

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HaulaufCard
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Session",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HaulaufTextSecondary
                )
                Text(
                    text = "~$durationStr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HaulaufTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SessionStat(value = preset.rounds.toString(), label = "Rounds")
                SessionStat(value = preset.unitsPerRound.toString(), label = "Calls/Round")
                SessionStat(value = preset.selectedMoveIds.size.toString(), label = "Moves")
            }
            if (selectedMoveNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Moves: ${selectedMoveNames.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HaulaufTextSecondary
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
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = HaulaufTextSecondary
        )
    }
}

@Composable
private fun ConfigureTrainingButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            containerColor = HaulaufCard
        ),
        border = null
    ) {
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Configure Training",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}m ${seconds}s"
}
