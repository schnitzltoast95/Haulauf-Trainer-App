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
import com.example.myapplication.training.TrainingState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TrainingScreen(
    state: StateFlow<TrainingState>,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
) {
    val strings = LocalStrings.current
    val s by state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        when (val st = s) {
            TrainingState.Idle -> {
                Text(strings.starting, style = MaterialTheme.typography.headlineMedium)
            }
            is TrainingState.StartCountdown -> {
                Text(
                    text = st.count.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(vertical = 48.dp)
                )
            }
            is TrainingState.Calling -> {
                Text(
                    text = st.move,
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
                Text(strings.unitRoundProgressFormat.format(st.unit, st.totalUnits, strings.roundLabel, st.round, st.totalRounds))
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
            }
            is TrainingState.WaitingWindow -> {
                Text(
                    text = st.move,
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
                Text(strings.unitRoundProgressFormat.format(st.unit, st.totalUnits, strings.roundLabel, st.round, st.totalRounds))
                val progress = if (st.windowMs > 0) (st.elapsedMs.toFloat() / st.windowMs).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
            }
            is TrainingState.RoundPause -> {
                Text(
                    text = "${strings.pause}\n${st.countdown}",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).wrapContentHeight(Alignment.CenterVertically)
                )
            }
            is TrainingState.Paused -> {
                Text(
                    text = strings.paused,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f).wrapContentHeight(Alignment.CenterVertically)
                )
                when (val c = st.current) {
                    is TrainingState.CallingState.InCall -> Text(strings.lastCallFormat.format(c.move))
                    is TrainingState.CallingState.InWindow -> Text(strings.lastCallFormat.format(c.move))
                    is TrainingState.CallingState.InRoundPause -> Text(strings.pauseRoundFormat.format(c.round))
                    TrainingState.CallingState.Countdown -> Text(strings.countdown)
                }
            }
            TrainingState.Finished -> {
                Text(
                    text = strings.finishedExclamation,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f).wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (s is TrainingState.Paused) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1f)
                ) { Text(strings.resume) }
            } else if (s !is TrainingState.Finished && s !is TrainingState.Idle) {
                OutlinedButton(
                    onClick = onPause,
                    modifier = Modifier.weight(1f)
                ) { Text(strings.pause) }
            }
            Button(
                onClick = onStop,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text(strings.stop) }
        }
    }
}

