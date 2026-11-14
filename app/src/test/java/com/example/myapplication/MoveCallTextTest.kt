package com.example.myapplication

import com.example.myapplication.audio.ttsCallText
import com.example.myapplication.data.DEFAULT_MOVES
import com.example.myapplication.data.MoveAlias
import com.example.myapplication.data.MoveIdentity
import org.junit.Assert.assertEquals
import org.junit.Test

class MoveCallTextTest {
    @Test fun krumphauUsesSeparatePAndHInSyntheticCall() {
        val krumphau = DEFAULT_MOVES.first { it.id == "krumphau" }
        assertEquals("Krumphau", krumphau.displayName)
        assertEquals("Krump Hau", krumphau.ttsCallText())
    }

    @Test fun personalTrainingNameOverridesPronunciationHint() {
        val krumphau = DEFAULT_MOVES.first { it.id == "krumphau" }
            .copy(identity = MoveIdentity(aliases = listOf(MoveAlias("Krummhau")), preferredAlias = "Krummhau"))
        assertEquals("Krummhau", krumphau.ttsCallText())
        assertEquals("Ochs", DEFAULT_MOVES.first { it.id == "ochs" }.ttsCallText())
    }
}
