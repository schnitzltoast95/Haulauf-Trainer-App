package com.example.myapplication.data

enum class MoveCategory { HAU, HUT }

data class Move(
    val id: String,
    val displayName: String,
    val category: MoveCategory = MoveCategory.HAU,
)

val DEFAULT_MOVES = listOf(
    // Haue
    Move("oberhau", "Oberhau", MoveCategory.HAU),
    Move("unterhau", "Unterhau", MoveCategory.HAU),
    Move("mittelhau", "Mittelhau", MoveCategory.HAU),
    Move("zwerchhau", "Zwerchhau", MoveCategory.HAU),
    Move("schielhau", "Schielhau", MoveCategory.HAU),
    Move("krumphau", "Krumphau", MoveCategory.HAU),
    Move("scheitelhau", "Scheitelhau", MoveCategory.HAU),
    // Huten
    Move("vom_tag", "Vom Tag", MoveCategory.HUT),
    Move("alber", "Alber", MoveCategory.HUT),
    Move("pflug", "Pflug", MoveCategory.HUT),
    Move("ochs", "Ochs", MoveCategory.HUT),
)
