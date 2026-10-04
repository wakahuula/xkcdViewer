package de.stoupas.xkcd.build

import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

private val localePattern = Regex("([a-z]{2,3})-([A-Z]{2})\\.json")
private val resourceNamePattern = Regex("[a-z][a-z0-9_]*")
private val fonts = mapOf(
    "FiraMono-Regular.ttf" to "fira_mono_regular.ttf",
    "FiraMono-Medium.ttf" to "fira_mono_medium.ttf",
    "FiraMono-Bold.ttf" to "fira_mono_bold.ttf",
)

fun generateLegacyResources(inputDir: Path, outputDir: Path) {
    val input = inputDir.toAbsolutePath().normalize()
    val output = outputDir.toAbsolutePath().normalize()
    require(!output.startsWith(input) && !input.startsWith(output)) {
        "Generated resources must be separate from the original assets: $output"
    }
    require(!Files.isSymbolicLink(output)) { "Generated resource directory must not be a symbolic link" }

    // Validate all source files before replacing any generated output.
    val locales = readLocales(input)
    for (name in fonts.keys) {
        require(Files.isRegularFile(input.resolve("fonts/$name"))) { "Missing source asset: $name" }
    }
    require(Files.isRegularFile(input.resolve("contributors.json"))) { "Missing source asset: contributors.json" }
    if (Files.exists(output)) {
        Files.walk(output).use { paths ->
            paths.sorted(Comparator.reverseOrder()).forEach(Files::delete)
        }
    }

    for ((directory, strings) in localizedDirectories(locales)) {
        val destination = output.resolve(directory)
        Files.createDirectories(destination)
        Files.writeString(destination.resolve("strings.xml"), stringsXml(strings))
    }
    Files.createDirectories(output.resolve("font"))
    for ((source, destination) in fonts) {
        Files.copy(input.resolve("fonts/$source"), output.resolve("font/$destination"))
    }
    Files.createDirectories(output.resolve("files"))
    Files.copy(input.resolve("contributors.json"), output.resolve("files/contributors.json"))
}

fun verifyLegacyResources(inputDir: Path, outputDir: Path) {
    for ((directory, expected) in localizedDirectories(readLocales(inputDir))) {
        val file = outputDir.resolve("$directory/strings.xml")
        require(Files.isRegularFile(file)) { "Missing generated resource: $file" }
        val factory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
        }
        val nodes = factory.newDocumentBuilder().parse(file.toFile()).getElementsByTagName("string")
        val actual = (0 until nodes.length).associate { index ->
            val node = nodes.item(index)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }
        require(expected == actual) { "Generated translations differ from their sources: $file" }
    }
    for ((source, destination) in fonts) {
        require(Files.readAllBytes(inputDir.resolve("fonts/$source")).contentEquals(Files.readAllBytes(outputDir.resolve("font/$destination")))) {
            "Generated font differs from its source: $source"
        }
    }
    require(Files.readAllBytes(inputDir.resolve("contributors.json")).contentEquals(Files.readAllBytes(outputDir.resolve("files/contributors.json")))) {
        "Generated contributors differ from their source"
    }
}

private fun readLocales(inputDir: Path): Map<String, Map<String, String>> {
    val locales = Files.list(inputDir.resolve("locale")).use { paths ->
        paths.filter { it.fileName.toString().endsWith(".json") }.sorted().toList().associate { file ->
            val name = file.fileName.toString()
            require(localePattern.matches(name)) { "Unsupported locale filename: $name" }
            val strings = Json.parseToJsonElement(Files.readString(file)).jsonObject.mapValues { (key, value) ->
                require(resourceNamePattern.matches(key)) { "Invalid resource key '$key' in $name" }
                require(value is JsonPrimitive && value.isString) { "Translation '$key' in $name must be a string" }
                value.content
            }
            name.removeSuffix(".json") to strings
        }
    }
    val english = requireNotNull(locales["en-US"]) { "Missing default locale en-US.json" }
    for ((locale, strings) in locales) {
        require(english.keys.containsAll(strings.keys)) { "Locale $locale contains a key without an English fallback" }
    }
    return locales
}

private fun localizedDirectories(locales: Map<String, Map<String, String>>): Map<String, Map<String, String>> {
    val english = locales.getValue("en-US")
    val result = linkedMapOf("values" to english)
    for ((locale, strings) in locales) {
        val (language, region) = locale.split('-')
        val merged = english + strings
        result["values-$language-r$region"] = merged
        // A regionless device uses the first deterministic locale for its language.
        result.putIfAbsent("values-$language", merged)
    }
    result["values-en"] = english
    return result
}

private fun stringsXml(strings: Map<String, String>): String = buildString {
    append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n")
    for ((key, value) in strings.toSortedMap()) {
        append("    <string name=\"").append(key).append("\">")
        append(value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\r", "&#13;"))
        append("</string>\n")
    }
    append("</resources>\n")
}
