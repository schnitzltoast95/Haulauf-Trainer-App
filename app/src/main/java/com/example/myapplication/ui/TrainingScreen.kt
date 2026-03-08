package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.TrainingPreset
import com.example.myapplication.training.TrainingState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TrainingScreen(
    preset: TrainingPreset,
    state: StateFlow<TrainingState>,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onStop: () -> Unit,
) {
    val strings = LocalStrings.current
    val s by state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            contentAlignment = Alignment.Center
        ) {
            TrainingStatusContent(state = s, preset = preset)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            if (s is TrainingState.Paused) {
                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                ) { Text(strings.resume, style = MaterialTheme.typography.headlineSmall) }
                Spacer(modifier = Modifier.height(20.dp))
            } else if (s !is TrainingState.Finished && s !is TrainingState.Idle) {
                OutlinedButton(
                    onClick = onPause,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                ) { Text(strings.pause, style = MaterialTheme.typography.headlineSmall) }
                Spacer(modifier = Modifier.height(20.dp))
            }
            if (s is TrainingState.Finished) {
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                ) {
                    Text(strings.restartTraining, style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
            Button(
                onClick = onStop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                val endLabel = if (s is TrainingState.Finished) strings.endTraining else strings.stop
                Text(endLabel, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun TrainingStatusContent(state: TrainingState, preset: TrainingPreset) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val st = state) {
            TrainingState.Idle -> {
                Text(strings.starting, style = MaterialTheme.typography.headlineMedium)
            }
            is TrainingState.StartCountdown -> {
                Text(
                    text = st.count.toString(),
                    style = MaterialTheme.typography.displayLarge
                )
            }
            is TrainingState.Calling -> {
                Text(
                    text = st.move,
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(strings.unitRoundProgressFormat.format(st.unit, st.totalUnits, strings.roundLabel, st.round, st.totalRounds))
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
            is TrainingState.WaitingWindow -> {
                Text(
                    text = st.move,
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(strings.unitRoundProgressFormat.format(st.unit, st.totalUnits, strings.roundLabel, st.round, st.totalRounds))
                Spacer(modifier = Modifier.height(8.dp))
                val progress = if (st.windowMs > 0) (st.elapsedMs.toFloat() / st.windowMs).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
            is TrainingState.RoundPause -> {
                Text(
                    text = "${strings.pause}\n${st.countdown}",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
            }
            is TrainingState.Paused -> {
                Text(
                    text = strings.paused,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                when (val c = st.current) {
                    is TrainingState.CallingState.InCall -> Text(strings.lastCallFormat.format(c.move))
                    is TrainingState.CallingState.InWindow -> Text(strings.lastCallFormat.format(c.move))
                    is TrainingState.CallingState.InRoundPause -> Text(strings.pauseRoundFormat.format(c.round))
                    TrainingState.CallingState.Countdown -> Text(strings.countdown)
                }
            }
            TrainingState.Finished -> {
                Text(strings.wellDone, style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(strings.trainingOverview, style = MaterialTheme.typography.titleMedium)
                        Text("${strings.rounds}: ${preset.rounds}")
                        Text("${strings.callsRound}: ${preset.unitsPerRound}")
                        Text("${strings.moves}: ${preset.totalUnits}")
                        Text("${strings.estimatedDuration}: ${formatDuration(preset.estimatedDurationSeconds())}")
                    }
                }
            }
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

