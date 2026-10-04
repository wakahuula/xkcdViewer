package de.stoupas.xkcd.settings

import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SettingsRepositoryTest {
    @Test
    fun defaultsAreDurableAndWritesWaitForInitialization() = runTest {
        val storage = MemoryStorage()
        val repository = SettingsRepository(storage)
        assertTrue(repository.setTheme(false).isFailure)
        assertNull(storage.json)
        assertTrue(repository.initialize(LegacySettings()).isSuccess)
        assertEquals(UserSettings(), repository.state.value)
        val restarted = SettingsRepository(storage)
        assertTrue(restarted.initialize(LegacySettings(darkTheme = false)).isSuccess)
        assertTrue(restarted.state.value.darkTheme)
    }

    @Test
    fun importsValidUniqueFavoritesInOriginalOrder() = runTest {
        val repository = SettingsRepository(MemoryStorage())
        repository.initialize(LegacySettings(favorites = listOf(12, 25, 12, 0, -1, 404)))
        assertEquals(listOf(12, 25), repository.state.value.favorites)
    }

    @Test
    fun existingFieldsIncludingDefaultsWinOverLegacy() = runTest {
        val storage = MemoryStorage("""{"schemaVersion":1,"darkTheme":true,"dataSaver":false,"favorites":[]}""")
        val repository = SettingsRepository(storage)
        assertTrue(repository.initialize(LegacySettings(false, 0xFF112233, true, listOf(12))).isSuccess)
        assertEquals(UserSettings(accentArgb = 0xFF112233), repository.state.value)
    }

    @Test
    fun failedImportCanRestartWithoutOverwritingOrLosingLegacy() = runTest {
        val storage = MemoryStorage().apply { fail = true }
        val repository = SettingsRepository(storage)
        val legacy = LegacySettings(false, 0xFF112233, true, listOf(25))
        assertTrue(repository.initialize(legacy).isFailure)
        assertNull(storage.json)
        assertEquals(UserSettings(), repository.state.value)
        assertTrue(repository.toggleFavorite(12).isFailure)
        storage.fail = false
        assertTrue(repository.initialize(legacy).isSuccess)
        assertEquals(UserSettings(false, 0xFF112233, true, listOf(25)), repository.state.value)
        val restarted = SettingsRepository(storage)
        restarted.initialize(LegacySettings(favorites = listOf(12)))
        assertEquals(listOf(25), restarted.state.value.favorites)
    }

    @Test
    fun failedWriteDoesNotPublishUncommittedState() = runTest {
        val storage = MemoryStorage()
        val repository = SettingsRepository(storage)
        repository.initialize(LegacySettings())
        val snapshot = storage.json
        storage.fail = true
        assertTrue(repository.setDataSaver(true).isFailure)
        assertFalse(repository.state.value.dataSaver)
        assertEquals(snapshot, storage.json)
    }

    @Test
    fun persistsEverySettingAndFavoriteOperation() = runTest {
        val storage = MemoryStorage()
        val repository = SettingsRepository(storage)
        repository.initialize(LegacySettings())
        repository.setTheme(false)
        repository.setAccent(0xFF334455)
        repository.setDataSaver(true)
        repository.toggleFavorite(12)
        repository.toggleFavorite(25)
        repository.toggleFavorite(12)
        assertEquals(listOf(25), repository.state.value.favorites)
        repository.removeFavorite(25)
        repository.toggleFavorite(99)
        repository.clearFavorites()
        val restarted = SettingsRepository(storage)
        restarted.initialize(LegacySettings())
        assertEquals(UserSettings(false, 0xFF334455, true), restarted.state.value)
        assertTrue(repository.toggleFavorite(404).isFailure)
        assertTrue(repository.toggleFavorite(0).isFailure)
        assertTrue(repository.setAccent(-1).isFailure)
        assertTrue(repository.setAccent(0x1FFFFFFFF).isFailure)
    }

    @Test
    fun concurrentChangesDoNotLoseFavorites() = runTest {
        val repository = SettingsRepository(MemoryStorage())
        repository.initialize(LegacySettings())
        val a = async { repository.toggleFavorite(12) }
        val b = async { repository.toggleFavorite(25) }
        a.await().getOrThrow()
        b.await().getOrThrow()
        assertEquals(listOf(12, 25), repository.state.value.favorites)
    }

    @Test
    fun malformedOrFutureSnapshotIsNotOverwritten() = runTest {
        for (source in listOf("broken", """{"schemaVersion":2}""", """{"darkTheme":"false"}""")) {
            val storage = MemoryStorage(source)
            val repository = SettingsRepository(storage)
            assertTrue(repository.initialize(LegacySettings()).isFailure)
            assertEquals(source, storage.json)
            assertTrue(repository.setTheme(false).isFailure)
        }
    }

    private class MemoryStorage(var json: String? = null) : SettingsStorage {
        var fail = false
        override fun read() = json
        override suspend fun write(json: String): Result<Unit> {
            if (fail) return Result.failure(IllegalStateException("Disk unavailable"))
            this.json = json
            return Result.success(Unit)
        }
    }
}
