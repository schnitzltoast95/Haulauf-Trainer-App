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
import com.example.myapplication.data.historicalImageUrl
import com.example.myapplication.data.historicalTextUrl
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
    val textUrl = move.historicalTextUrl()
    if (imageUrl == null && textUrl == null) return
    val uriHandler = LocalUriHandler.current
    val german = LocalStrings.current === StringsDe
    Column {
        if (showingHistoricalImage && imageUrl != null) {
            Text(
                "Joachim Meyer, Gründtliche Beschreibung (1570). ${move.historicalNote.orEmpty()}",
                style = MaterialTheme.typography.bodySmall,
                color = HaulaufTextSecondary
            )
            TextButton(onClick = { uriHandler.openUri(imageUrl) }) {
                Text(if (german) "Originalscan · Bayerische Staatsbibliothek ↗" else "Original scan · Bavarian State Library ↗", color = Color.White)
            }
        }
        if (textUrl != null) {
            TextButton(onClick = { uriHandler.openUri(textUrl) }) {
                Text(if (german) "Meyers Originaltext zum Kontext ↗" else "Meyer's original text for context ↗", color = Color.White)
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}
