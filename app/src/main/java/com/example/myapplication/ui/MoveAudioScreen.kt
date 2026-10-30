package com.example.myapplication.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    onBack: () -> Unit
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(s.moveDetailsTitle) },
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
                    IconButton(onClick = { showAddMoveDialog = true }) {
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
            Text(
                text = s.customizeAudioDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true,
                label = { Text(if (s === StringsDe) "Übung suchen" else "Search exercises") }, modifier = Modifier.fillMaxWidth())
            WrappingControls {
                listOf(null, MoveCategory.HAU, MoveCategory.HUT, MoveCategory.STICH).forEach { category ->
                    FilterChip(selected = categoryFilter == category, onClick = { categoryFilter = category }, label = {
                        Text(when (category) { MoveCategory.HAU -> s.haue; MoveCategory.HUT -> s.huten; MoveCategory.STICH -> s.stiche; null -> if (s === StringsDe) "Alle" else "All" })
                    })
                }
            }
            AppActionButton(onClick = { showAddMoveDialog = true }, modifier = Modifier.fillMaxWidth()) { Text(s.addMove) }
            }
            }
            if (visible.isEmpty()) item { Text(if (s === StringsDe) "Keine passenden Übungen." else "No matching exercises.") }
            items(visible, key = { it.id }) { move ->
                val overrideUri = moveOverrides[move.id]
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardDefaults.shape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = move.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                overrideUri != null -> s.customAudioEnabled
                                else -> s.defaultAudioEnabled
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!move.description.isNullOrBlank()) {
                            Text(
                                text = "${s.descriptionLabel}: ${move.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                        BoxWithConstraints(Modifier.fillMaxWidth()) {
                            val actionWidth = if (maxWidth < 300.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f)
                                maxWidth else (maxWidth - 8.dp) / 2
                            WrappingControls {
                            AppActionButton(
                                onClick = {
                                    onTestMove(move.id)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            s.testPlaybackStartedFormat.format(move.displayName)
                                        )
                                    }
                                },
                                modifier = Modifier.width(actionWidth).heightIn(min = 72.dp), tone = ActionTone.Secondary
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(s.test)
                            }
                            AppActionButton(
                                onClick = { onOpenMoveDetails(move.id) },
                                modifier = Modifier.width(actionWidth).heightIn(min = 72.dp)
                            ) {
                                Text(s.editMove)
                            }
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
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
