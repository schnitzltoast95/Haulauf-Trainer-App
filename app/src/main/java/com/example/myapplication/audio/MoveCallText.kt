package com.example.myapplication.audio

import com.example.myapplication.data.Move

/** Keep historical display names while giving TTS an unambiguous word boundary. */
internal fun Move.ttsCallText(): String =
    if (id == "krumphau" && trainingName == "Krumphau") "Krump Hau" else trainingName
