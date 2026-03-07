package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Move
import com.example.myapplication.data.MoveCategory
import com.example.myapplication.data.TrainingPreset
import com.example.myapplication.ui.theme.HaulaufCard
import com.example.myapplication.ui.theme.HaulaufGold
import com.example.myapplication.ui.theme.HaulaufGoldDark
import com.example.myapplication.ui.theme.HaulaufGoldLight
import com.example.myapplication.ui.theme.HaulaufTextSecondary
import androidx.compose.foundation.layout.width

private const val MIN_MOVE_PRIORITY = 1
private const val MAX_MOVE_PRIORITY = 3

private fun normalizeMovePriority(priority: Int): Int = priority.coerceIn(MIN_MOVE_PRIORITY, MAX_MOVE_PRIORITY)
private fun nextMovePriority(priority: Int): Int =
    if (normalizeMovePriority(priority) >= MAX_MOVE_PRIORITY) MIN_MOVE_PRIORITY else normalizeMovePriority(priority) + 1

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TrainingSetupScreen(
    preset: TrainingPreset,
    allMoves: List<Move>,
    onPresetChange: (TrainingPreset) -> Unit,
    onStart: () -> Unit,
    onSavePreset: (String) -> Unit,
    onBack: () -> Unit
) {
    val s = LocalStrings.current
    var showMoveInfoDialog by remember { mutableStateOf<Move?>(null) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }
    var selectedCategoryTab by remember { mutableStateOf(0) } // 0 = Alle, 1 = Haue, 2 = Huten, 3 = Stiche
    var rounds by remember(preset.rounds) { mutableStateOf(preset.rounds) }
    var unitsPerRound by remember(preset.unitsPerRound) { mutableStateOf(preset.unitsPerRound) }
    var fixedInterval by remember(preset.reactionIntervalMs) { mutableStateOf(preset.reactionIntervalMinMs == null) }
    var intervalSec by remember(preset.reactionIntervalMs) { mutableStateOf(preset.reactionIntervalMs / 1000f) }
    var intervalMinSec by remember(preset.reactionIntervalMinMs ?: 5f) { mutableStateOf((preset.reactionIntervalMinMs ?: 5000) / 1000f) }
    var intervalMaxSec by remember(preset.reactionIntervalMaxMs ?: 7f) { mutableStateOf((preset.reactionIntervalMaxMs ?: 7000) / 1000f) }
    var pauseSec by remember(preset.pauseBetweenRoundsMs) { mutableStateOf(preset.pauseBetweenRoundsMs / 1000f) }
    var selectedIds by remember(preset.selectedMoveIds) { mutableStateOf(preset.selectedMoveIds) }
    var movePriorities by remember(preset.movePriorities) {
        mutableStateOf(
            preset.movePriorities.mapValues { (_, priority) -> normalizeMovePriority(priority) }
        )
    }
    var showAdvanced by remember { mutableStateOf(false) }
    var endBeep by remember(preset.endBeepEnabled) { mutableStateOf(preset.endBeepEnabled) }
    var metronome by remember(preset.metronomeEnabled) { mutableStateOf(preset.metronomeEnabled) }
    var metronomeBeats by remember(preset.metronomeBeatIntervalMs) {
        mutableStateOf(preset.metronomeBeatIntervalMs.coerceIn(1L, 32L).toInt())
    }
    var noRep by remember(preset.noImmediateRepetition) { mutableStateOf(preset.noImmediateRepetition) }
    var initialCountdownSec by remember(preset.initialCountdownMs) { mutableStateOf((preset.initialCountdownMs / 1000).toInt()) }

    LaunchedEffect(
        rounds,
        unitsPerRound,
        fixedInterval,
        intervalSec,
        intervalMinSec,
        intervalMaxSec,
        pauseSec,
        selectedIds,
        movePriorities,
        endBeep,
        metronome,
        metronomeBeats,
        noRep,
        initialCountdownSec
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
                movePriorities = movePriorities
                    .filterKeys { it in selectedIds }
                    .mapValues { (_, priority) -> normalizeMovePriority(priority) },
                endBeepEnabled = endBeep,
                metronomeEnabled = metronome,
                metronomeBeatIntervalMs = metronomeBeats.toLong(),
                noImmediateRepetition = noRep,
                initialCountdownMs = (initialCountdownSec.coerceIn(1, 30) * 1000L),
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
                title = { Text(s.configureTrainingTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = s.back)
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
                                text = s.estimatedDuration,
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
                        text = s.totalCallsAcrossRoundsFormat.format(rounds * unitsPerRound, rounds),
                        style = MaterialTheme.typography.bodySmall,
                        color = HaulaufTextSecondary
                    )
                }
            }

            // Rounds
            StepperCard(
                title = s.rounds,
                value = rounds,
                onChange = { v -> rounds = v.coerceAtLeast(1) }
            )

            // Calls per round
            StepperCard(
                title = s.callsRound,
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
                            text = s.reactionInterval,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = s.random,
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
                            text = s.secondsRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = HaulaufTextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Slider(
                                value = intervalSec.coerceIn(0.05f, 5f),
                                onValueChange = { v ->
                                    intervalSec = (v * 20).toInt() / 20f
                                },
                                valueRange = 0.05f..5f,
                                steps = ((5f - 0.05f) / 0.05f).toInt() - 1,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = HaulaufGoldLight,
                                    inactiveTrackColor = Color.DarkGray
                                )
                            )
                            Text(
                                text = "%.2fs".format(intervalSec),
                                style = MaterialTheme.typography.bodyMedium,
                                color = HaulaufGoldLight,
                                modifier = Modifier.width(48.dp)
                            )
                        }
                    } else {
                        Text(
                            text = s.minMaxSecondsRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = HaulaufTextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Slider(
                                    value = intervalMinSec.coerceIn(0.05f, 5f),
                                    onValueChange = { v ->
                                        val n = (v * 20).toInt() / 20f
                                        intervalMinSec = n
                                        if (intervalMaxSec < intervalMinSec) intervalMaxSec = intervalMinSec
                                    },
                                    valueRange = 0.05f..5f,
                                    steps = ((5f - 0.05f) / 0.05f).toInt() - 1,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = HaulaufGoldLight,
                                        inactiveTrackColor = Color.DarkGray
                                    )
                                )
                                Text("%.2fs".format(intervalMinSec), style = MaterialTheme.typography.bodySmall, color = HaulaufTextSecondary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Slider(
                                    value = intervalMaxSec.coerceIn(0.05f, 5f),
                                    onValueChange = { v ->
                                        intervalMaxSec = ((v * 20).toInt() / 20f).coerceAtLeast(intervalMinSec)
                                    },
                                    valueRange = 0.05f..5f,
                                    steps = ((5f - 0.05f) / 0.05f).toInt() - 1,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = HaulaufGoldLight,
                                        inactiveTrackColor = Color.DarkGray
                                    )
                                )
                                Text("%.2fs".format(intervalMaxSec), style = MaterialTheme.typography.bodySmall, color = HaulaufTextSecondary)
                            }
                        }
                    }
                }
            }

            // Pause between rounds
            StepperCard(
                title = s.pauseBetweenRounds,
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
                        text = s.selectMoves,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    // Tabs for categories
                    TabRow(selectedTabIndex = selectedCategoryTab) {
                        Tab(
                            selected = selectedCategoryTab == 0,
                            onClick = { selectedCategoryTab = 0 },
                            text = { Text(s.all) }
                        )
                        Tab(
                            selected = selectedCategoryTab == 1,
                            onClick = { selectedCategoryTab = 1 },
                            text = { Text(s.haue) }
                        )
                        Tab(
                            selected = selectedCategoryTab == 2,
                            onClick = { selectedCategoryTab = 2 },
                            text = { Text(s.huten) }
                        )
                        Tab(
                            selected = selectedCategoryTab == 3,
                            onClick = { selectedCategoryTab = 3 },
                            text = { Text(s.stiche) }
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Filter moves based on selected tab
                    val movesByCategory = allMoves.groupBy { it.category }
                    val movesToShow = when (selectedCategoryTab) {
                        1 -> movesByCategory[MoveCategory.HAU] ?: emptyList()
                        2 -> movesByCategory[MoveCategory.HUT] ?: emptyList()
                        3 -> movesByCategory[MoveCategory.STICH] ?: emptyList()
                        else -> allMoves
                    }
                    
                    movesToShow.forEach { move ->
                                val selected = move.id in selectedIds
                                val priority = normalizeMovePriority(movePriorities[move.id] ?: MIN_MOVE_PRIORITY)
                                val increasePriority: () -> Unit = {
                                    if (!selected) {
                                        selectedIds = selectedIds + move.id
                                        movePriorities = movePriorities + (move.id to MIN_MOVE_PRIORITY)
                                    } else {
                                        val newPriority = nextMovePriority(priority)
                                        movePriorities = movePriorities + (move.id to newPriority)
                                    }
                                }
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .combinedClickable(
                                            onClick = {
                                            if (selected) {
                                                selectedIds = selectedIds - move.id
                                                movePriorities = movePriorities - move.id
                                            } else {
                                                selectedIds = selectedIds + move.id
                                                movePriorities = movePriorities + (move.id to MIN_MOVE_PRIORITY)
                                            }
                                        },
                                            onDoubleClick = increasePriority,
                                            onLongClick = increasePriority
                                        ),
                                    color = Color.White.copy(alpha = 0.02f)
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
                                    color = Color.White.copy(alpha = 0.95f),
                                    modifier = Modifier.weight(1f)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (selected) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (priority > MIN_MOVE_PRIORITY) {
                                                Text(
                                                    text = if (priority == 2) s.priorityHigh else s.priorityHighest,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = HaulaufTextSecondary
                                                )
                                            }
                                            IconButton(
                                                onClick = increasePriority,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                PriorityHeartIcon(
                                                    priority = priority,
                                                    contentDescription = s.priority,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = { showMoveInfoDialog = move },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = s.moveInfo,
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
                        text = "${selectedIds.size} ${s.selected}",
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
                            text = s.advancedSettings,
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
                                text = s.noImmediateRep,
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
                                text = s.metronome,
                                style = MaterialTheme.typography.bodySmall,
                                color = HaulaufTextSecondary
                            )
                        }
                        if (metronome) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = s.beatInterval,
                                style = MaterialTheme.typography.bodySmall,
                                color = HaulaufTextSecondary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = s.beatIntervalHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = HaulaufTextSecondary
                            )
                            Spacer(Modifier.height(4.dp))
                            StepperInline(
                                value = metronomeBeats,
                                onChange = { v -> metronomeBeats = v.coerceIn(1, 32) }
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
                                text = s.endBeep,
                                style = MaterialTheme.typography.bodySmall,
                                color = HaulaufTextSecondary
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${s.countdown} (${s.seconds}, 1-30)",
                            style = MaterialTheme.typography.bodySmall,
                            color = HaulaufTextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        StepperInline(
                            value = initialCountdownSec.coerceIn(1, 30),
                            onChange = { v -> initialCountdownSec = v.coerceIn(1, 30) }
                        )
                    }
                }
            } else {
                Text(
                    text = s.advancedSettings,
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
                            text = s.startTraining,
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
                        text = s.savePreset,
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
            title = { Text(s.savePresetTitle) },
            text = {
                Column {
                    Text(s.enterPresetName)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        label = { Text(s.presetName) },
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
                    Text(s.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSavePresetDialog = false
                    presetName = ""
                }) {
                    Text(s.cancel)
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

@Composable
private fun PriorityHeartIcon(
    priority: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    when (priority) {
        1 -> {
            Icon(
                imageVector = Icons.Filled.FavoriteBorder,
                contentDescription = contentDescription,
                tint = HaulaufTextSecondary,
                modifier = modifier
            )
        }
        2 -> {
            Box(modifier = modifier) {
                Icon(
                    imageVector = Icons.Filled.FavoriteBorder,
                    contentDescription = contentDescription,
                    tint = HaulaufGold,
                    modifier = Modifier.matchParentSize()
                )
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = HaulaufGold,
                    modifier = Modifier
                        .matchParentSize()
                        .drawWithContent {
                            clipRect(right = size.width / 2f) {
                                this@drawWithContent.drawContent()
                            }
                        }
                )
            }
        }
        else -> {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = contentDescription,
                tint = HaulaufGold,
                modifier = modifier
            )
        }
    }
}

