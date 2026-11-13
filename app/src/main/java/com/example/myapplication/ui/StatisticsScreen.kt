package com.example.myapplication.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BADGE_THRESHOLDS
import com.example.myapplication.data.MoveCategory
import com.example.myapplication.data.MoveStat
import com.example.myapplication.data.StatisticsSnapshot
import com.example.myapplication.data.badgeTitles

@Composable
fun StatisticsScreen(snapshot: StatisticsSnapshot, german: Boolean, onBack: () -> Unit) {
    val challenge = snapshot.challenge
    val challengeTitle = if (german) when (challenge.type) {
        0 -> "30 Häue in dieser Woche"
        1 -> "An 3 Tagen in dieser Woche trainieren"
        else -> "5 verschiedene Häue in dieser Woche"
    } else when (challenge.type) {
        0 -> "30 cuts this week"
        1 -> "Train on 3 days this week"
        else -> "5 different cuts this week"
    }
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text(if (german) "← Zurück" else "← Back") }
        Text(if (german) "Statistik" else "Statistics", style = MaterialTheme.typography.headlineLarge)
        Text(
            if (german) "Gezählt werden abgeschlossene Reaktionsfenster, auch aus abgebrochenen Trainings. Die App prüft die Bewegung nicht."
            else "Completed reaction windows count, including those in stopped sessions. The app cannot verify movement.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (german) "Dein Training" else "Your training", style = MaterialTheme.typography.titleLarge)
                StatisticLine(if (german) "Übungsaufrufe insgesamt" else "All exercise calls", snapshot.totalCalls)
                StatisticLine(if (german) "Häue insgesamt" else "All cuts", snapshot.totalCuts)
                StatisticLine(if (german) "Häue dieses Jahr" else "Cuts this year", snapshot.yearlyCuts)
                StatisticLine(if (german) "Abgeschlossene Trainings" else "Finished sessions", snapshot.finishedTrainings)
            }
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (german) "Wochenchallenge" else "Weekly challenge", style = MaterialTheme.typography.titleLarge)
                Text(challengeTitle)
                Text("${challenge.progress.coerceAtMost(challenge.target)} / ${challenge.target}")
                LinearProgressIndicator(
                    progress = { (challenge.progress.toFloat() / challenge.target).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (challenge.progress >= challenge.target) Text(if (german) "Geschafft!" else "Completed!")
                Text(
                    if (german) "Wechselt montags automatisch. Jede passende Übung zählt, ohne Anmeldung."
                    else "Changes automatically each Monday. Matching exercises count without signing up.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Text(if (german) "Übungen & Abzeichen" else "Exercises & badges", style = MaterialTheme.typography.titleLarge)
        Text(
            if (german) "Tippe auf eine Übung für alle vier Stufen. Verknüpfte eigene Varianten zählen für das Abzeichen der Grundübung mit."
            else "Tap an exercise to see all four levels. Linked custom variants also count toward the base exercise badge.",
            style = MaterialTheme.typography.bodySmall,
        )
        MoveCategory.entries.forEach { category ->
            val moves = snapshot.moves.filter { it.category == category }
            if (moves.isNotEmpty()) {
                Text(
                    if (german) when (category) {
                        MoveCategory.HAU -> "Häue"
                        MoveCategory.HUT -> "Huten"
                        MoveCategory.STICH -> "Stiche"
                    } else when (category) {
                        MoveCategory.HAU -> "Cuts"
                        MoveCategory.HUT -> "Guards"
                        MoveCategory.STICH -> "Thrusts"
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                moves.forEach { stat -> MoveBadgeCard(stat, snapshot.badgeCounts[stat.id] ?: 0, german) }
            }
        }
    }
}

@Composable
private fun StatisticLine(label: String, value: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Spacer(Modifier.width(12.dp))
        Text(value.toString(), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MoveBadgeCard(stat: MoveStat, badgeCount: Int, german: Boolean) {
    var expanded by rememberSaveable(stat.id) { mutableStateOf(false) }
    val titles = badgeTitles(stat.id, stat.name, german)
    val next = BADGE_THRESHOLDS.indexOfFirst { badgeCount < it }
    ElevatedCard(Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stat.name + if (stat.deleted) (if (german) " (gelöscht)" else " (deleted)") else "",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(if (german) "${stat.count} Aufrufe" else "${stat.count} calls")
            if (badgeCount != stat.count) Text(
                if (german) "$badgeCount für Abzeichen, inklusive Varianten"
                else "$badgeCount toward badges, including variants",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                when {
                    next < 0 -> if (german) "Alle Abzeichen erreicht" else "All badges earned"
                    else -> "${titles[next]} · $badgeCount/${BADGE_THRESHOLDS[next]}"
                },
                color = MaterialTheme.colorScheme.primary,
            )
            if (expanded) {
                HorizontalDivider()
                BADGE_THRESHOLDS.forEachIndexed { index, threshold ->
                    Text("${if (badgeCount >= threshold) "✓" else "○"} ${titles[index]} · $threshold")
                }
            }
        }
    }
}
