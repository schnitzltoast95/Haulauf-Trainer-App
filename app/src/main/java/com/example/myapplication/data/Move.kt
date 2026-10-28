package com.example.myapplication.data

import com.example.myapplication.R

enum class MoveCategory { HAU, HUT, STICH }

data class Move(
    val id: String,
    val displayName: String,
    val category: MoveCategory = MoveCategory.HAU,
    val description: String? = null,
    val imagePath: String? = null, // Path to image or GIF file
    val historicalImageRes: Int? = null,
    val historicalImagePage: Int? = null,
    val historicalTextPage: Int? = null,
    val historicalNote: String? = null,
)

fun Move.historicalImageUrl(): String? = historicalImagePage?.let { page ->
    "https://api.digitale-sammlungen.de/iiif/image/v2/bsb00024580_${page.toString().padStart(5, '0')}/full/full/0/default.jpg"
}

fun Move.historicalTextUrl(): String? = historicalTextPage?.let { page ->
    "https://wiktenauer.com/wiki/Page:Gr%C3%BCndtliche_Beschreibung_der_Kunst_des_Fechtens_(Joachim_Me%C3%BFer)_1570.pdf/$page"
}

private const val SCENE_NOTE = "Historische Fechtszene; keine eindeutige Einzelabbildung dieser Technik."
private const val DRILL_NOTE = "Die Bezeichnung dieser Stichübung ist bei Meyer nicht als eigenständige Technik belegt. Das Bild zeigt nur den historischen Fechtkontext."

val DEFAULT_MOVES = listOf(
    // Haue
    Move("oberhau", "Oberhau", MoveCategory.HAU, "Hau von oben nach unten. Meyer zählt ihn zu den vier Grundhäuen.", historicalImageRes = R.drawable.meyer_00025, historicalImagePage = 25, historicalTextPage = 42, historicalNote = SCENE_NOTE),
    Move("unterhau", "Unterhau", MoveCategory.HAU, "Hau von unten nach oben. Bei Meyer gehört er zu den vier Grundhäuen.", historicalImageRes = R.drawable.meyer_00046, historicalImagePage = 46, historicalTextPage = 43, historicalNote = SCENE_NOTE),
    Move("twerhau", "Twerhau", MoveCategory.HAU, "Waagerechter Hau mit hoch gehaltenen Händen; Meyer nennt ihn Zwerchhau und zählt ihn zu den Meisterhäuen.", historicalImageRes = R.drawable.meyer_00043, historicalImagePage = 43, historicalTextPage = 45, historicalNote = SCENE_NOTE),
    Move("schielhau", "Schielhau", MoveCategory.HAU, "Hau von oben, bei dem die Hand im Schlag gewendet wird und die kurze Schneide auf die gegnerische Klinge treffen kann.", historicalImageRes = R.drawable.meyer_00053, historicalImagePage = 53, historicalTextPage = 43, historicalNote = SCENE_NOTE),
    Move("krumphau", "Krumphau", MoveCategory.HAU, "Schräg geführter Hau mit gekreuzten Armen, häufig gegen Klinge oder Hände des Gegenübers.", historicalImageRes = R.drawable.meyer_00059, historicalImagePage = 59, historicalTextPage = 45, historicalNote = SCENE_NOTE),
    Move("scheitelhau", "Scheitelhau", MoveCategory.HAU, "Gerader Hau von oben zur Scheitellinie. Meyer zählt ihn zu den fünf Meisterhäuen.", historicalImageRes = R.drawable.meyer_00073, historicalImagePage = 73, historicalTextPage = 42, historicalNote = SCENE_NOTE),
    Move("zornhau", "Zornhau", MoveCategory.HAU, "Kräftiger diagonaler Hau von oben aus der rechten oder linken Seite; einer von Meyers Grundhäuen und Meisterhäuen.", historicalImageRes = R.drawable.meyer_00025, historicalImagePage = 25, historicalTextPage = 42, historicalNote = SCENE_NOTE),
    Move("sturzhau", "Sturzhau", MoveCategory.HAU, "Ein von oben geführter Hau, bei dem die Spitze im Durchhauen zum Gesicht des Gegenübers in den Ochs kommt.", historicalImageRes = R.drawable.meyer_00084, historicalImagePage = 84, historicalTextPage = 49, historicalNote = SCENE_NOTE),
    Move("prellhau", "Prellhau", MoveCategory.HAU, "Hau mit abprallender, unmittelbar wiederholter Bewegung. Meyer unterscheidet einen einfachen und einen doppelten Prellhau.", historicalImageRes = R.drawable.meyer_00046, historicalImagePage = 46, historicalTextPage = 46, historicalNote = SCENE_NOTE),
    Move("blendhau", "Blendhau", MoveCategory.HAU, "Aus einer Bindung am Schwert geführter Hau mit gekreuzten Händen zur oberen Blöße; Meyer beschreibt mehrere Varianten.", historicalImageRes = R.drawable.meyer_00053, historicalImagePage = 53, historicalTextPage = 48, historicalNote = SCENE_NOTE),
    // Huten
    Move("vom_tag", "Vom Tag", MoveCategory.HUT, "Oberhut mit hoch über dem Kopf gehaltenem Schwert und aufwärts gerichtetem Ort.", historicalImageRes = R.drawable.meyer_00036, historicalImagePage = 36, historicalTextPage = 33, historicalNote = SCENE_NOTE),
    Move("alber", "Alber", MoveCategory.HUT, "Tiefe Hut mit gesenkter Schwertspitze. Meyer zählt Alber zu den vier Haupthuten.", historicalImageRes = R.drawable.meyer_00031, historicalImagePage = 31, historicalTextPage = 35, historicalNote = SCENE_NOTE),
    Move("pflug", "Pflug", MoveCategory.HUT, "Hut mit dem Heft neben dem vorderen Knie und der Spitze zum Gegenüber; Meyer beschreibt rechte und linke Seite.", historicalImageRes = R.drawable.meyer_00033, historicalImagePage = 33, historicalTextPage = 33, historicalNote = SCENE_NOTE),
    Move("ochs", "Ochs", MoveCategory.HUT, "Hohe Hut mit dem Heft neben dem Kopf und der Spitze zum Gesicht des Gegenübers; rechts und links möglich.", historicalImageRes = R.drawable.meyer_00033, historicalImagePage = 33, historicalTextPage = 33, historicalNote = SCENE_NOTE),
    // Stiche
    Move("gerader_stich", "Gerader Stich", MoveCategory.STICH, "Trainingsbegriff für einen geradlinigen Stich mit der Schwertspitze. Meyer beschreibt Stiche unter anderem aus dem Pflug.", historicalImageRes = R.drawable.meyer_00033, historicalImagePage = 33, historicalTextPage = 33, historicalNote = DRILL_NOTE),
    Move("halber_stich", "Halber Stich", MoveCategory.STICH, "Trainingsbegriff für einen verkürzten Stichansatz. Die genaue Ausführung ist in der App nicht historisch definiert.", historicalImageRes = R.drawable.meyer_00043, historicalImagePage = 43, historicalTextPage = 33, historicalNote = DRILL_NOTE),
    Move("wechselstich", "Wechselstich", MoveCategory.STICH, "Trainingsbegriff für einen Stich nach einem Wechsel der Linie oder Seite. Die genaue Ausführung ist in der App nicht historisch definiert.", historicalImageRes = R.drawable.meyer_00059, historicalImagePage = 59, historicalTextPage = 33, historicalNote = DRILL_NOTE),
    Move("durchstich", "Durchstich", MoveCategory.STICH, "Trainingsbegriff für einen Stich, der durch eine geöffnete Linie geführt wird. Die genaue Ausführung ist in der App nicht historisch definiert.", historicalImageRes = R.drawable.meyer_00073, historicalImagePage = 73, historicalTextPage = 33, historicalNote = DRILL_NOTE),
    Move("unterstich", "Unterstich", MoveCategory.STICH, "Trainingsbegriff für einen von unten ansetzenden Stich. Meyers Pflug beschreibt eine entsprechende Ausrichtung der Spitze.", historicalImageRes = R.drawable.meyer_00033, historicalImagePage = 33, historicalTextPage = 33, historicalNote = DRILL_NOTE),
)
