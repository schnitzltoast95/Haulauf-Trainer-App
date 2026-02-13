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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    onTestMove: (String) -> Unit,
    onOpenMoveDetails: (String) -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var pendingMoveForPicker by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val moveId = pendingMoveForPicker
        if (moveId != null) {
            onSaveOverride(moveId, uri?.toString())
            pendingMoveForPicker = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Move Audio") },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
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
                text = "Customize the audio for each move. You can record your own voice or upload an audio file.",
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
                                    text = "Custom: $override",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HaulaufTextSecondary,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = { onSaveOverride(move.id, null) },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text("Reset")
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
                                        contentDescription = "Test",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Test",
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
                                        text = "Rec",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HaulaufTextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        pendingMoveForPicker = move.id
                                        filePicker.launch("audio/*")
                                    }
                                ) {
                                    Text(
                                        text = "File",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HaulaufTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
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
                title = { Text("Audio: ${move.displayName}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück", tint = Color.White)
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
                Text("Custom audio: $overrideUri", style = MaterialTheme.typography.bodySmall, color = HaulaufTextSecondary)
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = { onTestMove(moveId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Test abspielen")
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
                Text(if (isRecording) "Aufnahme stoppen" else "Neue Aufnahme")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { filePicker.launch("audio/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Datei wählen")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onSaveOverride(moveId, null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zurücksetzen auf Standard")
            }
        }
    }
}
