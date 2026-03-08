package com.example.myapplication.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var newMoveName by remember { mutableStateOf("") }
    var newMoveCategory by remember { mutableStateOf(MoveCategory.HAU) }
    var showAddMoveDialog by remember { mutableStateOf(false) }

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
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddMoveDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = s.addMove,
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = s.customizeAudioDesc,
                style = MaterialTheme.typography.bodySmall,
                color = HaulaufTextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            allMoves.forEach { move ->
                val overrideUri = moveOverrides[move.id]
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardDefaults.shape,
                    color = HaulaufCard
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = move.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Text(
                            text = when {
                                overrideUri != null -> s.customAudioEnabled
                                else -> s.defaultAudioEnabled
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = HaulaufTextSecondary
                        )
                        if (!move.description.isNullOrBlank()) {
                            Text(
                                text = "${s.descriptionLabel}: ${move.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = HaulaufTextSecondary,
                                maxLines = 2
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onTestMove(move.id)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            s.testPlaybackStartedFormat.format(move.displayName)
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(s.test, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { onOpenMoveDetails(move.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(s.editMove, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
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
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = HaulaufGoldLight,
                            unfocusedBorderColor = HaulaufTextSecondary,
                            cursorColor = HaulaufGoldLight,
                            focusedLabelColor = HaulaufGoldLight,
                            unfocusedLabelColor = HaulaufTextSecondary
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Text(s.addMove, color = HaulaufGoldLight)
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
            contentColor = if (selected) HaulaufGoldLight else HaulaufTextSecondary
        )
    ) {
        Text(label, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveAudioScreen(
    moveId: String,
    allMoves: List<Move>,
    overrideUri: String?,
    onSaveOverride: (String, String?) -> Unit,
    onSaveMoveInfo: (String, String?, String?) -> Unit,
    onDeleteMove: (String) -> Unit,
    onTestMove: (String) -> Unit,
    onBack: () -> Unit
) {
    val s = LocalStrings.current
    val move = allMoves.find { it.id == moveId } ?: return
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAudioMenu by remember { mutableStateOf(false) }

    var description by remember(move.id, move.description) { mutableStateOf(move.description ?: "") }
    var imagePath by remember(move.id, move.imagePath) { mutableStateOf(move.imagePath) }
    var isRecording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    fun saveMoveInfoNow() {
        onSaveMoveInfo(move.id, description.trim().ifBlank { null }, imagePath)
    }

    fun toggleRecording() {
        if (!hasMicPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        if (!isRecording) {
            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
            val file = File(outputDir, "haulauf_${move.id}.m4a")
            recordingFile = file
            val rec = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = rec
            isRecording = true
            scope.launch { snackbarHostState.showSnackbar(s.recordingStarted) }
        } else {
            try {
                recorder?.stop()
            } catch (_: Exception) {
            }
            recorder?.release()
            recorder = null
            isRecording = false
            recordingFile?.let { file ->
                onSaveOverride(move.id, Uri.fromFile(file).toString())
            }
            scope.launch { snackbarHostState.showSnackbar(s.recordingSaved) }
        }
    }

    val audioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onSaveOverride(move.id, uri.toString())
            scope.launch { snackbarHostState.showSnackbar(s.audioUpdated) }
        }
    }

    val imageFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagePath = uri.toString()
            saveMoveInfoNow()
            scope.launch { snackbarHostState.showSnackbar(s.imageUpdated) }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recorder?.run {
                try {
                    stop()
                } catch (_: Exception) {
                }
                release()
            }
            recorder = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(s.audioTitleFormat.format(move.displayName)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = s.back, tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardDefaults.shape,
                color = HaulaufCard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(s.audio, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (overrideUri != null) s.customAudioEnabled else s.defaultAudioEnabled,
                        style = MaterialTheme.typography.bodySmall,
                        color = HaulaufTextSecondary
                    )
                    Button(
                        onClick = { showAudioMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.editAudioMenu)
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardDefaults.shape,
                color = HaulaufCard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(s.moveDetails, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(s.descriptionFieldLabel) },
                        placeholder = { Text(s.descriptionPlaceholder) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
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
                    Text(
                        text = "${s.image}: ${if (imagePath.isNullOrBlank()) s.none else s.imageUpdated}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HaulaufTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { imageFilePicker.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(s.chooseImage, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                imagePath = null
                                saveMoveInfoNow()
                                scope.launch { snackbarHostState.showSnackbar(s.imageRemoved) }
                            },
                            enabled = !imagePath.isNullOrBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(s.removeImage, fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = {
                            saveMoveInfoNow()
                            scope.launch { snackbarHostState.showSnackbar(s.moveDetailsSaved) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.saveDetails)
                    }
                }
            }

            if (move.id.startsWith("custom_")) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(s.deleteMoveAction)
                }
            }
        }
    }

    if (showAudioMenu) {
        AlertDialog(
            onDismissRequest = { showAudioMenu = false },
            title = { Text(s.editAudioMenu) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onTestMove(move.id)
                            showAudioMenu = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    s.testPlaybackStartedFormat.format(move.displayName)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.testPlayback)
                    }
                    OutlinedButton(
                        onClick = {
                            toggleRecording()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isRecording) s.stopRecording else s.newRecording)
                    }
                    OutlinedButton(
                        onClick = {
                            audioFilePicker.launch("audio/*")
                            showAudioMenu = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.chooseFile)
                    }
                    OutlinedButton(
                        onClick = {
                            onSaveOverride(move.id, null)
                            showAudioMenu = false
                            scope.launch { snackbarHostState.showSnackbar(s.audioResetDone) }
                        },
                        enabled = overrideUri != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.resetToDefault)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudioMenu = false }) {
                    Text(s.close)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(s.deleteMoveTitle) },
            text = { Text(s.deleteMoveConfirm.format(move.displayName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteMove(move.id)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text(s.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(s.cancel)
                }
            }
        )
    }
}
