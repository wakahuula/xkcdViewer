package de.stoupas.xkcd.build

import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LegacyResourceGeneratorTest {
    @get:Rule
    val temporary = TemporaryFolder()

    private val assets: Path get() = Path.of(System.getProperty("xkcd.assets"))

    @Test
    fun preservesAllOriginalTranslationsAndAssets() {
        val output = temporary.newFolder("output").toPath()
        generateLegacyResources(assets, output)

        val locales = listOf("de-DE", "en-US", "es-ES", "fr-FR", "hu-HU", "ja-JP", "pt-BR", "ru-RU", "uk-UA")
        for (locale in locales) {
            val original = Json.parseToJsonElement(Files.readString(assets.resolve("locale/$locale.json"))).jsonObject
                .mapValues { it.value.jsonPrimitive.content }
            assertEquals(32, original.size)
            val (language, region) = locale.split('-')
            assertEquals(original, readStrings(output.resolve("values-$language-r$region/strings.xml")))
            assertEquals(original, readStrings(output.resolve("values-$language/strings.xml")))
        }
        assertEquals("Built with ❤ and Flutter by Kosta Stoupas.", readStrings(output.resolve("values/strings.xml"))["built_by"])
        assertArrayEquals(Files.readAllBytes(assets.resolve("contributors.json")), Files.readAllBytes(output.resolve("files/contributors.json")))
        for ((source, generated) in listOf("Regular" to "regular", "Medium" to "medium", "Bold" to "bold")) {
            assertArrayEquals(Files.readAllBytes(assets.resolve("fonts/FiraMono-$source.ttf")), Files.readAllBytes(output.resolve("font/fira_mono_$generated.ttf")))
        }
    }

    @Test
    fun preservesUnicodeQuotesWhitespaceAndXmlCharacters() {
        val input = fixture("""{"built_by":"❤ & <tag> \"quoted\" 'single'\\path\nnext\tend", "spaced":"  padded  ", "numeric":"123"}""")
        val output = temporary.newFolder("escaped").toPath()
        generateLegacyResources(input, output)

        val strings = readStrings(output.resolve("values/strings.xml"))
        assertEquals("❤ & <tag> \"quoted\" 'single'\\path\nnext\tend", strings["built_by"])
        assertEquals("  padded  ", strings["spaced"])
        assertEquals("123", strings["numeric"])
    }

    @Test
    fun missingTranslationFallsBackToEnglish() {
        val input = fixture("""{"download":"Download", "settings":"Settings"}""")
        Files.writeString(input.resolve("locale/de-DE.json"), """{"settings":"Einstellungen"}""")
        val output = temporary.newFolder("fallback").toPath()
        generateLegacyResources(input, output)

        assertEquals(mapOf("download" to "Download", "settings" to "Einstellungen"), readStrings(output.resolve("values-de/strings.xml")))
    }

    @Test
    fun removesStaleGeneratedLocalesOnNextRun() {
        val input = fixture("""{"settings":"Settings"}""")
        Files.writeString(input.resolve("locale/de-DE.json"), """{"settings":"Einstellungen"}""")
        val output = temporary.newFolder("stale").toPath()
        generateLegacyResources(input, output)
        Files.delete(input.resolve("locale/de-DE.json"))
        generateLegacyResources(input, output)

        assertFalse(Files.exists(output.resolve("values-de")))
        assertEquals("Settings", readStrings(output.resolve("values/strings.xml"))["settings"])
    }

    @Test
    fun invalidTranslationDoesNotProducePartialResources() {
        val input = fixture("""{"settings":123}""")
        val output = temporary.newFolder("invalid").toPath()
        val failure = assertThrows(IllegalArgumentException::class.java) { generateLegacyResources(input, output) }
        assertTrue(failure.message.orEmpty().contains("settings"))
        assertFalse(Files.exists(output.resolve("values/strings.xml")))
    }

    @Test
    fun verificationDetectsMissingGeneratedTranslation() {
        val input = fixture("""{"settings":"Settings"}""")
        val output = temporary.newFolder("verification").toPath()
        generateLegacyResources(input, output)
        verifyLegacyResources(input, output)
        Files.writeString(output.resolve("values/strings.xml"), "<resources/>")
        assertThrows(IllegalArgumentException::class.java) { verifyLegacyResources(input, output) }
    }

    @Test
    fun refusesToWriteInsideOriginalAssets() {
        val input = fixture("""{"settings":"Settings"}""")
        assertThrows(IllegalArgumentException::class.java) { generateLegacyResources(input, input.resolve("generated")) }
        assertEquals("""{"settings":"Settings"}""", Files.readString(input.resolve("locale/en-US.json")))
    }

    private fun fixture(english: String): Path {
        val input = temporary.newFolder().toPath()
        Files.createDirectories(input.resolve("locale"))
        Files.writeString(input.resolve("locale/en-US.json"), english)
        Files.createDirectories(input.resolve("fonts"))
        for (weight in listOf("Regular", "Medium", "Bold")) {
            Files.write(input.resolve("fonts/FiraMono-$weight.ttf"), byteArrayOf(1, 2, 3))
        }
        Files.writeString(input.resolve("contributors.json"), """{"contributors":[]}""")
        return input
    }

    private fun readStrings(file: Path): Map<String, String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.toFile())
        val nodes = document.getElementsByTagName("string")
        return (0 until nodes.length).associate { index ->
            val node = nodes.item(index)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }
    }
}
