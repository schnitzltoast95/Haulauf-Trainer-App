package com.example.myapplication.ui

import com.example.myapplication.R
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun BugReportOverlay(context: () -> String, language: String) {
    val localContext = LocalContext.current
    if (localContext.getString(R.string.bug_report_token).isBlank()) return
    val german = language != "en"
    var open by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reportContext by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var sent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val placement = remember(localContext) {
        localContext.getSharedPreferences("bug_report_button", android.content.Context.MODE_PRIVATE)
    }
    var compact by remember(placement) { mutableStateOf(placement.getBoolean("compact", true)) }
    fun setCompact(value: Boolean) { compact = value; placement.edit().putBoolean("compact", value).apply() }
    var dockEdge by remember(placement) {
        mutableStateOf(placement.getString("edge", null)?.takeIf { it in listOf("left", "right", "top", "bottom") }
            ?: if (placement.getBoolean("right", true)) "right" else "left")
    }
    var horizontalFraction by remember(placement) {
        mutableStateOf(placement.getFloat("horizontal_fraction", 0.5f).coerceIn(0f, 1f))
    }
    var verticalFraction by remember(placement) {
        mutableStateOf(placement.getFloat("vertical_fraction", 0.5f).coerceIn(0f, 1f))
    }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var buttonSize by remember { mutableStateOf(IntSize.Zero) }
    var dragTarget by remember { mutableStateOf<Offset?>(null) }
    val marginPx = with(LocalDensity.current) { 12.dp.toPx() }
    val edgeMargin = if (compact) 0f else marginPx
    val maxX = (viewportSize.width - buttonSize.width - edgeMargin).coerceAtLeast(edgeMargin)
    val maxY = (viewportSize.height - buttonSize.height - edgeMargin).coerceAtLeast(edgeMargin)
    val dockTarget = Offset(
        x = when (dockEdge) { "left" -> edgeMargin; "right" -> maxX; else -> edgeMargin + (maxX - edgeMargin) * horizontalFraction },
        y = when (dockEdge) { "top" -> edgeMargin; "bottom" -> maxY; else -> edgeMargin + (maxY - edgeMargin) * verticalFraction }
    )
    val animatedPosition by animateOffsetAsState(
        targetValue = dragTarget ?: dockTarget,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow),
        label = "Bug report button position"
    )
    val currentPosition by rememberUpdatedState(animatedPosition)
    val scale by animateFloatAsState(
        targetValue = if (dragTarget != null) 1.06f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "Bug report button drag feedback"
    )

    Box(
        Modifier.fillMaxSize().safeDrawingPadding().onSizeChanged { viewportSize = it }
    ) {
        Surface(
            shape = if (compact) RoundedCornerShape(
                topStart = if (dockEdge == "left" || dockEdge == "top") 0.dp else 16.dp,
                bottomStart = if (dockEdge == "left" || dockEdge == "bottom") 0.dp else 16.dp,
                topEnd = if (dockEdge == "right" || dockEdge == "top") 0.dp else 16.dp,
                bottomEnd = if (dockEdge == "right" || dockEdge == "bottom") 0.dp else 16.dp
            ) else RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = {
                reportContext = context()
                feedback = null
                open = true
            },
            modifier = Modifier
                .offset { IntOffset(animatedPosition.x.roundToInt(), animatedPosition.y.roundToInt()) }
                .then(if (compact) Modifier.size(48.dp) else Modifier.heightIn(min = 48.dp))
                .onSizeChanged { buttonSize = it }
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .semantics {
                    contentDescription = if (german) "Fehler melden. Zum Verschieben ziehen."
                    else "Report bug. Drag to move."
                }
                .pointerInput(viewportSize, buttonSize, marginPx, edgeMargin) {
                    detectDragGestures(
                        onDragStart = { dragTarget = currentPosition },
                        onDragEnd = {
                            dragTarget?.let { position ->
                                dockEdge = listOf(
                                    "left" to (position.x - edgeMargin), "right" to (maxX - position.x),
                                    "top" to (position.y - edgeMargin), "bottom" to (maxY - position.y)
                                ).minBy { it.second }.first
                                horizontalFraction = if (maxX > edgeMargin) ((position.x - edgeMargin) / (maxX - edgeMargin)).coerceIn(0f, 1f) else 0.5f
                                verticalFraction = if (maxY > edgeMargin) ((position.y - edgeMargin) / (maxY - edgeMargin)).coerceIn(0f, 1f) else 0.5f
                                placement.edit()
                                    .putString("edge", dockEdge)
                                    .putFloat("horizontal_fraction", horizontalFraction)
                                    .putFloat("vertical_fraction", verticalFraction)
                                    .apply()
                            }
                            dragTarget = null
                        },
                        onDragCancel = { dragTarget = null },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val position = dragTarget ?: currentPosition
                            dragTarget = Offset(
                                (position.x + dragAmount.x).coerceIn(edgeMargin, maxX),
                                (position.y + dragAmount.y).coerceIn(edgeMargin, maxY)
                            )
                        }
                    )
                }
        ) {
            if (compact) Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_report_bug), null, modifier = Modifier.size(24.dp))
            } else Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_report_bug), null, modifier = Modifier.padding(start = 12.dp).size(24.dp))
                Text(if (german) "Fehler melden" else "Report bug", modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = { setCompact(true) }) {
                    Icon(when (dockEdge) { "left" -> Icons.Default.KeyboardArrowLeft; "top" -> Icons.Default.KeyboardArrowUp; "bottom" -> Icons.Default.KeyboardArrowDown; else -> Icons.Default.KeyboardArrowRight },
                        if (german) "Meldebutton minimieren" else "Minimize report button")
                }
            }
        }
    }

    if (open) {
        AlertDialog(
            onDismissRequest = { if (!sending) open = false },
            title = { Text(if (german) "Fehler melden" else "Report a bug") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = { setCompact(!compact) }) {
                        Text(if (german) { if (compact) "Meldebutton vergrößern" else "Meldebutton minimieren" }
                            else { if (compact) "Expand report button" else "Minimize report button" })
                    }
                    Text(
                        if (german) "Beschreibe, was passiert ist. Wenn du magst, ergänze Schritte zum Nachstellen und was stattdessen passieren sollte. Das hilft uns beim Beheben."
                        else "Describe what happened. If you can, add steps to reproduce it and what should have happened. That helps us fix it.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { if (it.length <= 120) title = it },
                        label = { Text(if (german) "Kurzer Titel" else "Short title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.length <= 5000) description = it },
                        label = { Text(if (german) "Bericht" else "Report") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (feedback != null) Text(feedback!!, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                AppActionButton(
                    enabled = !sending && title.isNotBlank() && description.isNotBlank(),
                    onClick = {
                        sending = true
                        feedback = null
                        scope.launch {
                            val result = BugReportClient.submit(
                                localContext.applicationContext, title, description, reportContext
                            )
                            sending = false
                            if (result.isSuccess) {
                                title = ""
                                description = ""
                                open = false
                                sent = true
                            } else {
                                feedback = if (german) "Senden fehlgeschlagen. Bitte später erneut versuchen."
                                else "Could not send. Please try again later."
                            }
                        }
                    }
                ) { Text(if (sending) "…" else if (german) "Senden" else "Send") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }, enabled = !sending) {
                    Text(if (german) "Abbrechen" else "Cancel")
                }
            }
        )
    }
    if (sent) {
        AlertDialog(
            onDismissRequest = { sent = false },
            title = { Text(if (german) "Bericht gesendet" else "Report sent") },
            text = { Text(if (german) "Danke für deine Meldung." else "Thanks for reporting this.") },
            confirmButton = {
                TextButton(onClick = { sent = false }) { Text("OK") }
            }
        )
    }
}
