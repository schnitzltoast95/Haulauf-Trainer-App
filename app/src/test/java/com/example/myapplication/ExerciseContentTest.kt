package com.example.myapplication

import com.example.myapplication.data.*
import org.junit.Assert.*
import org.junit.Test

class ExerciseContentTest {
    @Test fun legacyDefaultOverridesBecomeAdditionsWithoutReplacingTheSource() {
        val original = DEFAULT_MOVES.first()
        val result = original.withPersonalContent("My old text | ü" to "/old/image.gif", null)
        assertEquals(original.description, result.description)
        assertEquals(original.historicalImageRes, result.historicalImageRes)
        assertEquals(original.historicalTextSources, result.historicalTextSources)
        assertEquals("My old text | ü", result.notes)
        assertEquals(listOf(ExerciseMedia("/old/image.gif")), result.media)
        assertNull(result.imagePath)
    }

    @Test fun savingEmptyAdditionsDoesNotResurrectLegacyData() {
        val original = DEFAULT_MOVES.first()
        val result = original.withPersonalContent("Old override" to "/old/image", ExerciseAdditions("", emptyList()))
        assertEquals("", result.notes)
        assertTrue(result.media.isEmpty())
        assertEquals(original.description, result.description)
    }

    @Test fun unchangedOriginalDescriptionDoesNotBecomeANote() {
        val original = DEFAULT_MOVES.first()
        assertEquals("", original.withPersonalContent(original.description to null, null).notes)
    }

    @Test fun customExercisesKeepEditableDescriptionsAndOrderedAttachments() {
        val media = listOf(ExerciseMedia("/image"), ExerciseMedia("/video", ExerciseMediaKind.VIDEO), ExerciseMedia("/animation"))
        val result = Move("custom_test", "Test").withPersonalContent("Description" to null, ExerciseAdditions("Notes", media))
        assertEquals("Description", result.description)
        assertEquals("Notes", result.notes)
        assertEquals(media, result.media)
    }
}
