package com.example.myapplication.data

val BADGE_THRESHOLDS = listOf(250, 1000, 3000, 10000)

private val germanBadges = mapOf(
    "oberhau" to listOf("Dachklopfer", "Kronenspalter", "Scheitelrichter", "Zwergenschlächter"),
    "unterhau" to listOf("Aufsteiger", "Klingenheber", "Tiefstarter", "Aufstand von unten"),
    "twerhau" to listOf("Quereinsteiger", "Querfechter", "Zwerchmeister", "Alles läuft quer"),
    "schielhau" to listOf("Schneidenwender", "Gegenhaukenner", "Meister der kurzen Schneide", "Kurz und schneidig"),
    "krumphau" to listOf("Winkelspieler", "Handkreuzer", "Klingenfänger", "Meister ums Eck"),
    "scheitelhau" to listOf("Linienfinder", "Scheitelteiler", "Kronenspalter", "Scheitel nach Maß"),
    "zornhau" to listOf("Schulterstarter", "Diagonalklinge", "Zornmeister", "Donner von der Schulter"),
    "sturzhau" to listOf("Spitzenwender", "Ochs im Anflug", "Sturzmeister", "Oben hui, Spitze voraus"),
    "prellhau" to listOf("Rückpraller", "Flachklopfer", "Prellmeister", "Kommt alles zurück"),
    "blendhau" to listOf("Klingenbinder", "Heftwender", "Schnellklinge", "Überraschung im Band"),
    "vom_tag" to listOf("Hochwache", "Dachwächter", "Meister der Oberhut", "Alles Gute kommt von oben"),
    "alber" to listOf("Bodenwache", "Tiefhalter", "Meister der tiefen Hut", "Tiefenentspannt"),
    "pflug" to listOf("Furchenzieher", "Spitzenpflüger", "Meister der Furche", "Gut bestellt"),
    "ochs" to listOf("Hornträger", "Hochhutwächter", "Meister der Hörner", "Spitze Hörner"),
    "gerader_stich" to listOf("Linienfinder", "Punktlandung", "Meister der Geraden", "Direkt auf den Punkt"),
    "halber_stich" to listOf("Kurzstarter", "Distanzkenner", "Meister der kurzen Strecke", "In der Kürze liegt die Spitze"),
    "wechselstich" to listOf("Seitenwechsler", "Linienwechsler", "Wechselmeister", "Neue Linie, gleicher Punkt"),
    "durchstich" to listOf("Lückenfinder", "Lückennutzer", "Meister der offenen Linie", "Lücke genutzt"),
    "unterstich" to listOf("Tiefstarter", "Aufwärtsspitze", "Meister der unteren Linie", "Spitze von unten"),
)

fun badgeTitles(moveId: String, moveName: String, german: Boolean): List<String> =
    if (german) germanBadges[moveId] ?: listOf("$moveName · Lehrling", "$moveName · Kenner", "$moveName · Könner", "$moveName · Legende")
    else listOf("$moveName · Apprentice", "$moveName · Adept", "$moveName · Expert", "$moveName · Legend")
