package com.example.myapplication.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(snapshot: StatisticsSnapshot, german: Boolean, onBack: () -> Unit) {
    var collection by rememberSaveable { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val badges = remember(snapshot) { snapshot.rankedBadges() }
    val selected = badges.firstOrNull { it.move.id == selectedId }
    BackHandler(enabled = collection && selected == null) { collection = false }
    Scaffold(topBar = {
        TopAppBar(title = { Text(if (collection) {
            if (german) "Alle Abzeichen" else "All badges"
        } else if (german) "Statistik & Abzeichen" else "Statistics & badges") },
            navigationIcon = {
                AppIconButton(onClick = { if (collection) collection = false else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, if (german) "Zurück" else "Back")
                }
            })
    }) { padding ->
        if (collection) {
            LazyColumn(Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { CollectionSummary(badges, german) }
                MoveCategory.entries.forEach { category ->
                    val group = badges.filter { it.move.category == category }
                    if (group.isNotEmpty()) {
                        item(key = "category-${category.name}") {
                            Text(categoryTitle(category, german), Modifier.padding(top = 12.dp),
                                style = MaterialTheme.typography.titleLarge)
                        }
                        items(group, key = { it.move.id }) { badge ->
                            BadgeRow(badge, german) { selectedId = badge.move.id }
                        }
                    }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { WeeklyChallengeCard(snapshot.challenge, german) }
                item { StatisticsOverview(snapshot, german) }
                item {
                    Text(if (german) "Gezählt werden abgeschlossene Reaktionsfenster, auch aus abgebrochenen Trainings. Die App prüft die Bewegung nicht."
                        else "Completed reaction windows count, including those in stopped sessions. The app cannot verify movement.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (german) "Deine Abzeichen" else "Your badges", Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge)
                        AppIconButton(onClick = { collection = true }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward,
                                if (german) "Alle Abzeichen ansehen" else "View all badges")
                        }
                    }
                }
                item {
                    BadgeHighlights(badges.take(3), german) { selectedId = it }
                }
            }
        }
    }
    if (selected != null) {
        ModalBottomSheet(onDismissRequest = { selectedId = null }) {
            BadgeDetails(selected, german, onClose = { selectedId = null })
        }
    }
}

@Composable
private fun BadgeHighlights(badges: List<BadgeProgress>, german: Boolean, onSelect: (String) -> Unit) {
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth / fontScale < 300.dp) 2 else 3
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            badges.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { badge ->
                        val title = badge.visibleTitles(german)[badge.titleIndex]
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                            .semantics(mergeDescendants = true) {
                                contentDescription = badge.move.name
                                stateDescription = if (german) "${badge.earnedLevels} von 4 Stufen erreicht"
                                    else "${badge.earnedLevels} of 4 levels earned"
                            }.clickable(role = Role.Button,
                                onClickLabel = if (german) "Abzeichen ansehen" else "View badge",
                                onClick = { onSelect(badge.move.id) })
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            BadgeEmblem(badge.earnedLevels, badge.move.category, Modifier.size(72.dp))
                            Text(title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleSmall)
                            Text(badge.move.name, modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (badge.earnedLevels > 0) {
                                Text(if (german) "Erreicht · ${badge.earnedLevels}/4" else "Earned · ${badge.earnedLevels}/4",
                                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            LinearProgressIndicator(drawStopIndicator = {}, progress = { badge.progress },
                                modifier = Modifier.fillMaxWidth())
                            Text(if (german) "${badge.count.coerceAtMost(badge.target)} von ${badge.target}"
                                else "${badge.count.coerceAtMost(badge.target)} of ${badge.target}",
                                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun StatisticsOverview(snapshot: StatisticsSnapshot, german: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (german) "Dein Training auf einen Blick" else "Your training at a glance",
            style = MaterialTheme.typography.titleLarge)
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val values = listOf(
                    snapshot.totalCalls to if (german) "Übungsaufrufe" else "Exercise calls",
                    snapshot.finishedTrainings to if (german) "Trainings beendet" else "Finished sessions",
                    snapshot.totalCuts to if (german) "Häue insgesamt" else "All cuts",
                    snapshot.yearlyCuts to if (german) "Häue dieses Jahr" else "Cuts this year")
                values.chunked(2).forEachIndexed { index, pair ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        pair.forEach { (value, label) ->
                            Column(Modifier.weight(1f)) {
                                Text(value.toString(), style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold)
                                Text(label, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionSummary(badges: List<BadgeProgress>, german: Boolean) {
    val earned = badges.sumOf { it.earnedLevels }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (german) "$earned von ${badges.size * 4} Titeln erreicht" else "$earned of ${badges.size * 4} titles earned",
            style = MaterialTheme.typography.titleLarge)
        Text(if (german) "Vier Stufen je Übung: 250, 1.000, 3.000 und 10.000 Aufrufe. Verknüpfte Varianten zählen mit."
            else "Four levels per exercise: 250, 1,000, 3,000 and 10,000 calls. Linked variants count too.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BadgeRow(badge: BadgeProgress, german: Boolean, onClick: () -> Unit) {
    val titles = badge.visibleTitles(german)
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().semantics {
            stateDescription = if (german) "${badge.earnedLevels} von 4 Stufen erreicht" else "${badge.earnedLevels} of 4 levels earned"
        }.clickable(role = Role.Button, onClickLabel = if (german) "Abzeichen ansehen" else "View badge", onClick = onClick)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            BadgeEmblem(badge.earnedLevels, badge.move.category, Modifier.size(64.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(titles[badge.titleIndex], style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(badge.move.name + if (badge.move.deleted) {
                    if (german) " (gelöscht)" else " (deleted)"
                } else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (badge.earnedLevels > 0 && !badge.complete) {
                    Text(if (german) "Nächstes: ${titles[badge.earnedLevels]}" else "Next: ${titles[badge.earnedLevels]}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LinearProgressIndicator(drawStopIndicator = {}, progress = { badge.progress }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                Text(if (badge.complete) {
                    if (german) "Alle 4 Stufen erreicht" else "All 4 levels earned"
                } else if (german) "${badge.count} / ${badge.target} · Stufe ${badge.earnedLevels}/4"
                else "${badge.count} / ${badge.target} · Level ${badge.earnedLevels}/4",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BadgeDetails(badge: BadgeProgress, german: Boolean, onClose: () -> Unit) {
    val titles = badge.visibleTitles(german)
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(badge.move.name, Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                AppIconButton(onClick = onClose) { Icon(Icons.Filled.Close, if (german) "Schließen" else "Close") }
            }
            Text(if (german) "${badge.count} Aufrufe für Abzeichen" else "${badge.count} calls toward badges",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (badge.count != badge.move.count) Text(
                if (german) "${badge.move.count} direkt, übrige aus verknüpften Varianten."
                else "${badge.move.count} direct, the rest from linked variants.", style = MaterialTheme.typography.bodySmall)
        }
        items(4) { index ->
            val earned = badge.count >= BADGE_THRESHOLDS[index]
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BadgeEmblem(if (earned) index+1 else 0, badge.move.category, Modifier.size(56.dp))
                Column(Modifier.weight(1f)) {
                    Text(titles[index], style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(if (german) "${BADGE_THRESHOLDS[index]} Aufrufe · ${if (earned) "Erreicht" else "Noch gesperrt"}"
                        else "${BADGE_THRESHOLDS[index]} calls · ${if (earned) "Earned" else "Locked"}",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(if (earned) Icons.Filled.Check else Icons.Filled.Lock, contentDescription = null,
                    tint = if (earned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun WeeklyChallengeCard(challenge: WeeklyChallenge, german: Boolean) {
    val title = if (german) when (challenge.type) {
        0 -> "30 Häue in dieser Woche"; 1 -> "An 3 Tagen in dieser Woche trainieren"; else -> "5 verschiedene Häue in dieser Woche"
    } else when (challenge.type) {
        0 -> "30 cuts this week"; 1 -> "Train on 3 days this week"; else -> "5 different cuts this week"
    }
    Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (german) "Wochenchallenge" else "Weekly challenge", style = MaterialTheme.typography.titleLarge)
        Text(title, style = MaterialTheme.typography.bodyLarge)
        LinearProgressIndicator(drawStopIndicator = {}, progress = { (challenge.progress.toFloat() / challenge.target.coerceAtLeast(1)).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth())
        Text(if (challenge.progress >= challenge.target) {
            if (german) "Geschafft!" else "Completed!"
        } else "${challenge.progress} / ${challenge.target}", color = MaterialTheme.colorScheme.primary)
        Text(if (german) "Wechselt montags automatisch. Jede passende Übung zählt, ohne Anmeldung."
            else "Changes automatically each Monday. Matching exercises count without signing up.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun categoryTitle(category: MoveCategory, german: Boolean): String = if (german) when (category) {
    MoveCategory.HAU -> "Häue"; MoveCategory.HUT -> "Huten"; MoveCategory.STICH -> "Stiche"
} else when (category) {
    MoveCategory.HAU -> "Cuts"; MoveCategory.HUT -> "Guards"; MoveCategory.STICH -> "Thrusts"
}
