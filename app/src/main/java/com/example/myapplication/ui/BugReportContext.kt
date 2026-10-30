package com.example.myapplication.ui

import com.example.myapplication.training.TrainingState

internal fun bugReportContext(route: String, section: String?, trainingState: TrainingState): String {
    val screen = when {
        route == "home" -> "Startseite"
        route == "setup" -> "Training konfigurieren"
        route == "training" -> "Training"
        route == "settings" -> "Einstellungen"
        route.startsWith("presets") -> "Trainingsbibliothek"
        route == "move_audio_list" -> "Übungsbibliothek"
        route.startsWith("move_details/") -> "Übungsdetails"
        route.startsWith("move_audio/") -> "Übung bearbeiten (${section ?: "DATA"})"
        else -> route
    }
    if (route != "training") return "$screen [route: $route]"
    val state = when (trainingState) {
        TrainingState.Idle -> "bereit"
        is TrainingState.StartCountdown -> "Start-Countdown"
        is TrainingState.Calling -> "Ansage, Runde ${trainingState.round}/${trainingState.totalRounds}, Übung ${trainingState.unit}/${trainingState.totalUnits}"
        is TrainingState.WaitingWindow -> "Reaktionsfenster, Runde ${trainingState.round}/${trainingState.totalRounds}, Übung ${trainingState.unit}/${trainingState.totalUnits}"
        is TrainingState.RoundPause -> "Rundenpause nach Runde ${trainingState.round}/${trainingState.totalRounds}"
        is TrainingState.Paused -> "pausiert"
        TrainingState.Finished -> "beendet"
    }
    return "$screen [route: $route; Zustand: $state]"
}
