package com.example.myapplication.data

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Copy first, validate, then expose. No dependency on a document provider after import. */
suspend fun importExerciseMedia(context: Context, uri: Uri, visual: Boolean): ExerciseMedia = withContext(Dispatchers.IO) {
    val directory = File(context.filesDir, "exercise-media").apply { check(mkdirs() || isDirectory) }
    val file = File(directory, UUID.randomUUID().toString())
    try {
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input)
            file.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var size = 0L
                while (true) {
                    coroutineContext.ensureActive()
                    val count = input.read(buffer)
                    if (count < 0) break
                    size += count
                    require(size <= 200L * 1024 * 1024)
                    output.write(buffer, 0, count)
                }
                require(size > 0)
            }
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        if (visual) BitmapFactory.decodeFile(file.absolutePath, bounds)
        val kind = if (visual && bounds.outWidth > 0 && bounds.outHeight > 0) ExerciseMediaKind.IMAGE else {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(file.absolutePath)
                val key = if (visual) MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO else MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO
                require(retriever.extractMetadata(key) == "yes")
            } finally { retriever.release() }
            ExerciseMediaKind.VIDEO
        }
        coroutineContext.ensureActive()
        ExerciseMedia(file.absolutePath, kind)
    } catch (error: Throwable) {
        file.delete()
        throw error
    }
}
