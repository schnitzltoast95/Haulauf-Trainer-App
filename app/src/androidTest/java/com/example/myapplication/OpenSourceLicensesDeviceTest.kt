package com.example.myapplication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.loadOpenSourceCatalog
import com.example.myapplication.ui.OpenSourceLicensesScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class OpenSourceLicensesDeviceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun bundledCatalogIncludesTransitiveNoticesAndOfflineSourceData() {
        val catalog = loadOpenSourceCatalog(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        assertTrue(catalog.libraries.size > 50)
        assertTrue(catalog.libraries.none { it.id.contains("junit") || it.id.contains("espresso") })
        val okhttp = catalog.libraries.single { it.id.startsWith("com.squareup.okhttp3:okhttp:") }
        assertTrue(okhttp.licenses.any { catalog.texts.getValue(it).contains("Mozilla Public License") })
        assertTrue(okhttp.notices.any { catalog.texts.getValue(it).contains("publicsuffix.org") })
        assertTrue(okhttp.sources.any { catalog.texts.getValue(it).contains("!www.ck") })
        catalog.libraries.forEach { library ->
            assertTrue(library.licenses.isNotEmpty())
            assertTrue(library.url.startsWith("http"))
            (library.licenses + library.notices + library.sources).forEach { id ->
                assertTrue(catalog.texts.getValue(id).isNotBlank())
            }
        }
    }

    @Test fun searchDetailsAndBackPreserveSearch() {
        var exits = 0
        compose.setContent { MyApplicationTheme(darkTheme = true) {
            OpenSourceLicensesScreen(german = true, onBack = { exits++ })
        } }
        compose.waitUntil(10000) { compose.onAllNodesWithText("Bibliothek suchen").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Bibliothek suchen").performTextInput("okhttp")
        compose.onNode(hasText("okhttp") and !hasSetTextAction()).performClick()
        compose.onNodeWithText("Projektwebseite öffnen").assertExists()
        compose.onAllNodesWithText("Apache License", substring = true).onFirst().assertExists()
        compose.onNodeWithContentDescription("Zurück").performClick()
        compose.onNodeWithText("Bibliothek suchen").assertExists()
        assertEquals(0, exits)
        compose.onNodeWithText("Bibliothek suchen").performTextReplacement("no-such-library")
        compose.onNodeWithText("Keine passende Bibliothek gefunden.").assertExists()
        compose.onNodeWithContentDescription("Zurück").performClick()
        compose.runOnIdle { assertEquals(1, exits) }
    }
}
