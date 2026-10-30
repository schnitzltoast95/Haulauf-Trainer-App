package com.example.myapplication.data

data class AudioLevels(val call: Float = 1f, val beep: Float = .75f, val tick: Float = .25f) {
    fun applyTo(preset: TrainingPreset) = preset.copy(volumeCall = call, volumeBeep = beep, volumeTick = tick)
    companion object {
        fun from(preset: TrainingPreset) = AudioLevels(preset.volumeCall, preset.volumeBeep, preset.volumeTick)
    }
}
