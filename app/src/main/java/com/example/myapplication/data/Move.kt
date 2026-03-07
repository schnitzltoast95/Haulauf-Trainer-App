package com.example.myapplication.data

enum class MoveCategory { HAU, HUT, STICH }

data class Move(
    val id: String,
    val displayName: String,
    val category: MoveCategory = MoveCategory.HAU,
    val description: String? = null,
    val imagePath: String? = null, // Path to image or GIF file
)

val DEFAULT_MOVES = listOf(
    // Haue
    Move("oberhau", "Oberhau", MoveCategory.HAU),
    Move("unterhau", "Unterhau", MoveCategory.HAU),
    Move("twerhau", "Twerhau", MoveCategory.HAU),
    Move("schielhau", "Schielhau", MoveCategory.HAU),
    Move("krumphau", "Krumphau", MoveCategory.HAU),
    Move("scheitelhau", "Scheitelhau", MoveCategory.HAU),
    Move("zornhau", "Zornhau", MoveCategory.HAU),
    Move("sturz", "Sturz", MoveCategory.HAU),
    Move("prell", "Prell", MoveCategory.HAU),
    Move("blendhau", "Blendhau", MoveCategory.HAU),
    // Huten
    Move("vom_tag", "Vom Tag", MoveCategory.HUT),
    Move("alber", "Alber", MoveCategory.HUT),
    Move("pflug", "Pflug", MoveCategory.HUT),
    Move("ochs", "Ochs", MoveCategory.HUT),
    // Stiche
    Move("gerader_stich", "Gerader Stich", MoveCategory.STICH),
    Move("halber_stich", "Halber Stich", MoveCategory.STICH),
    Move("wechselstich", "Wechselstich", MoveCategory.STICH),
    Move("durchstich", "Durchstich", MoveCategory.STICH),
    Move("unterstich", "Unterstich", MoveCategory.STICH),
)
