package com.example.myapplication.data

import java.util.UUID

data class SavedPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val preset: TrainingPreset
)
