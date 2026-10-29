package com.example.myapplication.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.core.net.toUri
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.myapplication.data.*
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseEditor(
    moveId: String,
    allMoves: List<Move>,
    overrideUri: String?,
    savedPresets: List<SavedPreset>,
    onSave: (Move, String?, () -> Unit, () -> Unit) -> Unit,
    onPreview: (Move, String?) -> Unit,
    onDelete: (String, () -> Unit, () -> Unit) -> Unit,
    onBack: () -> Unit
) {
    // Keep the open draft stable while repository updates arrive, including deletion.
    val move = remember(moveId) { allMoves.find { it.id == moveId } }
    val context = LocalContext.current
    val focus = androidx.compose.ui.platform.LocalFocusManager.current
    val s = LocalStrings.current
    val de = s === StringsDe
    if (move == null) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text(if (de) "Diese Übung ist nicht mehr verfügbar." else "This exercise is no longer available.")
            TextButton(onClick = onBack) { Text(s.back) }
        }
        return
    }
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current
    val custom = moveId.startsWith("custom_")
    var baseline by rememberSaveable(moveId) { mutableStateOf(move) }
    var baselineAudio by rememberSaveable(moveId) { mutableStateOf(overrideUri) }
    var title by rememberSaveable(moveId) { mutableStateOf(move.displayName) }
    var category by rememberSaveable(moveId) { mutableStateOf(move.category) }
    var description by rememberSaveable(moveId) { mutableStateOf(move.description.orEmpty()) }
    var imagePath by rememberSaveable(moveId) { mutableStateOf(move.imagePath) }
    var audioUri by rememberSaveable(moveId) { mutableStateOf(overrideUri) }
    var advanced by remember { mutableStateOf(false) }
    var confirmExit by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var importing by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageError by remember { mutableStateOf(false) }
    fun status(text: String?, error: Boolean = false) { message = text; messageError = error }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    val stagedFiles = rememberSaveable(moveId, saver = listSaver<MutableList<File>, String>(
        save = { it.map(File::getAbsolutePath) }, restore = { it.map(::File).toMutableList() }
    )) { mutableListOf<File>() }
    val draft = move.copy(displayName = title.trim(), category = category, description = description, imagePath = imagePath)
    val dirty = draft != baseline.copy(description = baseline.description.orEmpty()) || audioUri != baselineAudio
    val imageModel = moveImageModel(context, imagePath, move.historicalImageRes)
    val back: () -> Unit = { if (!saving && !importing && !deleting) { if (dirty || recorder != null) confirmExit = true else onBack() } }
    BackHandler(onBack = back)

    fun stopRecording(discard: Boolean = false) {
        val active = recorder ?: return
        var valid = true
        try { active.stop() } catch (_: RuntimeException) { valid = false }
        active.release()
        recorder = null
        if (valid && !discard) {
            audioUri = recordingFile?.let { Uri.fromFile(it).toString() }
            status(if (de) "Aufnahme bereit. Mit Speichern übernehmen." else "Recording ready. Save to apply.")
        }
        else {
            recordingFile?.delete()
            if (!discard) status(if (de) "Aufnahme zu kurz. Bitte erneut aufnehmen." else "Recording too short. Please try again.", true)
        }
    }
    fun startRecording() {
        val dir = File(context.filesDir, "exercise-media").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.m4a")
        stagedFiles.add(file)
        var active: MediaRecorder? = null
        try {
            active = MediaRecorder()
            active.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare(); start()
            }
            recorder = active
            recordingFile = file
            status(if (de) "Aufnahme läuft …" else "Recording …")
        } catch (_: Exception) {
            active?.release(); file.delete()
            status(if (de) "Aufnahme nicht möglich. Mikrofonzugriff prüfen oder Audiodatei wählen." else "Could not record. Check microphone access or choose an audio file.", true)
        }
    }
    val microphone = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRecording()
        else status(if (de) "Mikrofon nicht freigegeben. Du kannst eine Audiodatei wählen." else "Microphone permission denied. You can choose an audio file.", true)
    }
    fun importMedia(uri: Uri, image: Boolean) {
        importing = true
        scope.launch {
            val dir = File(context.filesDir, "exercise-media").apply { mkdirs() }
            val file = File(dir, UUID.randomUUID().toString())
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri).use { input ->
                        requireNotNull(input)
                        file.outputStream().use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            while (true) {
                                coroutineContext.ensureActive()
                                val count = input.read(buffer)
                                if (count < 0) break
                                output.write(buffer, 0, count)
                            }
                        }
                    }
                    require(file.length() > 0)
                    if (image) {
                        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        android.graphics.BitmapFactory.decodeFile(file.absolutePath, bounds)
                        require(bounds.outWidth > 0 && bounds.outHeight > 0)
                    } else {
                        val metadata = android.media.MediaMetadataRetriever()
                        try {
                            metadata.setDataSource(file.absolutePath)
                            require(metadata.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) == "yes")
                        } finally { metadata.release() }
                    }
                }
                stagedFiles.add(file)
                if (image) imagePath = file.absolutePath else audioUri = Uri.fromFile(file).toString()
                status(if (de) "Ausgewählt – noch nicht gespeichert." else "Selected – not saved yet.")
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                file.delete()
                throw cancelled
            } catch (_: Exception) {
                file.delete()
                status(if (de) "Datei nicht lesbar oder Format nicht unterstützt. Bitte eine andere Datei wählen." else "File unreadable or format unsupported. Please choose another file.", true)
            } finally {
                importing = false
            }
        }
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { importMedia(it, true) } }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { importMedia(it, false) } }
    val stopOnBackground by rememberUpdatedState { stopRecording() }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) stopOnBackground()
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(moveId) {
        onDispose {
            recorder?.let { try { it.stop() } catch (_: Exception) {}; it.release() }
            var owner: android.content.Context = context
            while (owner is android.content.ContextWrapper && owner !is android.app.Activity) owner = owner.baseContext
            if ((owner as? android.app.Activity)?.isChangingConfigurations != true) stagedFiles.forEach { it.delete() }
        }
    }
    fun save(leave: Boolean = false) {
        if (saving || importing || deleting || recorder != null || title.isBlank()) return
        confirmExit = false
        saving = true
        onSave(draft, audioUri, {
            saving = false
            val kept = setOf(imagePath, audioUri?.let { it.toUri().path })
            stagedFiles.filter { it.absolutePath !in kept }.forEach { it.delete() }
            stagedFiles.clear()
            focus.clearFocus()
            baseline = draft; baselineAudio = audioUri
            status(if (de) "Änderungen gespeichert." else "Changes saved.")
            if (leave) onBack()
        }, {
            saving = false
            status(if (de) "Speichern fehlgeschlagen. Deine Änderungen bleiben im Editor. Bitte erneut speichern." else "Save failed. Your changes remain in the editor. Please save again.", true)
        })
    }
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = { TopAppBar(title = { Text(if (custom) { if (de) "Eigene Übung" else "Custom exercise" } else move.displayName) },
            navigationIcon = { IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back) } }) },
        bottomBar = {
            Surface { Button(onClick = { save() }, enabled = dirty && title.isNotBlank() && !saving && recorder == null,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).heightIn(min = 56.dp)) {
                Text(if (saving) { if (de) "Wird gespeichert …" else "Saving …" } else s.save)
            } }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (custom) {
                OutlinedTextField(value = title, onValueChange = { title = it; status(null) }, label = { Text(s.newMoveName) }, singleLine = true,
                    isError = title.isBlank(), supportingText = { if (title.isBlank()) Text(if (de) "Bitte gib der Übung einen Namen." else "Please name this exercise.") }, modifier = Modifier.fillMaxWidth())
                val categoryChoices: @Composable () -> Unit = {
                    MoveCategory.entries.forEach { option ->
                        FilterChip(selected = category == option, onClick = { category = option; status(null) }, label = {
                            Text(when (option) { MoveCategory.HAU -> s.hau; MoveCategory.HUT -> s.hut; MoveCategory.STICH -> s.stich })
                        })
                    }
                }
                BoxWithConstraints {
                    if (maxWidth < 320.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.5f) {
                        Column { categoryChoices() }
                    } else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { categoryChoices() }
                }
            } else Text(if (de) "Standardübung · eigene Anpassungen sind wiederherstellbar" else "Standard exercise · customizations can be reset", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(value = description, onValueChange = { description = it; status(null) }, label = { Text(s.descriptionFieldLabel) }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = { onPreview(draft, audioUri) }, enabled = title.isNotBlank() && recorder == null, modifier = Modifier.fillMaxWidth()) { Text(s.testPlayback) }
            Text(if (audioUri == null) s.defaultAudioEnabled else s.customAudioEnabled, style = MaterialTheme.typography.bodySmall)
            message?.let { Text(it, color = if (messageError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
            OutlinedButton(onClick = { advanced = !advanced }, modifier = Modifier.fillMaxWidth()) {
                Text(if (advanced) { if (de) "Medien schließen" else "Hide media" } else { if (de) "Audio und Bild anpassen" else "Customize audio and image" })
            }
            if (advanced) {
                Text(s.audio, style = MaterialTheme.typography.titleMedium)
                Button(onClick = {
                    if (recorder != null) stopRecording()
                    else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRecording()
                    else microphone.launch(Manifest.permission.RECORD_AUDIO)
                }, modifier = Modifier.fillMaxWidth()) { Text(if (recorder != null) s.stopRecording else s.newRecording) }
                OutlinedButton(onClick = { audioPicker.launch(arrayOf("audio/*")) }, enabled = recorder == null, modifier = Modifier.fillMaxWidth()) { Text(s.chooseFile) }
                TextButton(onClick = { audioUri = null; status(null) }, enabled = audioUri != null && recorder == null) { Text(s.resetToDefault) }
                if (imageModel != null) AsyncImage(imageModel, s.image, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp))
                OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, enabled = recorder == null, modifier = Modifier.fillMaxWidth()) { Text(s.chooseImage) }
                if (imagePath != null) TextButton(onClick = { imagePath = null; status(null) }) { Text(s.removeImage) }
            }
            MoveHistoricalAttribution(move, showingHistoricalImage = advanced && imageModel != null && imageModel == move.historicalImageRes)
            if (!custom) TextButton(onClick = {
                val original = DEFAULT_MOVES.first { it.id == moveId }
                description = original.description.orEmpty(); imagePath = null; audioUri = null
                status(if (de) "Original vorbereitet. Mit Speichern übernehmen." else "Original restored in draft. Save to apply.")
            }) { Text(if (de) "Original wiederherstellen" else "Restore original") }
            else TextButton(onClick = { confirmDelete = true }, enabled = !saving && recorder == null) { Text(s.deleteMoveAction, color = MaterialTheme.colorScheme.error) }
        }
    }
    if (saving) AlertDialog(onDismissRequest = {}, title = { Text(if (de) "Wird gespeichert …" else "Saving …") },
        text = { CircularProgressIndicator() }, confirmButton = {})
    if (importing) AlertDialog(onDismissRequest = {}, title = { Text(if (de) "Datei wird geprüft …" else "Checking file …") },
        text = { CircularProgressIndicator() }, confirmButton = {})
    if (confirmExit) AlertDialog(onDismissRequest = { confirmExit = false },
        title = { Text(if (de) "Änderungen speichern?" else "Save changes?") },
        text = { Text(if (recorder != null) { if (de) "Beende zuerst die Aufnahme, um sie zu speichern." else "Stop recording before saving." } else { if (de) "Text, Bild und Audio werden gemeinsam gespeichert." else "Text, image and audio are saved together." }) },
        confirmButton = { TextButton(enabled = title.isNotBlank() && recorder == null && !saving, onClick = { save(true) }) { Text(s.save) } },
        dismissButton = { TextButton(enabled = !saving, onClick = { stopRecording(true); confirmExit = false; onBack() }) { Text(if (de) "Verwerfen" else "Discard") } })
    if (confirmDelete) {
        val affected = savedPresets.filter { moveId in it.preset.selectedMoveIds }
        AlertDialog(onDismissRequest = { if (!deleting) confirmDelete = false }, title = { Text(s.deleteMoveTitle) },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(s.deleteMoveConfirm.format(move.displayName))
                if (affected.isNotEmpty()) Text((if (de) "Wird auch aus diesen Trainings entfernt: " else "Also removed from these trainings: ") + affected.joinToString { it.name })
                if (deleteError) Text(if (de) "Löschen fehlgeschlagen. Bitte erneut versuchen." else "Delete failed. Please try again.", color = MaterialTheme.colorScheme.error)
            } },
            confirmButton = { TextButton(enabled = !deleting, onClick = {
                deleting = true; deleteError = false
                onDelete(moveId, { deleting = false; confirmDelete = false; onBack() }, { deleting = false; deleteError = true })
            }) { Text(if (deleting) { if (de) "Wird gelöscht …" else "Deleting …" } else s.delete, color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(enabled = !deleting, onClick = { confirmDelete = false; deleteError = false }) { Text(s.cancel) } })
    }
}
