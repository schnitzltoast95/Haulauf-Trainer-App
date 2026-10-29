package com.example.myapplication.ui

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.myapplication.data.*
import java.io.File

@Composable
fun ExerciseMediaPreview(media: ExerciseMedia, number: Int) {
    val de = LocalStrings.current === StringsDe
    var open by remember { mutableStateOf(false) }
    val label = if (media.kind == ExerciseMediaKind.VIDEO) {
        if (de) "Video $number abspielen" else "Play video $number"
    } else if (de) "Bild / GIF $number öffnen" else "Open image / GIF $number"
    if (media.kind == ExerciseMediaKind.VIDEO) {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(label) }
    } else {
        Column {
            MediaImage(media.path, label, Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 240.dp).clickable { open = true })
            TextButton(onClick = { open = true }) { Text(label) }
        }
    }
    if (open) Dialog(onDismissRequest = { open = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.safeDrawingPadding().padding(16.dp)) {
                TextButton(onClick = { open = false }) { Text(if (de) "Schließen" else "Close") }
                if (media.kind == ExerciseMediaKind.VIDEO) LocalVideo(media.path, Modifier.fillMaxWidth().weight(1f))
                else MediaImage(media.path, label, Modifier.fillMaxWidth().weight(1f))
            }
        }
    }
}

@Composable
private fun MediaImage(path: String, label: String, modifier: Modifier) {
    val context = LocalContext.current
    val de = LocalStrings.current === StringsDe
    var failed by remember(path) { mutableStateOf(false) }
    val loader = remember(context) {
        ImageLoader.Builder(context).components {
            if (android.os.Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
        }.build()
    }
    DisposableEffect(loader) { onDispose { loader.shutdown() } }
    if (failed) Text(if (de) "Bild nicht verfügbar. Bitte erneut hinzufügen." else "Image unavailable. Please add it again.", color = MaterialTheme.colorScheme.error)
    else AsyncImage(model = if (path.contains("://")) Uri.parse(path) else File(path), imageLoader = loader,
        contentDescription = label, contentScale = ContentScale.Fit, modifier = modifier, onError = { failed = true })
}

@Composable
private fun LocalVideo(path: String, modifier: Modifier) {
    var player by remember { mutableStateOf<VideoView?>(null) }
    var failed by remember { mutableStateOf(false) }
    val owner = LocalLifecycleOwner.current
    val de = LocalStrings.current === StringsDe
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) player?.pause() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); player?.stopPlayback() }
    }
    if (failed) Text(if (de) "Video kann auf diesem Gerät nicht abgespielt werden. Bitte ein anderes Format hinzufügen." else "This device cannot play the video. Please add another format.", color = MaterialTheme.colorScheme.error)
    else AndroidView(modifier = modifier, factory = { context ->
        VideoView(context).apply {
            player = this
            setMediaController(MediaController(context).also { it.setAnchorView(this) })
            setOnErrorListener { _, _, _ -> failed = true; true }
            setOnPreparedListener { if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) start() }
            setVideoURI(if (path.contains("://")) Uri.parse(path) else Uri.fromFile(File(path)))
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(move: Move, onEdit: (() -> Unit)?, onBack: () -> Unit) {
    val s = LocalStrings.current
    val de = s === StringsDe
    var showSources by remember { mutableStateOf(false) }
    Scaffold(bottomBar = {
        if (onEdit != null) Surface {
            OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).heightIn(min = 56.dp)) {
                Text(if (move.id.startsWith("custom_")) s.editMove else if (de) "Meine Ergänzungen bearbeiten" else "Edit my additions")
            }
        }
    }, topBar = { TopAppBar(title = { Text(move.displayName) }, navigationIcon = {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back) }
    }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(categoryLabel(move.category), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            move.historicalImageRes?.let {
                AsyncImage(it, if (de) "Historische Illustration zu ${move.displayName}" else "Historical illustration for ${move.displayName}",
                    contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp))
            }
            Text(move.description?.takeIf { it.isNotBlank() } ?: s.noDescription, style = MaterialTheme.typography.bodyLarge)
            if (move.historicalImageRes != null || move.historicalTextSources.isNotEmpty()) {
                move.historicalNote?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                TextButton(onClick = { showSources = !showSources }) {
                    Text(if (de) { if (showSources) "Quellen schließen" else "Quellen und historische Einordnung" } else { if (showSources) "Hide sources" else "Sources and historical context" })
                }
                if (showSources) MoveHistoricalAttribution(move, showingHistoricalImage = move.historicalImageRes != null)
            }
            HorizontalDivider()
            Text(if (de) "Meine Ergänzungen" else "My additions", style = MaterialTheme.typography.titleLarge)
            Text(move.notes.ifBlank { if (de) "Noch keine eigenen Notizen." else "No personal notes yet." }, style = MaterialTheme.typography.bodyLarge)
            move.media.forEachIndexed { index, item -> key(item.path) { ExerciseMediaPreview(item, index + 1) } }

        }
    }
}

@Composable
fun categoryLabel(category: MoveCategory): String {
    val s = LocalStrings.current
    return when (category) { MoveCategory.HAU -> s.hau; MoveCategory.HUT -> s.hut; MoveCategory.STICH -> s.stich }
}
