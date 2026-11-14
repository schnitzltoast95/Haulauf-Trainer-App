package com.example.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.myapplication.data.TrainingPreset
import com.example.myapplication.ui.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsAudioDeviceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun voiceSelectionIsExclusiveAndPreviewDoesNotChangeIt() {
        var previews = 0
        compose.setContent {
            var jonas by remember { mutableStateOf(true) }
            MyApplicationTheme(darkTheme = true) {
                Column(Modifier.selectableGroup()) {
                    VoiceChoice("Standardstimme", !jonas, true, { jonas = false }, { previews++ })
                    VoiceChoice("Jonas", jonas, true, { jonas = true }, { previews++ })
                }
            }
        }
        compose.onNodeWithText("Jonas").assertIsSelected()
        compose.onNodeWithText("Standardstimme").assertIsNotSelected()
        compose.onNodeWithContentDescription("Standardstimme anhören").performClick()
        compose.runOnIdle { assertEquals(1, previews) }
        compose.onNodeWithText("Jonas").assertIsSelected()
        compose.onNodeWithText("Standardstimme").performClick()
        compose.onNodeWithText("Standardstimme").assertIsSelected()
        compose.onNodeWithText("Jonas").assertIsNotSelected()
        compose.onAllNodesWithText("Ausgewählt").assertCountEquals(1)
    }

    @Test fun settingsExplainDefaultSpeedAndTrainingVolumes() {
        var restoreCalls = 0
        compose.setContent { MyApplicationTheme(darkTheme = true) {
            CompositionLocalProvider(LocalStrings provides StringsDe) {
                SettingsScreen(TrainingPreset(), 1.4f, {}, null, {}, "de", {}, {}, {},
                    legacyAudio = false, onUseLegacyAudio = { restoreCalls++ })
            }
        } }
        compose.onNodeWithText("Lautstärke").assertIsDisplayed()
        compose.onNodeWithText("Standardstimme").performScrollTo().assertIsSelected()
        compose.onNodeWithContentDescription("Standardstimme anhören").assertExists()
        compose.onNodeWithText("Stimme auswählen / anhören").assertDoesNotExist()
        compose.onNodeWithText("1,4 · Standard").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Lautstärken aus Trainings verwenden").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, restoreCalls) }
        compose.onNodeWithText("Darstellung").performScrollTo().assertIsDisplayed()
    }
}
