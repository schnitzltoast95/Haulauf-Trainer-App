package com.example.myapplication.data

val BADGE_THRESHOLDS = listOf(25, 100, 300, 1000)

private val germanBadges = mapOf(
    "oberhau" to listOf("Dachklopfer", "Höhenjäger", "Kronenspalter", "Zwergenschlächter"),
    "unterhau" to listOf("Bodenfeger", "Aufsteiger", "Tiefenbrecher", "Herr der Unterlinie"),
    "twerhau" to listOf("Querläufer", "Klingenwender", "Horizontjäger", "Herr der Zwerch"),
    "schielhau" to listOf("Seitenblick", "Schrägseher", "Blickfänger", "Meister des Schielens"),
    "krumphau" to listOf("Kurvenkratzer", "Winkeljäger", "Hakenklinge", "Herr der krummen Wege"),
    "scheitelhau" to listOf("Mittellinienfinder", "Scheiteltreffer", "Kronenteiler", "Scheitelrichter"),
    "zornhau" to listOf("Zornfunke", "Schultersturm", "Zornklinge", "Gewitter des Zorns"),
    "sturzhau" to listOf("Sturzpilot", "Klingenstürzer", "Spitzenstürmer", "Herr des Sturzes"),
    "prellhau" to listOf("Rückpraller", "Schwungfänger", "Prellkünstler", "König des Rückschlags"),
    "blendhau" to listOf("Schattenstreifer", "Blendwerk", "Nebelklinge", "Meister der Täuschung"),
    "vom_tag" to listOf("Tagwächter", "Hochwache", "Sonnenhüter", "Wächter des Tages"),
    "alber" to listOf("Bodenwächter", "Geduldige Klinge", "Meister der Ruhe", "König des Alber"),
    "pflug" to listOf("Furchenzieher", "Linienpflüger", "Eisenpflüger", "Herr der Furche"),
    "ochs" to listOf("Hornträger", "Eckwächter", "Klingenochs", "Herr der Hörner"),
    "gerader_stich" to listOf("Punktesucher", "Linienfinder", "Zielstecher", "Meister der geraden Linie"),
    "halber_stich" to listOf("Kurzentschlossener", "Distanzspieler", "Halber Schritt", "Meister des kurzen Stichs"),
    "wechselstich" to listOf("Seitenwechsler", "Linienwechsler", "Lückenfinder", "Herr des Wechsels"),
    "durchstich" to listOf("Lückensucher", "Linienbrecher", "Durchdringer", "Herr der offenen Linie"),
    "unterstich" to listOf("Tiefensucher", "Aufwärtsstecher", "Tiefenlanze", "Herr des Unterstichs"),
)

fun badgeTitles(moveId: String, moveName: String, german: Boolean): List<String> =
    if (german) germanBadges[moveId] ?: listOf("$moveName · Lehrling", "$moveName · Kenner", "$moveName · Könner", "$moveName · Legende")
    else listOf("$moveName · Apprentice", "$moveName · Adept", "$moveName · Expert", "$moveName · Legend")
