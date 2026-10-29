package com.example.myapplication.data

import com.example.myapplication.R

enum class MoveCategory { HAU, HUT, STICH }

// These are independently verified scan indices, not printed page numbers.
// Do not infer the BSB index from Wiktenauer's PDF index for other passages.
data class HistoricalTextSource(
    val bsbPage: Int,
    val wikPage: Int,
    val passage: String,
) {
    fun originalUrl(): String = meyerScanUrl(bsbPage)
    fun transcriptionUrl(): String =
        "https://wiktenauer.com/wiki/Page:Gr%C3%BCndtliche_Beschreibung_der_Kunst_des_Fechtens_(Joachim_Me%C3%BFer)_1570.pdf/$wikPage"
}

data class Move(
    val id: String,
    val displayName: String,
    val category: MoveCategory = MoveCategory.HAU,
    val description: String? = null,
    val imagePath: String? = null, // Path to image or GIF file
    val historicalImageRes: Int? = null,
    val historicalImagePage: Int? = null,
    val historicalTextSources: List<HistoricalTextSource> = emptyList(),
    val historicalNote: String? = null,
    val historicalContextOnly: Boolean = false,
)

private fun meyerScanUrl(page: Int): String =
    "https://api.digitale-sammlungen.de/iiif/image/v2/bsb00024580_${page.toString().padStart(5, '0')}/full/full/0/default.jpg"

fun Move.historicalImageUrl(): String? = historicalImagePage?.let(::meyerScanUrl)

const val MEYER_IMAGE_RIGHTS_URL = "https://creativecommons.org/publicdomain/mark/1.0/"

private val GUARDS = HistoricalTextSource(32, 33, "I.6v · Ochs, Pflug, Tag")
private val ALBER = HistoricalTextSource(34, 35, "I.7v · Tag (Fortsetzung), Olber")
private val CUT_PATHS = HistoricalTextSource(38, 39, "I.9v · Huten im Hau, beide Seiten")
private val CUT_CONTEXT = HistoricalTextSource(40, 41, "I.10v · Von den Häuwen")
private val HIGH_CUTS = HistoricalTextSource(41, 42, "I.11r · Oberhauw, Zornhauw")
private val LOW_AND_SQUINT = HistoricalTextSource(42, 43, "I.11v · Underhauw, Schielhauw")
private val CROOKED_AND_THWART = HistoricalTextSource(44, 45, "I.12v · Krumphauw, Zwerch")
private val REBOUND = HistoricalTextSource(45, 46, "I.13r · Prellhauw")
private val BLIND = HistoricalTextSource(47, 48, "I.14r · Prellhauw (Fortsetzung), Blendthauw")
private val PLUNGING = HistoricalTextSource(48, 49, "I.14v · Sturzhauw")

private const val SCENE_NOTE = "Kontextbild; keine eindeutige Einzelabbildung dieser Technik."
private const val DRILL_IMAGE_NOTE = "Tafel B zeigt Ochs und Pflug, nicht diese moderne Stichübung."

private fun thrustDrill(id: String, name: String, description: String) = Move(
    id, name, MoveCategory.STICH, description,
    historicalImageRes = R.drawable.meyer_00031,
    historicalImagePage = 31,
    historicalTextSources = listOf(GUARDS, CUT_CONTEXT),
    historicalNote = DRILL_IMAGE_NOTE,
    historicalContextOnly = true,
)

val DEFAULT_MOVES = listOf(
    // Häue: modern summaries of the cited sword passages, not quotations.
    Move(
        "oberhau", "Oberhau", MoveCategory.HAU,
        "Gerader Hau von oben zum Kopf. Meyer nennt ihn auch Schedelhauw; er gehört zu den vier Haupthäuen. In diesem Zusammenhang ist er nicht scharf vom Scheitelhau getrennt.",
        historicalImageRes = R.drawable.meyer_00025, historicalImagePage = 25,
        historicalTextSources = listOf(HIGH_CUTS, CUT_PATHS),
        historicalNote = "Tafel A zur Teilung des Mannes. $SCENE_NOTE",
    ),
    Move(
        "unterhau", "Unterhau", MoveCategory.HAU,
        "Hau von unten nach oben; einer der vier Haupthäue. Meyer beschreibt ihn von unten quer zum linken Arm des Gegenübers, mit der Parierstange am Ende hoch über dem eigenen Kopf.",
        historicalImageRes = R.drawable.meyer_00031, historicalImagePage = 31,
        historicalTextSources = listOf(LOW_AND_SQUINT),
        historicalNote = "Tafel B: Meyer verweist für den Unterhau auf die kleinen Figuren oben links. Die großen Figuren zeigen Ochs und Pflug.",
    ),
    Move(
        "twerhau", "Twerhau", MoveCategory.HAU,
        "Meyers Zwerch: quer geführter Gegenhau gegen einen Hieb von oben, mit der Parierstange hoch über dem Kopf. Zur linken Seite des Gegenübers trifft die kurze, zur rechten die lange Schneide. Einer der fünf Meisterhäue.",
        historicalImageRes = R.drawable.meyer_00102, historicalImagePage = 102,
        historicalTextSources = listOf(CROOKED_AND_THWART),
        historicalNote = "Tafel H: Meyer verweist für die Zwerch auf das Figurenpaar links. Die große Szene rechts gehört zum Windhau, nicht zur Zwerch.",
    ),
    Move(
        "schielhau", "Schielhau", MoveCategory.HAU,
        "Oberhau mit im Schlag gewendeter kurzer Schneide gegen den gegnerischen Hieb. Meyer beschreibt dabei einen Schritt zur linken Seite des Gegenübers und einen gleichzeitigen Angriff zum Kopf.",
        historicalImageRes = R.drawable.meyer_00046, historicalImagePage = 46,
        historicalTextSources = listOf(LOW_AND_SQUINT),
        historicalNote = "Tafel G: Meyer verweist beim Schielhau auf die große Figur links. Das Blatt zeigt mehrere Fechtszenen, keine Bewegungsfolge.",
    ),
    Move(
        "krumphau", "Krumphau", MoveCategory.HAU,
        "Hau mit langer Schneide und gekreuzten Händen. Meyer beschreibt einen Schritt aus dem Angriff zur linken Seite des Gegenübers und einen Gegenhau gegen dessen Hieb oder quer auf die Hände.",
        historicalImageRes = R.drawable.meyer_00053, historicalImagePage = 53,
        historicalTextSources = listOf(CROOKED_AND_THWART),
        historicalNote = "Tafel D: Der Krumphau-Abschnitt verweist auf die kleinen Figuren oben rechts, nicht auf die große Szene im Vordergrund.",
    ),
    Move(
        "scheitelhau", "Scheitelhau", MoveCategory.HAU,
        "Gerader Hau von oben; einer der fünf Meisterhäue. Meyer beschreibt Ober- und Scheitelhau gemeinsam als Weg vom Tag über den Langort zum Alber. Die getrennten App-Einträge bedeuten hier keine zwei klar getrennten Techniken.",
        historicalImageRes = R.drawable.meyer_00073, historicalImagePage = 73,
        historicalTextSources = listOf(CUT_PATHS, HIGH_CUTS),
        historicalNote = "Tafel A bei den vier Blößen. $SCENE_NOTE",
    ),
    Move(
        "zornhau", "Zornhau", MoveCategory.HAU,
        "Kräftiger diagonaler Hau von der Schulter durch die obere Blöße. Meyers Beispiel beginnt an der rechten Schulter; er erklärt die Häue auch für die andere Seite. Der Zornhau gehört zu Haupt- und Meisterhäuen.",
        historicalImageRes = R.drawable.meyer_00025, historicalImagePage = 25,
        historicalTextSources = listOf(HIGH_CUTS, CUT_PATHS),
        historicalNote = "Tafel A zur Teilung des Mannes. $SCENE_NOTE",
    ),
    Move(
        "sturzhau", "Sturzhau", MoveCategory.HAU,
        "Oberhau, der im Durchhauen oben umgewendet wird, sodass die Spitze im Ochs zum Gesicht des Gegenübers steht. Meyer nennt vor allem den Gebrauch im Zufechten.",
        historicalImageRes = R.drawable.meyer_00031, historicalImagePage = 31,
        historicalTextSources = listOf(PLUNGING),
        historicalNote = "Tafel B zeigt unter anderem Ochs. Nur Kontext für die Endlage; keine Darstellung des Sturzhau-Ablaufs.",
    ),
    Move(
        "prellhau", "Prellhau", MoveCategory.HAU,
        "Meyer unterscheidet einfachen und doppelten Prellhau. Kennzeichnend sind Schläge mit der Klingenfläche und das Weiterführen im zurückprallenden Schwung. Beim einfachen Prellhau folgen auf eine Zwerch ein Flachschlag zum Ohr und erneut eine Zwerch.",
        historicalImageRes = R.drawable.meyer_00043, historicalImagePage = 43,
        historicalTextSources = listOf(REBOUND, BLIND),
        historicalNote = "Tafel K: Meyer verweist beim einfachen Prellhau auf die große Figur rechts. Für den doppelten nennt er Tafel I; diese ist hier nicht abgebildet.",
    ),
    Move(
        "blendhau", "Blendhau", MoveCategory.HAU,
        "Aus der Schwertbindung: mit dem Heft unten durchwinden und die vordere Klinge mit gekreuzten Händen zum Kopf schnellen lassen. Meyer beschreibt danach weiteres Durchwinden oder Ausreißen und erwähnt mehrere Varianten.",
        historicalImageRes = R.drawable.meyer_00053, historicalImagePage = 53,
        historicalTextSources = listOf(BLIND),
        historicalNote = "Tafel D aus dem Abschnitt Handarbeit. $SCENE_NOTE Der Blendhau-Abschnitt nennt keine eigene Figur.",
    ),
    // Four principal guards.
    Move(
        "vom_tag", "Vom Tag", MoveCategory.HUT,
        "Meyers Tag oder Oberhut: Schwert hoch über dem Kopf, die Spitze gerade nach oben. Das beschriebene Beispiel steht mit dem linken Fuß vorn.",
        historicalImageRes = R.drawable.meyer_00033, historicalImagePage = 33,
        historicalTextSources = listOf(GUARDS, ALBER),
        historicalNote = "Tafel C: große Figur links = Tag. Der Verweis auf B im gedruckten Text ist ein Druckfehler, in Meyers Errata zu C berichtigt.",
    ),
    Move(
        "alber", "Alber", MoveCategory.HUT,
        "Meyers Olber (Alber): Schwert vor dem Körper, die Spitze zum Boden vor dem vorderen Fuß; kurze Schneide oben, lange unten. Eine der vier Haupthuten.",
        historicalImageRes = R.drawable.meyer_00033, historicalImagePage = 33,
        historicalTextSources = listOf(ALBER),
        historicalNote = "Tafel C: große Figur rechts = Olber (Alber); links steht Tag.",
    ),
    Move(
        "pflug", "Pflug", MoveCategory.HUT,
        "Hut mit dem Heft neben dem vorderen Knie und der Spitze zum Gesicht des Gegenübers, als wollte man von unten aufstechen. Meyer beschreibt rechten und linken Pflug.",
        historicalImageRes = R.drawable.meyer_00031, historicalImagePage = 31,
        historicalTextSources = listOf(GUARDS),
        historicalNote = "Tafel B: große Figur rechts = Pflug; links steht Ochs.",
    ),
    Move(
        "ochs", "Ochs", MoveCategory.HUT,
        "Hohe Hut mit dem Heft seitlich neben dem Kopf und der Spitze zum Gesicht des Gegenübers. Beim rechten Ochs steht links vorn, beim linken Ochs rechts.",
        historicalImageRes = R.drawable.meyer_00031, historicalImagePage = 31,
        historicalTextSources = listOf(GUARDS),
        historicalNote = "Tafel B: große Figur links = Ochs; rechts steht Pflug.",
    ),
    // Modern app drill labels. The cited sword passages are context, not proof
    // of five independently named historical thrust techniques.
    thrustDrill("gerader_stich", "Gerader Stich", "Moderner Übungsname der App für einen geradlinigen Stich. Die verlinkte Pflug-Beschreibung erklärt eine zum Gesicht gerichtete Spitze, belegt aber keine eigene Technik dieses Namens."),
    thrustDrill("halber_stich", "Halber Stich", "Moderner Übungsname der App für einen verkürzten Stichansatz. Länge und Ablauf sind damit nicht historisch festgelegt; insbesondere ist damit nicht automatisch Halbschwertfechten gemeint."),
    thrustDrill("wechselstich", "Wechselstich", "Moderner Übungsname der App für einen Stich nach einem Wechsel von Linie oder Seite. Nicht mit Meyers Wechselhau gleichzusetzen; die verlinkten Stellen belegen diesen Übungsnamen nicht."),
    thrustDrill("durchstich", "Durchstich", "Moderner Übungsname der App für einen Stich durch eine geöffnete Linie. Daraus folgt keine bestimmte historische Ausführung und keine Gleichsetzung mit Meyers Durchwechseln."),
    thrustDrill("unterstich", "Unterstich", "Moderner Übungsname der App für einen von unten angesetzten Stich. Meyers Pflug liefert Kontext für diese Richtung, aber keinen Beleg für diese Übung als eigenständig benannte Technik."),
)
