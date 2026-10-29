package com.example.myapplication.ui

import androidx.compose.ui.semantics.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Move
import com.example.myapplication.data.MoveCategory
import com.example.myapplication.data.TrainingPreset
import com.example.myapplication.ui.theme.HaulaufCard
import com.example.myapplication.ui.theme.HaulaufCardBorder
import com.example.myapplication.ui.theme.HaulaufGold
import com.example.myapplication.ui.theme.HaulaufGoldDark
import com.example.myapplication.ui.theme.HaulaufGoldLight
import com.example.myapplication.ui.theme.HaulaufTextSecondary
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

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
    onBack: () -> Unit,
    savedName: String? = null,
    onSaveCopy: ((String) -> Unit)? = null
) {
    val s = LocalStrings.current
    var showMoveInfoDialog by remember { mutableStateOf<Move?>(null) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf(savedName.orEmpty()) }
    var saveCopy by remember { mutableStateOf(false) }
    var showPriorities by remember { mutableStateOf(false) }
    val de = s === StringsDe
    var selectedCategoryTab by remember { mutableIntStateOf(0) } // 0 = Alle, 1 = Haue, 2 = Huten, 3 = Stiche
    var rounds by remember(preset.rounds) { mutableIntStateOf(preset.rounds) }
    var unitsPerRound by remember(preset.unitsPerRound) { mutableIntStateOf(preset.unitsPerRound) }
    var fixedInterval by remember(preset.reactionIntervalMs) { mutableStateOf(preset.reactionIntervalMinMs == null) }
    var intervalSec by remember(preset.reactionIntervalMs) { mutableFloatStateOf(preset.reactionIntervalMs / 1000f) }
    var intervalMinSec by remember(preset.reactionIntervalMinMs ?: 5f) { mutableFloatStateOf((preset.reactionIntervalMinMs ?: 5000) / 1000f) }
    var intervalMaxSec by remember(preset.reactionIntervalMaxMs ?: 7f) { mutableFloatStateOf((preset.reactionIntervalMaxMs ?: 7000) / 1000f) }
    var pauseSec by remember(preset.pauseBetweenRoundsMs) { mutableFloatStateOf(preset.pauseBetweenRoundsMs / 1000f) }
    var selectedIds by remember(preset.selectedMoveIds) { mutableStateOf(preset.selectedMoveIds) }
    var movePriorities by remember(preset.movePriorities) {
        mutableStateOf(
            preset.movePriorities.mapValues { (_, priority) -> normalizeMovePriority(priority) }
        )
    }
    var showAdvanced by remember { mutableStateOf(false) }
    var showSelection by remember { mutableStateOf(false) }
    var endBeep by remember(preset.endBeepEnabled) { mutableStateOf(preset.endBeepEnabled) }
    var metronome by remember(preset.metronomeEnabled) { mutableStateOf(preset.metronomeEnabled) }
    var metronomeBeats by remember(preset.metronomeBeatIntervalMs) {
        mutableIntStateOf(preset.metronomeBeatIntervalMs.coerceIn(1L, 32L).toInt())
    }
    var noRep by remember(preset.noImmediateRepetition) { mutableStateOf(preset.noImmediateRepetition) }
    var initialCountdownSec by remember(preset.initialCountdownMs) { mutableIntStateOf((preset.initialCountdownMs / 1000).toInt()) }
    val availableMoveIds = remember(allMoves) { allMoves.map { it.id }.toSet() }
    val selectedValidIds = selectedIds.intersect(availableMoveIds)

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
                selectedMoveIds = selectedValidIds,
                movePriorities = movePriorities
                    .filterKeys { it in selectedValidIds }
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
                title = { Text(savedName ?: s.configureTrainingTitle, maxLines = 1) },
                actions = {
                    if (savedName != null) TextButton(onClick = { saveCopy = true; presetName = "$savedName (${if (de) "Kopie" else "copy"})"; showSavePresetDialog = true }) {
                        Text(if (de) "Kopie" else "Copy")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = s.back)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 0.dp,
                shadowElevation = 8.dp
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    val actionWidth = if (LocalDensity.current.fontScale > 1.3f || maxWidth < 280.dp) maxWidth else (maxWidth - 12.dp) / 2
                    WrappingControls {
                    Button(
                        onClick = onStart,
                        enabled = selectedValidIds.isNotEmpty(),
                        modifier = Modifier
                            .width(actionWidth)
                            .heightIn(min = 88.dp),
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
                                .fillMaxWidth()
                                .heightIn(min = 88.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(MaterialTheme.colorScheme.primary, HaulaufGoldDark)
                                    ),
                                    shape = MaterialTheme.shapes.large
                                )
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (savedName != null) { if (de) "Nur diesmal starten" else "Start this time" } else s.startTraining,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { saveCopy = false; showSavePresetDialog = true },
                        modifier = Modifier
                            .width(actionWidth)
                            .heightIn(min = 88.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = if (savedName != null) s.save else s.savePreset,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                }
            }
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = (if (de) "Dauer ca. " else "About ") + "${durationMinutes}m ${durationSeconds}s",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = s.totalCallsAcrossRoundsFormat.format(rounds * unitsPerRound, rounds) +
                        (if (de) " · Start in ${initialCountdownSec}s" else " · Starts in ${initialCountdownSec}s"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = { showSelection = !showSelection },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) {
                Text(
                    text = if (showSelection) {
                        if (de) "Auswahl schließen (${selectedValidIds.size})" else "Close selection (${selectedValidIds.size})"
                    } else {
                        if (de) "Übungen auswählen (${selectedValidIds.size})" else "Choose exercises (${selectedValidIds.size})"
                    },
                    textAlign = TextAlign.Center
                )
            }
            if (selectedValidIds.isEmpty()) Text(
                if (de) "Wähle mindestens eine Übung zum Starten." else "Select at least one exercise to start.",
                color = MaterialTheme.colorScheme.error
            ) else if (!showSelection) Text(
                text = allMoves.filter { it.id in selectedValidIds }.joinToString(", ") { it.displayName },
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (showSelection) {
                // Select moves
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardDefaults.shape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(Modifier.padding(16.dp)) {
                        // Filter moves based on selected tab
                        val movesByCategory = allMoves.groupBy { it.category }
                        val hauMoves = movesByCategory[MoveCategory.HAU] ?: emptyList()
                        val hutMoves = movesByCategory[MoveCategory.HUT] ?: emptyList()
                        val stichMoves = movesByCategory[MoveCategory.STICH] ?: emptyList()
                        val movesToShow = when (selectedCategoryTab) {
                            1 -> hauMoves
                            2 -> hutMoves
                            3 -> stichMoves
                            else -> allMoves
                        }
                        val visibleMoveIds = movesToShow.map { it.id }.toSet()
                        val allVisibleSelected = movesToShow.isNotEmpty() && movesToShow.all { it.id in selectedValidIds }
                        val selectedHauCount = hauMoves.count { it.id in selectedValidIds }
                        val selectedHutCount = hutMoves.count { it.id in selectedValidIds }
                        val selectedStichCount = stichMoves.count { it.id in selectedValidIds }
                        val totalAllCount = allMoves.size
                        val totalHauCount = hauMoves.size
                        val totalHutCount = hutMoves.size
                        val totalStichCount = stichMoves.size

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = s.selectMoves,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = {
                                    if (allVisibleSelected) {
                                        selectedIds = selectedValidIds - visibleMoveIds
                                        movePriorities = movePriorities - visibleMoveIds
                                    } else {
                                        val missingIds = visibleMoveIds - selectedValidIds
                                        selectedIds = selectedValidIds + visibleMoveIds
                                        movePriorities = movePriorities + missingIds.associateWith { MIN_MOVE_PRIORITY }
                                    }
                                },
                                enabled = movesToShow.isNotEmpty(),
                                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = if (allVisibleSelected) s.selectNone else s.selectAll,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        TextButton(onClick = { showPriorities = !showPriorities }) {
                            Text(if (showPriorities) { if (de) "Gewichtung schließen" else "Hide weighting" } else { if (de) "Gewichtung anpassen" else "Adjust weighting" })
                        }
                        // Tabs for categories
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            TabRow(
                                selectedTabIndex = selectedCategoryTab,
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary,
                                divider = {},
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier
                                            .tabIndicatorOffset(tabPositions[selectedCategoryTab])
                                            .padding(horizontal = 14.dp),
                                        height = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            ) {
                                Tab(
                                    selected = selectedCategoryTab == 0,
                                    onClick = { selectedCategoryTab = 0 },
                                    selectedContentColor = MaterialTheme.colorScheme.primary,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    text = {
                                        Text(
                                            "${s.all}\n(${selectedValidIds.size}/$totalAllCount)",
                                            fontWeight = if (selectedCategoryTab == 0) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                )
                                Tab(
                                    selected = selectedCategoryTab == 1,
                                    onClick = { selectedCategoryTab = 1 },
                                    selectedContentColor = MaterialTheme.colorScheme.primary,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    text = {
                                        Text(
                                            "${s.haue}\n($selectedHauCount/$totalHauCount)",
                                            fontWeight = if (selectedCategoryTab == 1) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                )
                                Tab(
                                    selected = selectedCategoryTab == 2,
                                    onClick = { selectedCategoryTab = 2 },
                                    selectedContentColor = MaterialTheme.colorScheme.primary,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    text = {
                                        Text(
                                            "${s.huten}\n($selectedHutCount/$totalHutCount)",
                                            fontWeight = if (selectedCategoryTab == 2) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                )
                                Tab(
                                    selected = selectedCategoryTab == 3,
                                    onClick = { selectedCategoryTab = 3 },
                                    selectedContentColor = MaterialTheme.colorScheme.primary,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    text = {
                                        Text(
                                            "${s.stiche}\n($selectedStichCount/$totalStichCount)",
                                            fontWeight = if (selectedCategoryTab == 3) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        movesToShow.forEach { move ->
                                    val selected = move.id in selectedValidIds
                                    val priority = normalizeMovePriority(movePriorities[move.id] ?: MIN_MOVE_PRIORITY)
                                    val increasePriority: () -> Unit = {
                                        if (!selected) {
                                            selectedIds = selectedValidIds + move.id
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
                                                    selectedIds = selectedValidIds - move.id
                                                    movePriorities = movePriorities - move.id
                                                } else {
                                                    selectedIds = selectedValidIds + move.id
                                                    movePriorities = movePriorities + (move.id to MIN_MOVE_PRIORITY)
                                                }
                                            },
                                                onDoubleClick = if (showPriorities) increasePriority else null,
                                                onLongClick = if (showPriorities) increasePriority else null
                                            ),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
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
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f),
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
                                                if (showPriorities) TextButton(onClick = increasePriority) {
                                                    Text("${priority}×")
                                                }
                                            }
                                        }
                                        IconButton(
                                            onClick = { showMoveInfoDialog = move },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Info,
                                                contentDescription = s.moveInfo,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                    shape = RectangleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (selected) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = s.selectionSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${s.haue}: $selectedHauCount/$totalHauCount · ${s.huten}: $selectedHutCount/$totalHutCount · ${s.stiche}: $selectedStichCount/$totalStichCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }
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
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(Modifier.padding(16.dp)) {
                    WrappingControls {
                        Text(
                            text = s.reactionInterval,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                    text = s.random,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                modifier = Modifier.semantics { contentDescription = s.random },
                                checked = !fixedInterval,
                                onCheckedChange = { checked -> fixedInterval = !checked },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    if (fixedInterval) {
                        Text(
                            text = s.secondsRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                modifier = Modifier.weight(1f).semantics { contentDescription = s.reactionInterval; stateDescription = "%.2f ${s.seconds}".format(intervalSec) },
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.onSurface,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                            Text(
                                text = "%.2fs".format(intervalSec),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.widthIn(min = 48.dp)
                            )
                        }
                    } else {
                        Text(
                            text = s.minMaxSecondsRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Slider(
                                    modifier = Modifier.semantics { contentDescription = "${s.reactionInterval}: Minimum"; stateDescription = "%.2f ${s.seconds}".format(intervalMinSec) },
                                    value = intervalMinSec.coerceIn(0.05f, 5f),
                                    onValueChange = { v ->
                                        val n = (v * 20).toInt() / 20f
                                        intervalMinSec = n
                                        if (intervalMaxSec < intervalMinSec) intervalMaxSec = intervalMinSec
                                    },
                                    valueRange = 0.05f..5f,
                                    steps = ((5f - 0.05f) / 0.05f).toInt() - 1,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.onSurface,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                                Text("%.2fs".format(intervalMinSec), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Slider(
                                    modifier = Modifier.semantics { contentDescription = "${s.reactionInterval}: Maximum"; stateDescription = "%.2f ${s.seconds}".format(intervalMaxSec) },
                                    value = intervalMaxSec.coerceIn(0.05f, 5f),
                                    onValueChange = { v ->
                                        intervalMaxSec = ((v * 20).toInt() / 20f).coerceAtLeast(intervalMinSec)
                                    },
                                    valueRange = 0.05f..5f,
                                    steps = ((5f - 0.05f) / 0.05f).toInt() - 1,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.onSurface,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                                Text("%.2fs".format(intervalMaxSec), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Pause between rounds
            if (rounds > 1) StepperCard(
                title = s.pauseBetweenRounds,
                value = pauseSec.toInt(),
                onChange = { v -> pauseSec = v.coerceAtLeast(0).toFloat() }
            )

            OutlinedButton(onClick = { showAdvanced = !showAdvanced }, modifier = Modifier.fillMaxWidth()) {
                Text(if (showAdvanced) { if (de) "Erweitert schließen" else "Hide advanced" } else s.advancedSettings)
            }

            // Advanced settings kept minimal / collapsed toggle (optional)
            if (showAdvanced) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardDefaults.shape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = s.advancedSettings,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                modifier = Modifier.semantics { contentDescription = s.noImmediateRep },
                                checked = noRep,
                                onCheckedChange = { noRep = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = s.noImmediateRep,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                modifier = Modifier.semantics { contentDescription = s.metronome },
                                checked = metronome,
                                onCheckedChange = { metronome = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = s.metronome,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (metronome) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = s.beatInterval,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = s.beatIntervalHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            StepperInline(
                                label = s.beatInterval,
                                value = metronomeBeats,
                                onChange = { v -> metronomeBeats = v.coerceIn(1, 32) }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                modifier = Modifier.semantics { contentDescription = s.endBeep },
                                checked = endBeep,
                                onCheckedChange = { endBeep = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = s.endBeep,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${s.countdown} (${s.seconds}, 1-30)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        StepperInline(
                            label = s.countdown,
                            value = initialCountdownSec.coerceIn(1, 30),
                            onChange = { v -> initialCountdownSec = v.coerceIn(1, 30) }
                        )
                    }
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
                            if (saveCopy && onSaveCopy != null) onSaveCopy(presetName.trim()) else onSavePreset(presetName.trim())
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

