package de.stoupas.xkcd.build

import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

class ReleaseConfiguration(
    val storeFile: Path,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
) {
    override fun toString() = "ReleaseConfiguration(signing details redacted)"
}

fun validateReleaseConfiguration(properties: Properties, baseDir: Path, versionCode: Int, previousVersionCode: Int?): ReleaseConfiguration {
    fun required(key: String): String = properties.getProperty(key)?.takeIf { it.isNotBlank() }
        ?: throw IllegalArgumentException("Release blocked: missing signing property $key in android/key.properties")
    val path = required("storeFile")
    val storePassword = required("storePassword")
    val alias = required("keyAlias")
    val keyPassword = required("keyPassword")
    require(previousVersionCode != null && previousVersionCode > 0) { "Release blocked: published versionCode must be confirmed first" }
    require(versionCode > previousVersionCode) { "Release blocked: versionCode must exceed the confirmed published version" }
    val store = baseDir.resolve(path).normalize()
    require(Files.isRegularFile(store)) { "Release blocked: original signing keystore does not exist" }
    return ReleaseConfiguration(store, storePassword, alias, keyPassword)
}
