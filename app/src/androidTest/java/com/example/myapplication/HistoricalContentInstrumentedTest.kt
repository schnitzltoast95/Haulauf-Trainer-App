package com.example.myapplication

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import com.example.myapplication.data.DEFAULT_MOVES
import com.example.myapplication.data.MEYER_IMAGE_RIGHTS_URL
import com.example.myapplication.data.historicalImageUrl
import com.example.myapplication.ui.LocalStrings
import com.example.myapplication.ui.MoveInfoDialog
import com.example.myapplication.ui.StringsDe
import com.example.myapplication.ui.StringsEn
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class HistoricalContentInstrumentedTest {
    @get:Rule val compose = createComposeRule()

    @Before fun interceptExternalLinks() {
        Intents.init()
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
    }

    @After fun releaseIntentMonitor() { Intents.release() }

    @Test
    fun everyDefaultMoveLoadsItsImageAndOpensAllCitedSources() {
        val current = mutableStateOf(DEFAULT_MOVES.first())
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent {
            CompositionLocalProvider(LocalStrings provides StringsDe) {
                MyApplicationTheme(darkTheme = true) { MoveInfoDialog(current.value) {} }
            }
        }
        DEFAULT_MOVES.forEach { move ->
            val bitmap = BitmapFactory.decodeResource(context.resources, move.historicalImageRes!!)
            assertNotNull("Image for ${move.id}", bitmap)
            bitmap.recycle()
            val previousIntents = Intents.getIntents().size
            compose.runOnIdle { current.value = move }
            compose.onNodeWithText(move.displayName).performScrollTo().assertIsDisplayed()
            if (move.id in setOf("twerhau", "pflug", "halber_stich")) {
                val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
                val file = File(context.getExternalFilesDir(null), "historical-${move.id}.png")
                file.outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 100, it) }
                screenshot.recycle()
            }
            compose.onNodeWithText(move.description!!).performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("Originalscan · Bayerische Staatsbibliothek ↗").performScrollTo().performClick()
            compose.onNodeWithText("Public Domain Mark 1.0 ↗").performScrollTo().performClick()
            move.historicalTextSources.indices.forEach { index ->
                compose.onAllNodesWithText("Originaltext · BSB ↗")[index].performScrollTo().performClick()
                compose.onAllNodesWithText("Transkription · Wiktenauer ↗")[index].performScrollTo().performClick()
            }
            compose.waitForIdle()
            assertEquals("Source buttons for ${move.id}",
                listOf(move.historicalImageUrl(), MEYER_IMAGE_RIGHTS_URL) +
                    move.historicalTextSources.flatMap { listOf(it.originalUrl(), it.transcriptionUrl()) },
                Intents.getIntents().drop(previousIntents).filter { it.action == Intent.ACTION_VIEW }.map { it.dataString })
        }
    }

    @Test
    fun customImageAndDescriptionKeepTheModernDrillQualificationInBothLanguages() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val image = File(context.cacheDir, "historical-test-custom.jpg")
        context.resources.openRawResource(R.drawable.meyer_00031).use { input ->
            image.outputStream().use { input.copyTo(it) }
        }
        val german = mutableStateOf(true)
        val move = DEFAULT_MOVES.first { it.id == "halber_stich" }.copy(
            imagePath = image.absolutePath, description = "My own drill description"
        )
        try {
            compose.setContent {
                CompositionLocalProvider(LocalStrings provides if (german.value) StringsDe else StringsEn) {
                    MyApplicationTheme(darkTheme = true) { MoveInfoDialog(move) {} }
                }
            }
            compose.onNodeWithText("My own drill description").performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("Moderner Übungsname der App.", substring = true).performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("Originalscan · Bayerische Staatsbibliothek ↗").assertDoesNotExist()
            compose.runOnIdle { german.value = false }
            compose.onNodeWithText("Modern app drill name.", substring = true).performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("Original scan · Bavarian State Library ↗").assertDoesNotExist()
        } finally {
            image.delete()
        }
    }

    @Test
    fun missingCustomFileRestoresHistoricalImageAttribution() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val move = DEFAULT_MOVES.first { it.id == "pflug" }.copy(
            imagePath = File(context.cacheDir, "missing-historical-test-image.jpg").absolutePath
        )
        compose.setContent {
            CompositionLocalProvider(LocalStrings provides StringsDe) {
                MyApplicationTheme(darkTheme = true) { MoveInfoDialog(move) {} }
            }
        }
        compose.onNodeWithText("Tafel B: große Figur rechts = Pflug", substring = true)
            .performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Originalscan · Bayerische Staatsbibliothek ↗")
            .performScrollTo().assertIsDisplayed()
    }
}
