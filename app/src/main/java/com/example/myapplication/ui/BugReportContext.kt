package com.example.myapplication.ui

import com.example.myapplication.training.TrainingState

internal class BugReportTrail {
    private val locations = ArrayDeque<String>()
    private val trainingPhases = ArrayDeque<String>()

    fun record(route: String, section: String?, moveId: String?) {
        val location = bugReportScreen(route, section, moveId)
        if (locations.lastOrNull() == location) return
        locations.addLast(location)
        if (locations.size > 6) locations.removeFirst()
    }

    fun beforeCurrent(): List<String> = locations.dropLast(1)

    fun recordTrainingState(state: TrainingState) {
        if (state == TrainingState.Idle) return
        val phase = state.bugReportLabel()
        if (trainingPhases.lastOrNull() == phase) return
        trainingPhases.addLast(phase)
        if (trainingPhases.size > 6) trainingPhases.removeFirst()
    }

    fun recentTrainingPhases(): List<String> = trainingPhases.toList()
}

internal fun bugReportContext(
    route: String,
    section: String?,
    moveId: String?,
    trainingState: TrainingState,
    previousLocations: List<String>,
    recentTrainingPhases: List<String>
): String = buildString {
    appendLine("- Ansicht vor dem Öffnen des Meldeformulars: ${bugReportScreen(route, section, moveId)} [route: $route]")
    if (previousLocations.isNotEmpty()) {
        appendLine("- Zuvor geöffnete Ansichten (älteste zuerst): ${previousLocations.joinToString(" → ")}")
    }
    if (route == "training") appendLine("- Trainingsphase beim Öffnen: ${trainingState.bugReportLabel()}")
    if (recentTrainingPhases.isNotEmpty()) {
        appendLine("- Zuletzt durchlaufene Trainingsphasen: ${recentTrainingPhases.joinToString(" → ")}")
    }
}

private fun bugReportScreen(route: String, section: String?, moveId: String?): String = when {
    route == "home" -> "Startseite"
    route == "setup" -> "Training konfigurieren"
    route == "training" -> "Training"
    route == "settings" -> "Einstellungen"
    route.startsWith("presets") -> "Trainingsbibliothek"
    route == "move_audio_list" -> "Übungsbibliothek"
    route.startsWith("move_details/") -> "Übungsdetails${moveId?.let { " (Übungs-ID: $it)" } ?: ""}"
    route.startsWith("move_audio/") -> "Übung bearbeiten${moveId?.let { " (Übungs-ID: $it)" } ?: ""}, Bereich ${section ?: "DATA"}"
    else -> route
}

private fun TrainingState.bugReportLabel(): String = when (this) {
    TrainingState.Idle -> "bereit"
    is TrainingState.StartCountdown -> "Start-Countdown"
    is TrainingState.Calling -> "Ansage, Runde $round/$totalRounds, Übung $unit/$totalUnits"
    is TrainingState.WaitingWindow -> "Reaktionsfenster, Runde $round/$totalRounds, Übung $unit/$totalUnits"
    is TrainingState.RoundPause -> "Rundenpause nach Runde $round/$totalRounds"
    is TrainingState.Paused -> "pausiert"
    TrainingState.Finished -> "beendet"
}
