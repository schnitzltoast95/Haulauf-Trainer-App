package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Move
import com.example.myapplication.data.MoveCategory
import com.example.myapplication.data.TrainingPreset
import com.example.myapplication.ui.theme.HaulaufCard
import com.example.myapplication.ui.theme.HaulaufGoldDark
import com.example.myapplication.ui.theme.HaulaufGoldLight
import com.example.myapplication.ui.theme.HaulaufTextSecondary
import androidx.compose.foundation.layout.width

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingSetupScreen(
    preset: TrainingPreset,
    allMoves: List<Move>,
    onPresetChange: (TrainingPreset) -> Unit,
    onStart: () -> Unit,
    onSavePreset: (String) -> Unit,
    onBack: () -> Unit
) {
    var showMoveInfoDialog by remember { mutableStateOf<Move?>(null) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }
    var selectedCategoryTab by remember { mutableStateOf(0) } // 0 = Alle, 1 = Haue, 2 = Huten
    var rounds by remember(preset.rounds) { mutableStateOf(preset.rounds) }
    var unitsPerRound by remember(preset.unitsPerRound) { mutableStateOf(preset.unitsPerRound) }
    var fixedInterval by remember(preset.reactionIntervalMs) { mutableStateOf(preset.reactionIntervalMinMs == null) }
    var intervalSec by remember(preset.reactionIntervalMs) { mutableStateOf(preset.reactionIntervalMs / 1000f) }
    var intervalMinSec by remember(preset.reactionIntervalMinMs ?: 5f) { mutableStateOf((preset.reactionIntervalMinMs ?: 5000) / 1000f) }
    var intervalMaxSec by remember(preset.reactionIntervalMaxMs ?: 7f) { mutableStateOf((preset.reactionIntervalMaxMs ?: 7000) / 1000f) }
    var pauseSec by remember(preset.pauseBetweenRoundsMs) { mutableStateOf(preset.pauseBetweenRoundsMs / 1000f) }
    var selectedIds by remember(preset.selectedMoveIds) { mutableStateOf(preset.selectedMoveIds) }
    var showAdvanced by remember { mutableStateOf(false) }
    var endBeep by remember(preset.endBeepEnabled) { mutableStateOf(preset.endBeepEnabled) }
    var metronome by remember(preset.metronomeEnabled) { mutableStateOf(preset.metronomeEnabled) }
    var metronomeInterval by remember(preset.metronomeBeatIntervalMs) { mutableStateOf(preset.metronomeBeatIntervalMs / 1000f) }
    var noRep by remember(preset.noImmediateRepetition) { mutableStateOf(preset.noImmediateRepetition) }

    LaunchedEffect(
        rounds,
        unitsPerRound,
        fixedInterval,
        intervalSec,
        intervalMinSec,
        intervalMaxSec,
        pauseSec,
        selectedIds,
        endBeep,
        metronome,
        metronomeInterval,
        noRep
    ) {
        onPresetChange(
            TrainingPreset(
                rounds = rounds,
                unitsPerRound = unitsPerRound,
                reactionIntervalMs = (intervalSec * 1000).toLong(),
                reactionIntervalMinMs = if (!fixedInterval) (intervalMinSec * 1000).toLong() else null,
                reactionIntervalMaxMs = if (!fixedInterval) (intervalMaxSec * 1000).toLong() else null,
                pauseBetweenRoundsMs = (pauseSec * 1000).toLong(),
                selectedMoveIds = selectedIds,
                endBeepEnabled = endBeep,
                metronomeEnabled = metronome,
                metronomeBeatIntervalMs = (metronomeInterval * 1000).toLong(),
                noImmediateRepetition = noRep,
                volumeCall = preset.volumeCall,
                volumeBeep = preset.volumeBeep,
                volumeTick = preset.volumeTick,
            )
        )
    }

    val scrollState = rememberScrollState()
    val estimatedSeconds = preset.estimatedDurationSeconds()
    val durationMinutes = estimatedSeconds / 60
    val durationSeconds = estimatedSeconds % 60

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Training") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estimated duration card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardDefaults.shape,
                color = HaulaufGoldDark.copy(alpha = 0.25f),
                tonalElevation = 0.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(HaulaufGoldLight.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⏱",
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Estimated Duration",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${durationMinutes}m ${durationSeconds}s",
                            style = MaterialTheme.typography.titleMedium,
                            color = HaulaufGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${rounds * unitsPerRound} total calls across $rounds rounds",
                        style = MaterialTheme.typography.bodySmall,
                        color = HaulaufTextSecondary
                    )
                }
            }

            // Rounds
            StepperCard(
                title = "Rounds",
                value = rounds,
                onChange = { v -> rounds = v.coerceAtLeast(1) }
            )

            // Calls per round
            StepperCard(
                title = "Calls per Round",
                value = unitsPerRound,
                onChange = { v -> unitsPerRound = v.coerceAtLeast(1) }
            )

            // Reaction interval
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardDefaults.shape,
                color = HaulaufCard
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reaction Interval",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Random",
                                style = MaterialTheme.typography.bodySmall,
                                color = HaulaufTextSecondary
                            )
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = !fixedInterval,
                                onCheckedChange = { checked -> fixedInterval = !checked },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = HaulaufGoldLight,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.DarkGray
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    if (fixedInterval) {
                        Text(
                            text = "Seconds",
                            style = MaterialTheme.typography.bodySmall,
                            color = HaulaufTextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        StepperInline(
                            value = intervalSec.toInt(),
                            onChange = { v -> intervalSec = v.coerceAtLeast(1).toFloat() }
                        )
                    } else {
                        Text(
                            text = "Min / Max (seconds)",
                            style = MaterialTheme.typography.bodySmall,
                            color = HaulaufTextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StepperInline(
                                value = intervalMinSec.toInt(),
                                onChange = { v ->
                                    val n = v.coerceAtLeast(1)
                                    intervalMinSec = n.toFloat()
                                    if (intervalMaxSec < intervalMinSec) {
                                        intervalMaxSec = intervalMinSec
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            StepperInline(
                                value = intervalMaxSec.toInt(),
                                onChange = { v ->
                                    val n = v.coerceAtLeast(intervalMinSec.toInt())
                                    intervalMaxSec = n.toFloat()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Pause between rounds
            StepperCard(
                title = "Pause Between Rounds (sec)",
                value = pauseSec.toInt(),
                onChange = { v -> pauseSec = v.coerceAtLeast(0).toFloat() }
            )

            // Select moves
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardDefaults.shape,
                color = HaulaufCard
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Moves",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    // Tabs for categories
                    TabRow(selectedTabIndex = selectedCategoryTab) {
                        Tab(
                            selected = selectedCategoryTab == 0,
                            onClick = { selectedCategoryTab = 0 },
                            text = { Text("Alle") }
                        )
                        Tab(
                            selected = selectedCategoryTab == 1,
                            onClick = { selectedCategoryTab = 1 },
                            text = { Text("Haue") }
                        )
                        Tab(
                            selected = selectedCategoryTab == 2,
                            onClick = { selectedCategoryTab = 2 },
                            text = { Text("Huten") }
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Filter moves based on selected tab
                    val movesByCategory = allMoves.groupBy { it.category }
                    val movesToShow = when (selectedCategoryTab) {
                        1 -> movesByCategory[MoveCategory.HAU] ?: emptyList()
                        2 -> movesByCategory[MoveCategory.HUT] ?: emptyList()
                        else -> allMoves
                    }
                    
                    movesToShow.forEach { move ->
                                val selected = move.id in selectedIds
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .clickable {
                                            selectedIds = if (selected) selectedIds - move.id else selectedIds + move.id
                                        },
                                    color = if (selected) HaulaufGoldDark.copy(alpha = 0.7f) else Color.Transparent
                                ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = move.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selected) HaulaufGoldLight else Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { showMoveInfoDialog = move },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = "Move Info",
                                            tint = HaulaufTextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = HaulaufGoldLight,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    
                    // Selected count at the bottom
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${selectedIds.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = HaulaufTextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }

            // Advanced settings kept minimal / collapsed toggle (optional)
            if (showAdvanced) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardDefaults.shape,
                    color = HaulaufCard
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Advanced Settings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = noRep,
                                onCheckedChange = { noRep = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = HaulaufGoldLight
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "No immediate repetition",
                                style = MaterialTheme.typography.bodySmall,
                                color = HaulaufTextSecondary
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = metronome,
                                onCheckedChange = { metronome = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = HaulaufGoldLight
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Metronome",
                                style = MaterialTheme.typography.bodySmall,
                                color = HaulaufTextSecondary
                            )
                        }
                        if (metronome) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Metronome interval (sec)",
                                style = MaterialTheme.typography.bodySmall,
                                color = HaulaufTextSecondary
                            )
                            Spacer(Modifier.height(4.dp))
                            StepperInline(
                                value = metronomeInterval.toInt(),
                                onChange = { v -> metronomeInterval = v.coerceAtLeast(1).toFloat() }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = endBeep,
                                onCheckedChange = { endBeep = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = HaulaufGoldLight
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "End-of-window beep",
                                style = MaterialTheme.typography.bodySmall,
                                color = HaulaufTextSecondary
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Advanced settings",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { showAdvanced = true }
                        .padding(top = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = HaulaufTextSecondary
                )
            }

            Spacer(Modifier.height(8.dp))

            // Start Training and Save Preset buttons side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Training button
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
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
                                shape = MaterialTheme.shapes.large
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Start Training",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }

                // Save Preset button
                OutlinedButton(
                    onClick = { showSavePresetDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Save Preset",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save Preset") },
            text = {
                Column {
                    Text("Enter a name for this preset:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        label = { Text("Preset Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (presetName.isNotBlank()) {
                            onSavePreset(presetName.trim())
                            presetName = ""
                            showSavePresetDialog = false
                        }
                    },
                    enabled = presetName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSavePresetDialog = false
                    presetName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Move Info Dialog
    showMoveInfoDialog?.let { move ->
        MoveInfoDialog(
            move = move,
            onDismiss = { showMoveInfoDialog = null }
        )
    }
}
