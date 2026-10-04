package de.stoupas.xkcd.favorites

import androidx.lifecycle.viewModelScope
import de.stoupas.xkcd.comic.*
import de.stoupas.xkcd.settings.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    private suspend fun settings(ids: List<Int>) = SettingsRepository(object : SettingsStorage {
        override fun read(): String? = null
        override suspend fun write(json: String) = Result.success(Unit)
    }).also { it.initialize(LegacySettings(favorites = ids)).getOrThrow() }

    @Test
    fun partialFailureKeepsOtherFavoritesAndRemovalDoesNotReload() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val settings = settings(listOf(12, 25, 30))
        val requests = mutableListOf<Int>()
        val api = object : ComicApi {
            override suspend fun latest() = Result.success(comic(30))
            override suspend fun byId(id: Int): Result<Comic> {
                requests.add(id)
                return if (id == 25) Result.failure(ComicFailure.Network()) else Result.success(comic(id))
            }
        }
        val vm = FavoritesViewModel(ComicRepository(api), settings)
        try {
            advanceUntilIdle()
            assertEquals(listOf(12, 25, 30), vm.items.value.map { it.id })
            assertEquals(listOf(12, 30), vm.items.value.mapNotNull { it.comic?.num })
            assertIs<ComicFailure.Network>(vm.items.value[1].failure)
            assertFalse(vm.loading.value, "Immediate responses must complete loading")
            settings.removeFavorite(25).getOrThrow(); advanceUntilIdle()
            assertEquals(listOf(12, 30), vm.items.value.map { it.id })
            assertEquals(listOf(12, 25, 30), requests)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun atMostFourRequestsAreActiveAndEmptyListPublishesEmptyState() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val settings = settings((1..12).toList())
        var active = 0
        var peak = 0
        val api = object : ComicApi {
            override suspend fun latest() = Result.success(comic(12))
            override suspend fun byId(id: Int): Result<Comic> {
                active++; peak = maxOf(peak, active)
                try { delay(100); return Result.success(comic(id)) } finally { active-- }
            }
        }
        val vm = FavoritesViewModel(ComicRepository(api), settings)
        try {
            advanceUntilIdle()
            assertEquals(4, peak)
            assertFalse(vm.loading.value)
            assertEquals(12, vm.items.value.size)
            settings.clearFavorites().getOrThrow(); advanceUntilIdle()
            assertTrue(vm.items.value.isEmpty())
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    private fun comic(id: Int) = Comic(id, "Comic $id", "Alt", "https://example.test/$id.png", "2014", "1", "1")
}
