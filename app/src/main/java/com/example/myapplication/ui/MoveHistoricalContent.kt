package com.example.myapplication.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Move
import com.example.myapplication.data.MEYER_IMAGE_RIGHTS_URL
import com.example.myapplication.data.historicalImageUrl
import com.example.myapplication.ui.theme.HaulaufTextSecondary
import java.io.File

fun moveImageModel(context: Context, imagePath: String?, historicalImageRes: Int?): Any? = when {
    imagePath?.startsWith("content://") == true || imagePath?.startsWith("file://") == true -> Uri.parse(imagePath)
    !imagePath.isNullOrBlank() && File(imagePath).exists() -> File(imagePath)
    else -> historicalImageRes
}

@Composable
fun MoveHistoricalAttribution(move: Move, showingHistoricalImage: Boolean) {
    val imageUrl = move.historicalImageUrl()
    if (imageUrl == null && move.historicalTextSources.isEmpty()) return
    val uriHandler = LocalUriHandler.current
    val german = LocalStrings.current === StringsDe
    Column {
        // This qualification also applies when the user supplies their own image
        // or description; it must not disappear with the image attribution.
        if (move.historicalContextOnly) {
            Text(
                if (german) "Moderner Übungsname der App. In den geprüften Schwertpassagen nicht als eigene Technik belegt; die Quellen dienen nur als Kontext."
                else "Modern app drill name. Not established as a separate technique in the reviewed sword passages; the sources provide context only.",
                style = MaterialTheme.typography.bodySmall,
                color = HaulaufTextSecondary
            )
        }
        if (showingHistoricalImage && imageUrl != null) {
            Text(
                "Joachim Meyer, Gründtliche Beschreibung (1570). Bayerische Staatsbibliothek, Res/4 Gymn. 26 t. ${move.historicalNote.orEmpty()}",
                style = MaterialTheme.typography.bodySmall,
                color = HaulaufTextSecondary
            )
            TextButton(onClick = { uriHandler.openUri(imageUrl) }) {
                Text(if (german) "Originalscan · Bayerische Staatsbibliothek ↗" else "Original scan · Bavarian State Library ↗", color = Color.White)
            }
            TextButton(onClick = { uriHandler.openUri(MEYER_IMAGE_RIGHTS_URL) }) {
                Text("Public Domain Mark 1.0 ↗", color = Color.White)
            }
        }
        move.historicalTextSources.forEach { source ->
            Text(
                "Meyer (1570), ${source.passage}",
                style = MaterialTheme.typography.bodySmall,
                color = HaulaufTextSecondary
            )
            TextButton(onClick = { uriHandler.openUri(source.originalUrl()) }) {
                Text(if (german) "Originaltext · BSB ↗" else "Original text · BSB ↗", color = Color.White)
            }
            TextButton(onClick = { uriHandler.openUri(source.transcriptionUrl()) }) {
                Text(if (german) "Transkription · Wiktenauer ↗" else "Transcription · Wiktenauer ↗", color = Color.White)
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}
