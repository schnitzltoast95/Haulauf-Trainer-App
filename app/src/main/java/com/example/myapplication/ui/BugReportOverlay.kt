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
    var steps by remember { mutableStateOf("") }
    var expected by remember { mutableStateOf("") }
    var reportingRoute by remember { mutableStateOf("home") }
    var sending by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var sent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val placement = remember(localContext) {
        localContext.getSharedPreferences("bug_report_button", android.content.Context.MODE_PRIVATE)
    }
    var dockRight by remember(placement) { mutableStateOf(placement.getBoolean("right", true)) }
    var verticalFraction by remember(placement) {
        mutableStateOf(placement.getFloat("vertical_fraction", 0.5f).coerceIn(0f, 1f))
    }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var buttonSize by remember { mutableStateOf(IntSize.Zero) }
    var dragTarget by remember { mutableStateOf<Offset?>(null) }
    val marginPx = with(LocalDensity.current) { 12.dp.toPx() }
    val maxX = (viewportSize.width - buttonSize.width - marginPx).coerceAtLeast(marginPx)
    val maxY = (viewportSize.height - buttonSize.height - marginPx).coerceAtLeast(marginPx)
    val dockTarget = Offset(
        x = if (dockRight) maxX else marginPx,
        y = marginPx + (maxY - marginPx) * verticalFraction
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
        FloatingActionButton(
            onClick = {
                reportingRoute = context()
                feedback = null
                open = true
            },
            modifier = Modifier
                .offset { IntOffset(animatedPosition.x.roundToInt(), animatedPosition.y.roundToInt()) }
                .onSizeChanged { buttonSize = it }
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .semantics {
                    contentDescription = if (german) "Fehler melden. Zum Verschieben ziehen."
                    else "Report bug. Drag to move."
                }
                .pointerInput(viewportSize, buttonSize, marginPx) {
                    detectDragGestures(
                        onDragStart = { dragTarget = currentPosition },
                        onDragEnd = {
                            dragTarget?.let { position ->
                                dockRight = position.x >= (marginPx + maxX) / 2f
                                verticalFraction = if (maxY > marginPx) {
                                    ((position.y - marginPx) / (maxY - marginPx)).coerceIn(0f, 1f)
                                } else 0.5f
                                placement.edit()
                                    .putBoolean("right", dockRight)
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
                                (position.x + dragAmount.x).coerceIn(marginPx, maxX),
                                (position.y + dragAmount.y).coerceIn(marginPx, maxY)
                            )
                        }
                    )
                }
        ) { Text(if (german) "Fehler melden" else "Report bug", modifier = Modifier.padding(horizontal = 12.dp)) }
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
                    Text(
                        if (german) "Beschreibe, was passiert ist. Ansicht, Trainingsphase, App- und Android-Version sowie Gerätemodell werden mitgesendet. Der Bericht erscheint öffentlich auf GitHub."
                        else "Describe what happened. Screen, training phase, app and Android versions, and device model will be sent. The report will be public on GitHub.",
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
                        label = { Text(if (german) "Was ist passiert?" else "What happened?") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = steps,
                        onValueChange = { if (it.length <= 2000) steps = it },
                        label = { Text(if (german) "Schritte zum Nachstellen (optional)" else "Steps to reproduce (optional)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expected,
                        onValueChange = { if (it.length <= 1000) expected = it },
                        label = { Text(if (german) "Was sollte passieren? (optional)" else "Expected result (optional)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (feedback != null) Text(feedback!!, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(
                    enabled = !sending && title.isNotBlank() && description.isNotBlank(),
                    onClick = {
                        sending = true
                        feedback = null
                        scope.launch {
                            val result = BugReportClient.submit(
                                localContext.applicationContext, title, description, steps, expected, reportingRoute
                            )
                            sending = false
                            if (result.isSuccess) {
                                title = ""
                                description = ""
                                steps = ""
                                expected = ""
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
