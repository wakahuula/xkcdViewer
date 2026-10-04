package de.stoupas.xkcd.settings

import android.content.Context
import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.util.Date
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LegacyPreferencesMigrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext

    @Test
    fun importsFixedPluginFixtureAndLeavesOriginalFileUntouched() = runTest {
        // Install only in this dedicated test application's private preferences.
        val folder = java.io.File(context.applicationInfo.dataDir, "shared_prefs").apply { mkdirs() }
        val file = java.io.File(folder, "legacy_fixture.xml")
        val original = instrumentation.context.assets.open("legacy_preferences.xml").use { it.readBytes() }
        file.writeBytes(original)
        val legacy = FlutterPreferencesReader(context.getSharedPreferences("legacy_fixture", Context.MODE_PRIVATE)).read()
        assertEquals(LegacySettings(false, 0xFF112233, true, listOf(12, 25, 12, 0, 404)), legacy)
        val repository = SettingsRepository(AndroidSettingsStorage(context.getSharedPreferences("new_fixture", Context.MODE_PRIVATE)))
        repository.initialize(legacy).getOrThrow()
        assertEquals(UserSettings(false, 0xFF112233, true, listOf(12, 25)), repository.state.value)
        assertArrayEquals(original, file.readBytes())
    }

    @Test
    fun wrongTypesAreSkippedAndOlderSetsAreAccepted() {
        val prefs = context.getSharedPreferences("legacy_types", Context.MODE_PRIVATE)
        prefs.edit().clear().putString("flutter.isDark", "false")
            .putInt("flutter.accentColor", 123).putString("flutter.data_saver", "true")
            .putStringSet("flutter.favorites", setOf("25", "12", "bad")).commit()
        assertEquals(LegacySettings(favorites = listOf(12, 25)), FlutterPreferencesReader(prefs).read())
    }

    @Test
    fun decoderRejectsCorruptionOversizedInputAndUnapprovedClasses() {
        assertNull(LegacyStringListDecoder.decode("VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu!!!"))
        assertNull(LegacyStringListDecoder.decode("x".repeat(1024 * 1024 + 1)))
        val bytes = ByteArrayOutputStream()
        ObjectOutputStream(bytes).use { it.writeObject(Date()) }
        val hostile = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu" + Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP)
        assertNull(LegacyStringListDecoder.decode(hostile))
    }

    @Test
    fun failedAtomicCommitBlocksWritesAndRetryPersistsSnapshot() = runTest {
        val prefs = context.getSharedPreferences("failed_import", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        val failed = SettingsRepository(AndroidSettingsStorage(prefs) { false })
        assertTrue(failed.initialize(LegacySettings(favorites = listOf(12))).isFailure)
        assertTrue(failed.setTheme(false).isFailure)
        assertFalse(prefs.contains("settings_snapshot"))
        val restarted = SettingsRepository(AndroidSettingsStorage(prefs))
        restarted.initialize(LegacySettings(favorites = listOf(12))).getOrThrow()
        restarted.setTheme(false).getOrThrow()
        val next = SettingsRepository(AndroidSettingsStorage(prefs))
        next.initialize(LegacySettings(darkTheme = true, favorites = listOf(25))).getOrThrow()
        assertEquals(UserSettings(darkTheme = false, favorites = listOf(12)), next.state.value)
    }
}
