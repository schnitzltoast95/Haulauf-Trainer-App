package com.example.myapplication.ui

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AudioLevels
import com.example.myapplication.data.DEFAULT_MOVES
import com.example.myapplication.data.MoveCategory
import com.example.myapplication.data.PreferencesRepository
import com.example.myapplication.data.SavedPreset
import com.example.myapplication.data.TrainingPreset
import com.example.myapplication.data.Move
import com.example.myapplication.service.TrainingForegroundService
import com.example.myapplication.training.TrainingEngine
import com.example.myapplication.training.TrainingState
import com.example.myapplication.audio.TrainingAudioManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HaulaufViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesRepository(application)
    private val audioManager = TrainingAudioManager(
        application,
        volumeCall = 1f,
        volumeBeep = 0.75f,
        volumeTick = 0.25f
    )

    private var engine: TrainingEngine? = null
    private var engineStateJob: Job? = null
    private var userEditedPreset = false

    val preset = MutableStateFlow(TrainingPreset())
    private val _trainingState = MutableStateFlow<TrainingState>(TrainingState.Idle)
    val trainingState: StateFlow<TrainingState> = _trainingState.asStateFlow()

    val lastUsedPreset = prefs.lastUsedPreset.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )
    val moveOverrides = prefs.moveAudioOverrides.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    val moveInfo = prefs.moveInfo.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    val allMoves = combine(prefs.customMoves, moveInfo) { custom, infoMap ->
        val defaultWithInfo = DEFAULT_MOVES.map { move ->
            val info = infoMap[move.id]
            move.copy(
                description = info?.first ?: move.description,
                imagePath = if (info != null) info.second else move.imagePath
            )
        }
        val customWithInfo = custom.map { move ->
            val info = infoMap[move.id]
            move.copy(
                description = info?.first ?: move.description,
                imagePath = if (info != null) info.second else move.imagePath
            )
        }
        defaultWithInfo + customWithInfo
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DEFAULT_MOVES
    )

    val savedPresets = prefs.savedPresets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val ttsSpeechRate = prefs.ttsSpeechRate.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        1.4f
    )

    val ttsVoiceName = prefs.ttsVoiceName.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val uiLanguage = prefs.uiLanguage.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "de"
    )

    init {
        viewModelScope.launch {
            val storedPreset = prefs.getLastUsedPreset()
            if (!userEditedPreset && storedPreset != null) {
                preset.value = storedPreset
            }
            audioManager.setTtsRate(prefs.getTtsSpeechRate())
            audioManager.setTtsVoice(prefs.getTtsVoiceName())
        }
    }

    fun saveTtsSpeechRate(rate: Float) {
        viewModelScope.launch {
            prefs.saveTtsSpeechRate(rate)
            audioManager.setTtsRate(rate)
        }
    }

    fun saveTtsVoiceName(name: String?) {
        viewModelScope.launch {
            prefs.saveTtsVoiceName(name)
            audioManager.setTtsVoice(name)
        }
    }

    fun saveUiLanguage(lang: String) {
        viewModelScope.launch {
            prefs.saveUiLanguage(lang)
        }
    }

    val globalAudio = prefs.globalAudio.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    fun saveGlobalAudio(levels: AudioLevels?) { viewModelScope.launch { prefs.saveGlobalAudio(levels) } }

    fun updatePreset(newPreset: TrainingPreset) {
        userEditedPreset = true
        preset.value = newPreset
        viewModelScope.launch {
            prefs.saveLastUsedPreset(newPreset)
        }
    }

    fun updateAdvancedPreset(newPreset: TrainingPreset) {
        updatePreset(newPreset)
    }

    fun startQuickstart(onNavigate: () -> Unit) {
        viewModelScope.launch {
            val p = prefs.getLastUsedPreset() ?: preset.value
            preset.value = p
            startTraining(p, onNavigate)
        }
    }

    fun savePresetAndStart(onNavigate: () -> Unit) {
        viewModelScope.launch {
            prefs.saveLastUsedPreset(preset.value)
            startTraining(preset.value, onNavigate)
        }
    }

    private fun startTraining(p: TrainingPreset, onNavigate: () -> Unit) {
        val app = getApplication<Application>()
        val movesSnapshot = allMoves.value
        if (movesSnapshot.none { it.id in p.selectedMoveIds }) return

        engine?.stop()
        engineStateJob?.cancel()
        ContextCompat.startForegroundService(app, Intent(app, TrainingForegroundService::class.java))
        val levels = globalAudio.value ?: AudioLevels.from(p)
        audioManager.setVolumes(levels.call, levels.beep, levels.tick)
        val overridesSnapshot = moveOverrides.value
        val trainingEngine = TrainingEngine(
            scope = viewModelScope,
            preset = p,
            availableMoves = movesSnapshot,
            onPlayMove = { move ->
                audioManager.playMoveAudio(move, overridesSnapshot[move.id])
            },
            onPlayEndBeep = { audioManager.playEndBeep() },
            onPlayCountdownBeep = { secondsRemaining -> audioManager.playCountdownBeep(secondsRemaining) },
            onPlayRoundFinishedSignal = { audioManager.playRoundFinishedSignal() },
            onPlayTrainingFinishedSignal = { audioManager.playTrainingFinishedSignal() },
            onPlayMetronomeTick = { audioManager.playMetronomeTick() },
            onStopMetronome = { audioManager.stopMetronome() }
        )
        engine = trainingEngine
        engineStateJob = viewModelScope.launch {
            trainingEngine.state.collect { state ->
                _trainingState.value = state
                if (state is TrainingState.Finished) {
                    stopTrainingService()
                }
            }
        }
        trainingEngine.start()
        onNavigate()
    }

    fun pauseTraining() {
        engine?.pause()
    }

    fun resumeTraining() {
        engine?.resume()
    }

    fun stopTraining() {
        engine?.stop()
        engine = null
        engineStateJob?.cancel()
        engineStateJob = null
        _trainingState.value = TrainingState.Idle
        audioManager.stopAll()
        stopTrainingService()
    }

    fun restartTraining() {
        stopTraining()
        startTraining(preset.value) {}
    }

    fun getMoveOverride(moveId: String): String? = moveOverrides.value[moveId]

    fun addCustomMove(displayName: String, category: MoveCategory, onAdded: (String) -> Unit = {}) {
        viewModelScope.launch {
            val move = prefs.addCustomMove(displayName, category)
            onAdded(move.id)
        }
    }

    fun removeCustomMove(moveId: String, onDeleted: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                prefs.removeCustomMove(moveId)
                preset.value = preset.value.copy(
                    selectedMoveIds = preset.value.selectedMoveIds - moveId,
                    movePriorities = preset.value.movePriorities - moveId
                )
                onDeleted()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) { onError() }
        }
    }

    fun renameCustomMove(moveId: String, newTitle: String) {
        viewModelScope.launch {
            prefs.updateCustomMoveTitle(moveId, newTitle)
        }
    }

    fun saveMoveOverride(moveId: String, uri: String?) {
        viewModelScope.launch {
            prefs.saveMoveAudioOverride(moveId, uri)
        }
    }

    fun testMoveAudio(moveId: String) {
        viewModelScope.launch {
            val move = allMoves.value.find { it.id == moveId } ?: return@launch
            val override = moveOverrides.value[moveId]
            val levels = globalAudio.value ?: AudioLevels.from(preset.value)
            audioManager.setVolumes(levels.call, levels.beep, levels.tick)
            audioManager.playMoveAudio(move, override)
        }
    }

    fun savePreset(name: String, presetId: String? = null, value: TrainingPreset = preset.value, onSaved: () -> Unit = {}, onError: () -> Unit = {}) {
        viewModelScope.launch {
            val savedPreset = SavedPreset(
                id = presetId ?: java.util.UUID.randomUUID().toString(),
                name = name,
                preset = value
            )
            try { prefs.savePreset(savedPreset); onSaved() }
            catch (_: Exception) { onError() }
        }
    }

    fun renamePreset(presetId: String, name: String, onSaved: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try { prefs.renamePreset(presetId, name); onSaved() }
            catch (_: Exception) { onError() }
        }
    }

    fun deletePreset(presetId: String) {
        viewModelScope.launch {
            prefs.deletePreset(presetId)
        }
    }

    fun loadPreset(savedPreset: SavedPreset) {
        preset.value = savedPreset.preset
    }

    fun saveExercise(move: Move, audioUri: String?, onSaved: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try { prefs.saveExercise(move, audioUri); onSaved() }
            catch (_: Exception) { onError() }
        }
    }

    fun previewExercise(move: Move, audioUri: String?) {
        viewModelScope.launch {
            val levels = globalAudio.value ?: AudioLevels.from(preset.value)
            audioManager.setVolumes(levels.call, levels.beep, levels.tick)
            audioManager.playMoveAudio(move, audioUri)
        }
    }

    fun saveMoveInfo(moveId: String, description: String?, imagePath: String?) {
        viewModelScope.launch {
            prefs.saveMoveInfo(moveId, description, imagePath)
        }
    }

    override fun onCleared() {
        stopTrainingService()
        audioManager.release()
    }

    private fun stopTrainingService() {
        val app = getApplication<Application>()
        app.stopService(Intent(app, TrainingForegroundService::class.java))
    }
}
