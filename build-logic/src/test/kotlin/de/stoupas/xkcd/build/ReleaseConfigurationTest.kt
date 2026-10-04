package de.stoupas.xkcd.build

import java.nio.file.Files
import java.util.Properties
import org.junit.Assert.*
import org.junit.Test

class ReleaseConfigurationTest {
    private fun properties() = Properties().apply {
        setProperty("storeFile", "signing/original.jks")
        setProperty("storePassword", "PRIVATE_STORE")
        setProperty("keyAlias", "original")
        setProperty("keyPassword", "PRIVATE_KEY")
    }

    @Test fun missingSigningFieldsRejectReleaseWithoutExposingSecrets() {
        val base = Files.createTempDirectory("xkcd-release-test")
        try {
            for (key in listOf("storeFile", "storePassword", "keyAlias", "keyPassword")) {
                val values = properties().apply { remove(key) }
                val failure = runCatching { validateReleaseConfiguration(values, base, 14, 13) }.exceptionOrNull()!!
                assertFalse(failure.message.orEmpty().contains("PRIVATE_STORE"))
                assertFalse(failure.message.orEmpty().contains("PRIVATE_KEY"))
                assertTrue(failure.message.orEmpty().contains(key))
            }
        } finally { Files.delete(base) }
    }

    @Test fun relativeStoreFileUsesHistoricalAndroidDirectoryAndVersionMustAdvance() {
        val base = Files.createTempDirectory("xkcd-release-test")
        val dir = Files.createDirectory(base.resolve("signing"))
        val key = Files.createFile(dir.resolve("original.jks"))
        try {
            val valid = validateReleaseConfiguration(properties(), base, 14, 13)
            assertEquals(key, valid.storeFile)
            for (previous in listOf(null, 14, 15)) {
                assertTrue(runCatching { validateReleaseConfiguration(properties(), base, 14, previous) }.isFailure)
            }
            assertTrue(runCatching { validateReleaseConfiguration(properties(), base, 0, 0) }.isFailure)
        } finally { Files.delete(key); Files.delete(dir); Files.delete(base) }
    }

    @Test fun missingKeyFileDoesNotPermitRelease() {
        val base = Files.createTempDirectory("xkcd-release-test")
        try { assertTrue(runCatching { validateReleaseConfiguration(properties(), base, 14, 13) }.isFailure) }
        finally { Files.delete(base) }
    }
}
