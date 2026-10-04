package de.stoupas.xkcd.resources

import java.util.Locale
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalResourceApi::class)
class PackagedTranslationsTest {
    @Test
    fun newStringsArePackagedAlongsideLegacyStrings() = runTest {
        assertEquals("KMP foundation. Comic browsing is not available yet.", getString(Res.string.foundation_status))
    }

    @Test
    fun allOriginalStringsSurviveComposePackaging() = runTest {
        val previous = Locale.getDefault()
        try {
            for (locale in listOf("de-DE", "en-US", "es-ES", "fr-FR", "hu-HU", "ja-JP", "pt-BR", "ru-RU", "uk-UA")) {
                Locale.setDefault(Locale.forLanguageTag(locale))
                val source = javaClass.getResourceAsStream("/$locale.json")!!.bufferedReader().use { it.readText() }
                val expected = Json.parseToJsonElement(source).jsonObject
                for ((key, value) in expected) {
                    val resource = assertNotNull(Res.allStringResources[key], "Missing resource: $key")
                    assertEquals(value.jsonPrimitive.content, getString(resource), "$locale/$key")
                }
            }
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun languageFallbackWorksWithoutMatchingRegion() = runTest {
        val previous = Locale.getDefault()
        try {
            for ((locale, expected) in listOf("de-AT" to "Einstellungen", "uk" to "Налаштування", "zz-ZZ" to "Settings")) {
                Locale.setDefault(Locale.forLanguageTag(locale))
                assertEquals(expected, getString(Res.allStringResources.getValue("settings")), locale)
            }
        } finally {
            Locale.setDefault(previous)
        }
    }
}
