package com.example.myapplication.ui

import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.*
import kotlinx.coroutines.launch
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
fun ExerciseMediaPreview(media: ExerciseMedia, number: Int, showLabel: Boolean = true) {
    val de = LocalStrings.current === StringsDe
    var open by remember { mutableStateOf(false) }
    val label = if (media.kind == ExerciseMediaKind.VIDEO) {
        if (de) "Video $number abspielen" else "Play video $number"
    } else if (de) "Bild / GIF $number öffnen" else "Open image / GIF $number"
    if (media.kind == ExerciseMediaKind.VIDEO) {
        if (showLabel) AppActionButton(tone = ActionTone.Secondary, onClick = { open = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(label) }
        else VideoThumbnail(media.path, Modifier.fillMaxWidth().height(240.dp).clickable { open = true }.semantics { contentDescription = label })
    } else {
        Column {
            MediaImage(media.path, label, Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 240.dp).clickable { open = true })
            if (showLabel) TextButton(onClick = { open = true }) { Text(label) }
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
private fun VideoThumbnail(path: String, modifier: Modifier) {
    val context = LocalContext.current
    val frame by produceState<Bitmap?>(null, path) {
        value = withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, if (path.contains("://")) Uri.parse(path) else Uri.fromFile(File(path)))
                if (android.os.Build.VERSION.SDK_INT >= 27) retriever.getScaledFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 640, 360)
                else retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            } catch (_: Exception) { null } finally { retriever.release() }
        }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        frame?.let { Image(it.asImageBitmap(), null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize()) }
        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface) {
            Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(4.dp))
        }
    }
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
fun ExerciseDetailScreen(
    move: Move,
    onEdit: ((ExerciseSection) -> Unit)?,
    onBack: () -> Unit,
    onPreview: (() -> Unit)? = null,
) {
    val s = LocalStrings.current
    val de = s === StringsDe
    var showSources by rememberSaveable(move.id) { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    Scaffold(topBar = {
        TopAppBar(title = { Text(move.displayName) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back) }
        }, actions = {
            if (onPreview != null) OutlinedIconButton(onClick = onPreview) {
                Icon(Icons.Default.PlayArrow, if (de) "Ansage anhören" else "Listen to call", tint = MaterialTheme.colorScheme.primary)
            }
            if (onEdit != null) Box {
                IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.MoreVert, if (de) "Übung bearbeiten" else "Edit exercise") }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    listOf(
                        ExerciseSection.MEDIA to if (de) "Bilder & Videos" else "Images & videos",
                        ExerciseSection.AUDIO to if (de) "Audio / Ansage" else "Audio / call",
                        ExerciseSection.NOTES to if (de) "Notizen" else "Notes",
                        ExerciseSection.DATA to if (de) "Beschreibung" else "Description"
                    ).forEach { (section, label) ->
                        DropdownMenuItem(text = { Text(label) }, enabled = section != ExerciseSection.DATA || move.id.startsWith("custom_"),
                            onClick = { menuOpen = false; onEdit(section) })
                    }
                }
            }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ExerciseGallery(move)
            Text(move.description?.takeIf { it.isNotBlank() } ?: s.noDescription, style = MaterialTheme.typography.bodyLarge)
            if (move.historicalImageRes != null || move.historicalTextSources.isNotEmpty()) {
                TextButton(onClick = { showSources = !showSources }) {
                    Text(if (de) "Quellen & Einordnung" else "Sources & context", modifier = Modifier.weight(1f, fill = false))
                    Icon(if (showSources) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        if (de) { if (showSources) "Einklappen" else "Ausklappen" } else { if (showSources) "Collapse" else "Expand" })
                }
                if (showSources) MoveHistoricalAttribution(move, showingHistoricalImage = move.historicalImageRes != null)
            }
            if (move.notes.isNotBlank()) {
                Text(if (de) "Notizen" else "Notes", style = MaterialTheme.typography.titleMedium)
                Text(move.notes, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ExerciseGallery(move: Move) {
    val de = LocalStrings.current === StringsDe
    val context = LocalContext.current
    val historicalRatio = remember(move.historicalImageRes) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        move.historicalImageRes?.let { BitmapFactory.decodeResource(context.resources, it, bounds) }
        if (bounds.outWidth > 0 && bounds.outHeight > 0) bounds.outWidth.toFloat() / bounds.outHeight else 1.3f
    }
    val historicalCount = if (move.historicalImageRes != null) 1 else 0
    val count = historicalCount + move.media.size
    val pager = rememberPagerState(pageCount = { count })
    val scope = rememberCoroutineScope()
    var historicalOpen by remember { mutableStateOf(false) }
    if (count > 0) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            HorizontalPager(state = pager, modifier = Modifier.weight(1f), key = { index ->
                if (index < historicalCount) "historical" else move.media[index - historicalCount].path
            }) { index ->
                if (index < historicalCount) AsyncImage(move.historicalImageRes,
                    if (de) "Historische Illustration öffnen" else "Open historical illustration", contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().aspectRatio(historicalRatio).clickable { historicalOpen = true })
                else ExerciseMediaPreview(move.media[index - historicalCount], index + 1, showLabel = false)
            }

        }
        if (pager.currentPage < historicalCount) move.historicalNote?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (count > 1) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(count) { index ->
                    val selected = pager.currentPage == index
                    Box(Modifier.size(52.dp).border(if (selected) 2.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .clickable { scope.launch { pager.animateScrollToPage(index) } }
                        .semantics { contentDescription = if (de) "Medium ${index + 1} anzeigen" else "Show media ${index + 1}"; this.selected = selected }
                        .padding(4.dp), contentAlignment = Alignment.Center) {
                        if (index < historicalCount) AsyncImage(move.historicalImageRes, null, contentScale = ContentScale.Fit)
                        else {
                            val media = move.media[index - historicalCount]
                            if (media.kind == ExerciseMediaKind.IMAGE) MediaImage(media.path, "", Modifier.fillMaxSize())
                            else VideoThumbnail(media.path, Modifier.fillMaxSize())
                        }
                    }
                }
            }
            Text("${pager.currentPage + 1} / $count", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.labelMedium)
        }
    }
    if (historicalOpen) Dialog(onDismissRequest = { historicalOpen = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.safeDrawingPadding().padding(16.dp)) {
                TextButton(onClick = { historicalOpen = false }) { Text(if (de) "Schließen" else "Close") }
                AsyncImage(move.historicalImageRes, move.displayName, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().weight(1f))
            }
        }
    }
}

@Composable
fun categoryLabel(category: MoveCategory): String {
    val s = LocalStrings.current
    return when (category) { MoveCategory.HAU -> s.hau; MoveCategory.HUT -> s.hut; MoveCategory.STICH -> s.stich }
}
