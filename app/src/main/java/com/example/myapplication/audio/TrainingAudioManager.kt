package com.example.myapplication.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class TrainingAudioManager(
    private val context: Context,
    private var volumeCall: Float = 1f,
    private var volumeBeep: Float = 0.75f,
    private var volumeTick: Float = 0.25f,
) {
    private var tts: TextToSpeech? = null
    private var toneGenerator: ToneGenerator? = null
    private var soundPool: SoundPool? = null
    private var tickSoundId = 0
    private var windowEndSoundId = 0

    init {
        initTts()
        initToneGenerator()
        initSoundPool()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.GERMAN
            }
        }
    }

    private fun initToneGenerator() {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (_: Exception) {}
    }

    private fun initSoundPool() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build()
        try {
            val resId = context.resources.getIdentifier("tick", "raw", context.packageName)
            if (resId != 0) tickSoundId = soundPool!!.load(context, resId, 1)
        } catch (_: Exception) {}
        try {
            val resId = context.resources.getIdentifier("window_end", "raw", context.packageName)
            if (resId != 0) windowEndSoundId = soundPool!!.load(context, resId, 1)
        } catch (_: Exception) {}
    }

    fun setVolumes(call: Float, beep: Float, tick: Float) {
        volumeCall = call
        volumeBeep = beep
        volumeTick = tick
    }

    suspend fun playMoveAudio(move: com.example.myapplication.data.Move, overrideUri: String?) {
        val uri = overrideUri
        if (uri != null) {
            playFromUri(uri, volumeCall)
        } else {
            playMoveCall(move.displayName)
        }
    }

    private suspend fun playFromUri(uriString: String, volume: Float) = suspendCancellableCoroutine { cont ->
        try {
            val mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(context, android.net.Uri.parse(uriString))
                setOnCompletionListener {
                    if (cont.isActive) cont.resume(Unit)
                    release()
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    if (cont.isActive) cont.resume(Unit)
                    true
                }
                setVolume(volume, volume)
                prepare()
                start()
            }
            cont.invokeOnCancellation {
                try {
                    mediaPlayer.stop()
                } catch (_: Exception) { }
                mediaPlayer.release()
            }
        } catch (_: Exception) {
            if (cont.isActive) cont.resume(Unit)
        }
    }

    suspend fun playMoveCall(text: String) = suspendCancellableCoroutine { cont ->
        val t = tts ?: run {
            cont.resume(Unit)
            return@suspendCancellableCoroutine
        }
        t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) {
                if (cont.isActive) cont.resume(Unit)
            }
            override fun onError(id: String?) {
                if (cont.isActive) cont.resume(Unit)
            }
        })
        val params = android.os.Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volumeCall)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            t.speak(text, TextToSpeech.QUEUE_FLUSH, params, "move_${System.currentTimeMillis()}")
        } else {
            @Suppress("DEPRECATION")
            t.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    fun playEndBeep() {
        if (windowEndSoundId != 0) {
            soundPool?.play(windowEndSoundId, volumeBeep, volumeBeep, 1, 0, 1f)
        } else {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
        }
    }

    /** Short beep for countdown (3-2-1 at start and during round pause). */
    fun playCountdownBeep() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
    }

    fun playMetronomeTick() {
        if (tickSoundId != 0) {
            soundPool?.play(tickSoundId, volumeTick, volumeTick, 1, 0, 1f)
        } else {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 50)
        }
    }

    fun stopAll() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        toneGenerator?.release()
        toneGenerator = null
        soundPool?.release()
        soundPool = null
    }
}
