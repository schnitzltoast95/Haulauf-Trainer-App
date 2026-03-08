package com.example.myapplication.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
                imagePath = info?.second ?: move.imagePath
            )
        }
        val customWithInfo = custom.map { move ->
            val info = infoMap[move.id]
            move.copy(
                description = info?.first ?: move.description,
                imagePath = info?.second ?: move.imagePath
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
        }
    }

    fun saveTtsSpeechRate(rate: Float) {
        viewModelScope.launch {
            prefs.saveTtsSpeechRate(rate)
            audioManager.setTtsRate(rate)
        }
    }

    fun saveUiLanguage(lang: String) {
        viewModelScope.launch {
            prefs.saveUiLanguage(lang)
        }
    }

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
        app.startForegroundService(Intent(app, TrainingForegroundService::class.java))
        audioManager.setVolumes(p.volumeCall, p.volumeBeep, p.volumeTick)
        val overridesSnapshot = moveOverrides.value
        val movesSnapshot = allMoves.value
        engine = TrainingEngine(
            scope = viewModelScope,
            preset = p,
            availableMoves = movesSnapshot,
            onPlayMove = { move ->
                audioManager.playMoveAudio(move, overridesSnapshot[move.id])
            },
            onPlayEndBeep = { audioManager.playEndBeep() },
            onPlayCountdownBeep = { audioManager.playCountdownBeep() },
            onPlayMetronomeTick = { audioManager.playMetronomeTick() },
            onStopMetronome = { audioManager.stopAll() }
        )
        viewModelScope.launch {
            engine!!.state.collect { _trainingState.value = it }
        }
        engine!!.start()
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
        getApplication<Application>().stopService(Intent(getApplication(), TrainingForegroundService::class.java))
    }

    fun restartTraining() {
        stopTraining()
        startTraining(preset.value) {}
    }

    fun getMoveOverride(moveId: String): String? = moveOverrides.value[moveId]

    fun addCustomMove(displayName: String, category: MoveCategory) {
        viewModelScope.launch {
            prefs.addCustomMove(displayName, category)
        }
    }

    fun removeCustomMove(moveId: String) {
        viewModelScope.launch {
            prefs.removeCustomMove(moveId)
            prefs.saveMoveAudioOverride(moveId, null)
            preset.value = preset.value.copy(
                selectedMoveIds = preset.value.selectedMoveIds - moveId,
                movePriorities = preset.value.movePriorities - moveId
            )
            prefs.saveLastUsedPreset(preset.value)
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
            audioManager.playMoveAudio(move, override)
        }
    }

    fun savePreset(name: String, presetId: String? = null) {
        viewModelScope.launch {
            val savedPreset = SavedPreset(
                id = presetId ?: java.util.UUID.randomUUID().toString(),
                name = name,
                preset = preset.value
            )
            prefs.savePreset(savedPreset)
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

    fun saveMoveInfo(moveId: String, description: String?, imagePath: String?) {
        viewModelScope.launch {
            prefs.saveMoveInfo(moveId, description, imagePath)
        }
    }

    override fun onCleared() {
        audioManager.release()
    }
}
