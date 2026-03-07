package com.example.myapplication.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.data.Move
import com.example.myapplication.ui.theme.HaulaufCard
import com.example.myapplication.ui.theme.HaulaufGoldDark
import com.example.myapplication.ui.theme.HaulaufGoldLight
import com.example.myapplication.ui.theme.HaulaufTextSecondary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveAudioListScreen(
    allMoves: List<com.example.myapplication.data.Move>,
    moveOverrides: Map<String, String>,
    onSaveOverride: (String, String?) -> Unit,
    onSaveMoveInfo: (String, String?, String?) -> Unit,
    onTestMove: (String) -> Unit,
    onOpenMoveDetails: (String) -> Unit,
    onBack: () -> Unit
) {
    val s = LocalStrings.current
    val scrollState = rememberScrollState()
    var pendingMoveForPicker by remember { mutableStateOf<String?>(null) }
    var pendingMoveForImagePicker by remember { mutableStateOf<String?>(null) }
    var editingMoveInfo by remember { mutableStateOf<Move?>(null) }

    val audioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val moveId = pendingMoveForPicker
        if (moveId != null) {
            onSaveOverride(moveId, uri?.toString())
            pendingMoveForPicker = null
        }
    }

    val imageFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val moveId = pendingMoveForImagePicker
        if (moveId != null) {
            val move = allMoves.find { it.id == moveId }
            if (move != null) {
                onSaveMoveInfo(moveId, move.description, uri?.toString())
            }
            pendingMoveForImagePicker = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.moveDetailsTitle) },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = s.close,
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
                val override = moveOverrides[move.id]
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
                        if (override != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${s.customAudioPrefix}: $override",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HaulaufTextSecondary,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = { onSaveOverride(move.id, null) },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(s.reset)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onTestMove(move.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = ButtonDefaults.ContentPadding,
                                modifier = Modifier
                                    .height(36.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = s.test,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = s.test,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onOpenMoveDetails(move.id) }
                                ) {
                                    Text(
                                        text = s.rec,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HaulaufTextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        pendingMoveForPicker = move.id
                                        audioFilePicker.launch("audio/*")
                                    }
                                ) {
                                    Text(
                                        text = s.audioFile,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HaulaufTextSecondary
                                    )
                                }
                            }
                        }
                        
                        // Description and Image section
                        if (move.description != null || move.imagePath != null) {
                            Spacer(Modifier.height(8.dp))
                            if (move.description != null) {
                                Text(
                                    text = "${s.descriptionLabel}: ${move.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HaulaufTextSecondary,
                                    maxLines = 2
                                )
                            }
                            if (move.imagePath != null) {
                                Text(
                                    text = "${s.image}: ${move.imagePath}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HaulaufTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        // Edit description and image buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { editingMoveInfo = move },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(s.editInfo, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    pendingMoveForImagePicker = move.id
                                    imageFilePicker.launch("image/*")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(s.image, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            
            // Edit Move Info Dialog
            editingMoveInfo?.let { move ->
                MoveInfoEditDialog(
                    move = move,
                    onSave = { description, imagePath ->
                        onSaveMoveInfo(move.id, description, imagePath)
                        editingMoveInfo = null
                    },
                    onDismiss = { editingMoveInfo = null }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveAudioScreen(
    moveId: String,
    allMoves: List<Move>,
    overrideUri: String?,
    onSaveOverride: (String, String?) -> Unit,
    onTestMove: (String) -> Unit,
    onBack: () -> Unit
) {
    val s = LocalStrings.current
    val move = allMoves.find { it.id == moveId } ?: return
    val context = LocalContext.current
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

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onSaveOverride(moveId, uri?.toString())
    }

    Scaffold(
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
        ) {
            if (overrideUri != null) {
                Text("${s.customAudioPrefix}: $overrideUri", style = MaterialTheme.typography.bodySmall, color = HaulaufTextSecondary)
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = { onTestMove(moveId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.testPlayback)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (!hasMicPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        return@Button
                    }
                    if (!isRecording) {
                        // Start recording
                        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                            ?: context.filesDir
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
                    } else {
                        // Stop recording
                        try {
                            recorder?.stop()
                        } catch (_: Exception) {
                        }
                        recorder?.release()
                        recorder = null
                        isRecording = false
                        recordingFile?.let { file ->
                            onSaveOverride(moveId, Uri.fromFile(file).toString())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color.Red else Color.DarkGray
                )
            ) {
                Text(if (isRecording) s.stopRecording else s.newRecording)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { filePicker.launch("audio/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s.chooseFile)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onSaveOverride(moveId, null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s.resetToDefault)
            }
        }
    }
}

@Composable
private fun MoveInfoEditDialog(
    move: Move,
    onSave: (String?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalStrings.current
    var description by remember { mutableStateOf(move.description ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.editMoveInfoTitleFormat.format(move.displayName)) },
        text = {
            Column {
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
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${s.image}: ${move.imagePath ?: s.none}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HaulaufTextSecondary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        description.takeIf { it.isNotBlank() },
                        move.imagePath // Keep existing image path
                    )
                }
            ) {
                Text(s.save, color = HaulaufGoldLight)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancel)
            }
        }
    )
}


