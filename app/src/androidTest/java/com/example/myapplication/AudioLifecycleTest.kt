package com.example.myapplication

import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.audio.AsyncUriPlayer
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class AudioLifecycleTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun validCorruptMissingAndCancelledPlaybackReleasePlayer() = runBlocking {
        val player = AsyncUriPlayer(context)
        val corrupt = File(context.cacheDir, "audit-corrupt.wav").apply { writeText("not audio") }
        val good = "android.resource://${context.packageName}/${R.raw.training_finished}"
        try {
            withTimeout(15000) {
                assertFalse(player.play(corrupt.toURI().toString(), 0f))
                assertFalse(player.play(File(context.cacheDir, "missing-audit.wav").toURI().toString(), 0f))
                assertTrue(player.play(good, 0f))
                // Cancel before preparation completes, then prove that the next request still works.
                withContext(Dispatchers.Main.immediate) {
                    val job = launch(start = CoroutineStart.UNDISPATCHED) { player.play(good, 0f) }
                    job.cancelAndJoin()
                }
                assertTrue(player.play(good, 0f))
                withContext(Dispatchers.Main.immediate) {
                    val job = launch(start = CoroutineStart.UNDISPATCHED) { player.play(good, 0f) }
                    delay(50)
                    player.stop()
                    job.join()
                    assertTrue(job.isCancelled)
                }
                assertTrue(player.play(good, 0f))
            }
        } finally { player.stop(); corrupt.delete() }
    }
}
