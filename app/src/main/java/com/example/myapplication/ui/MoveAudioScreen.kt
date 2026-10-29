package com.example.myapplication.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.material3.Checkbox
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.Move
import com.example.myapplication.data.MoveCategory
import com.example.myapplication.ui.theme.HaulaufCard
import com.example.myapplication.ui.theme.HaulaufGoldLight
import com.example.myapplication.ui.theme.HaulaufTextSecondary
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveAudioListScreen(
    allMoves: List<Move>,
    moveOverrides: Map<String, String>,
    onAddMove: (String, MoveCategory) -> Unit,
    onTestMove: (String) -> Unit,
    onOpenMoveDetails: (String) -> Unit,
    onBack: () -> Unit,
    selectedIds: Set<String>? = null,
    onToggle: (String) -> Unit = {},
    onApplySelection: (() -> Unit)? = null
) {
    val s = LocalStrings.current
    val scrollState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var newMoveName by rememberSaveable { mutableStateOf("") }
    var newMoveCategory by rememberSaveable { mutableStateOf(MoveCategory.HAU) }
    var categoryFilter by rememberSaveable { mutableStateOf<MoveCategory?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var showAddMoveDialog by rememberSaveable { mutableStateOf(false) }

    val visible = remember(allMoves, categoryFilter, query) {
        allMoves.filter { (categoryFilter == null || it.category == categoryFilter) && it.displayName.contains(query, ignoreCase = true) }
    }
    Scaffold(
        bottomBar = {
            if (onApplySelection != null) Surface {
                Button(onClick = onApplySelection, modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).heightIn(min = 56.dp)) {
                    val count = selectedIds.orEmpty().size
                    Text(if (s === StringsDe) { if (count == 1) "1 Übung übernehmen" else "$count Übungen übernehmen" }
                        else { if (count == 1) "Use 1 exercise" else "Use $count exercises" })
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (selectedIds != null) s.selectMoves else s.moveDetailsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = s.back,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (selectedIds == null) IconButton(onClick = { showAddMoveDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = s.addMove,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "controls") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true,
                label = { Text(if (s === StringsDe) "Übung suchen" else "Search exercises") }, modifier = Modifier.fillMaxWidth())
            WrappingControls {
                listOf(null, MoveCategory.HAU, MoveCategory.HUT, MoveCategory.STICH).forEach { category ->
                    FilterChip(selected = categoryFilter == category, onClick = { categoryFilter = category }, label = {
                        Text(when (category) { MoveCategory.HAU -> s.haue; MoveCategory.HUT -> s.huten; MoveCategory.STICH -> s.stiche; null -> if (s === StringsDe) "Alle" else "All" })
                    })
                }
            }
            if (selectedIds != null) {
                val allVisibleSelected = visible.isNotEmpty() && visible.all { it.id in selectedIds }
                TextButton(onClick = {
                    visible.filter { (it.id in selectedIds) == allVisibleSelected }.forEach { onToggle(it.id) }
                }, enabled = visible.isNotEmpty()) {
                    Text(if (allVisibleSelected) s.selectNone else s.selectAll)
                }
            }
            }
            }
            if (visible.isEmpty()) item { Text(if (s === StringsDe) "Keine passenden Übungen." else "No matching exercises.") }
            items(visible, key = { it.id }) { move ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenMoveDetails(move.id) },
                    shape = MaterialTheme.shapes.medium,
                    color = if (selectedIds != null && move.id in selectedIds) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    BoxWithConstraints {
                    val compact = maxWidth < 320.dp || LocalDensity.current.fontScale > 1.3f
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        val preview = move.historicalImageRes ?: move.media.firstOrNull { it.kind == com.example.myapplication.data.ExerciseMediaKind.IMAGE }?.path?.let { moveImageModel(LocalContext.current, it, null) }
                        if (preview != null && !compact) AsyncImage(preview, null, contentScale = ContentScale.Fit, modifier = Modifier.size(64.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(move.displayName, style = MaterialTheme.typography.titleMedium)
                            Text(categoryLabel(move.category), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (preview != null && compact) AsyncImage(preview, null, contentScale = ContentScale.Fit, modifier = Modifier.size(64.dp))
                            if (move.notes.isNotBlank() || move.media.isNotEmpty()) Text(if (s === StringsDe) "Mit eigenen Ergänzungen" else "Personal additions", style = MaterialTheme.typography.bodySmall)
                        }
                        if (selectedIds != null) Checkbox(checked = move.id in selectedIds, onCheckedChange = { onToggle(move.id) },
                            modifier = Modifier.semantics { contentDescription = if (s === StringsDe) "${move.displayName} fürs Training auswählen" else "Select ${move.displayName} for training" })
                        else IconButton(onClick = { onTestMove(move.id) }) {
                            Icon(Icons.Filled.PlayArrow, if (s === StringsDe) "${move.displayName} anhören" else "Listen to ${move.displayName}")
                        }
                    }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showAddMoveDialog) {
        AlertDialog(
            onDismissRequest = { showAddMoveDialog = false },
            title = { Text(s.addMove) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newMoveName,
                        onValueChange = { newMoveName = it },
                        label = { Text(s.newMoveName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    WrappingControls {
                        CategoryButton(
                            label = s.hau,
                            selected = newMoveCategory == MoveCategory.HAU,
                            onClick = { newMoveCategory = MoveCategory.HAU }
                        )
                        CategoryButton(
                            label = s.hut,
                            selected = newMoveCategory == MoveCategory.HUT,
                            onClick = { newMoveCategory = MoveCategory.HUT }
                        )
                        CategoryButton(
                            label = s.stich,
                            selected = newMoveCategory == MoveCategory.STICH,
                            onClick = { newMoveCategory = MoveCategory.STICH }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newMoveName.trim()
                        if (trimmed.isNotEmpty()) {
                            onAddMove(trimmed, newMoveCategory)
                            newMoveName = ""
                            showAddMoveDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    s.moveAddedFormat.format(trimmed)
                                )
                            }
                        }
                    },
                    enabled = newMoveName.isNotBlank()
                ) {
                    Text(s.addMove, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMoveDialog = false }) {
                    Text(s.cancel)
                }
            }
        )
    }
}

@Composable
private fun CategoryButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(label, fontSize = 12.sp)
    }
}
