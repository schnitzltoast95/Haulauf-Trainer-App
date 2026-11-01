package com.example.myapplication

import com.example.myapplication.data.DEFAULT_MOVES
import com.example.myapplication.data.MoveAlias
import com.example.myapplication.data.MoveIdentity
import org.junit.Assert.assertEquals
import org.junit.Test

class MoveIdentityTest {
    @Test
    fun preferredAliasChangesTrainingNameWithoutChangingMoveIdOrOriginal() {
        val original = DEFAULT_MOVES.first { it.id == "oberhau" }
        val named = original.copy(identity = MoveIdentity(
            aliases = listOf(MoveAlias("Mein Oberhau", "Eigene Schule", "Notiz")),
            preferredAlias = "Mein Oberhau"
        ))

        assertEquals("oberhau", named.id)
        assertEquals(original.displayName, named.displayName)
        assertEquals("Mein Oberhau", named.trainingName)
        assertEquals(original.displayName, named.copy(identity = named.identity.copy(preferredAlias = null)).trainingName)
    }

    @Test
    fun missingPreferredAliasFallsBackToOriginalName() {
        val original = DEFAULT_MOVES.first { it.id == "oberhau" }
        val named = original.copy(identity = MoveIdentity(preferredAlias = "Gelöschter Name"))

        assertEquals(original.displayName, named.trainingName)
    }
}
