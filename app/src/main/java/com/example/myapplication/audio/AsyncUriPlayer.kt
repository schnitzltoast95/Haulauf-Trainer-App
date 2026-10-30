package com.example.myapplication.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/** All player state lives on Main; decoding/preparation is asynchronous. */
internal class AsyncUriPlayer(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private var cancelPlayback: (() -> Unit)? = null

    suspend fun play(uri: String, volume: Float): Boolean = withContext(Dispatchers.Main.immediate) {
        stop()
        suspendCancellableCoroutine { continuation ->
            var player: MediaPlayer? = null
            var finished = false
            fun finish(success: Boolean, cancelled: Boolean = false) {
                if (finished) return
                finished = true
                cancelPlayback = null
                player?.apply {
                    setOnPreparedListener(null)
                    setOnCompletionListener(null)
                    setOnErrorListener(null)
                    release()
                }
                player = null
                if (cancelled) continuation.cancel()
                else if (continuation.isActive) continuation.resume(success)
            }
            cancelPlayback = { finish(false, cancelled = true) }
            continuation.invokeOnCancellation {
                if (Looper.myLooper() == Looper.getMainLooper()) finish(false, cancelled = true)
                else handler.post { finish(false, cancelled = true) }
            }
            if (!continuation.isActive) return@suspendCancellableCoroutine
            try {
                player = MediaPlayer()
                player!!.apply {
                    setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                    setOnPreparedListener {
                        if (!finished && continuation.isActive) {
                            try { it.start() } catch (_: Exception) { finish(false) }
                        }
                    }
                    setOnCompletionListener { finish(true) }
                    setOnErrorListener { _, _, _ -> finish(false); true }
                    setVolume(volume, volume)
                    setDataSource(context, uri.toUri())
                    prepareAsync()
                }
            } catch (_: Exception) { finish(false) }
        }
    }

    fun stop() {
        if (Looper.myLooper() == Looper.getMainLooper()) cancelPlayback?.invoke()
        else handler.post { cancelPlayback?.invoke() }
    }
}
