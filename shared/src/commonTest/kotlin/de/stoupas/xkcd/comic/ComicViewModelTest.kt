package de.stoupas.xkcd.comic

import androidx.lifecycle.viewModelScope
import de.stoupas.xkcd.settings.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ComicViewModelTest {
    @Test
    fun restoredOrDetailIdLoadsWithoutReplacingItWithLatest() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val handle = androidx.lifecycle.SavedStateHandle(mapOf("comic_id" to 25))
        val api = object : ComicApi {
            override suspend fun latest(): Result<Comic> = error("Must preserve restored selection")
            override suspend fun byId(id: Int) = Result.success(comic(id))
        }
        val vm = ComicViewModel(ComicRepository(api), settings(), savedState = handle, initialComicId = 12)
        try {
            runCurrent()
            assertEquals(25, vm.state.value.comic?.num)
            vm.select(30); runCurrent()
            assertEquals(30, handle.get<Int>("comic_id"))
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    private suspend fun settings(): SettingsRepository = SettingsRepository(object : SettingsStorage {
        var json: String? = null
        override fun read() = json
        override suspend fun write(json: String): Result<Unit> { this.json = json; return Result.success(Unit) }
    }).also { it.initialize(LegacySettings()).getOrThrow() }

    @Test
    fun delayedOlderResponseCannotReplaceLastSelection() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val responses = mapOf(12 to CompletableDeferred<Result<Comic>>(), 25 to CompletableDeferred<Result<Comic>>())
        val api = object : ComicApi {
            override suspend fun latest() = Result.success(comic(1))
            override suspend fun byId(id: Int) = withContext(NonCancellable) { responses.getValue(id).await() }
        }
        val vm = ComicViewModel(ComicRepository(api), settings())
        try {
            runCurrent()
            vm.select(12); runCurrent()
            vm.select(25); runCurrent()
            responses.getValue(25).complete(Result.success(comic(25))); runCurrent()
            assertEquals(25, vm.state.value.comic?.num)
            responses.getValue(12).complete(Result.success(comic(12))); runCurrent()
            assertEquals(25, vm.state.value.comic?.num)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun retryRepeatsFailedSelectionAndClearsFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var fails = true
        val api = object : ComicApi {
            override suspend fun latest() = Result.success(comic(1))
            override suspend fun byId(id: Int): Result<Comic> = if (fails) Result.failure(ComicFailure.Network()) else Result.success(comic(id))
        }
        val vm = ComicViewModel(ComicRepository(api), settings())
        try {
            runCurrent()
            vm.select(12); runCurrent()
            assertIs<ComicFailure.Network>(vm.state.value.failure)
            assertFalse(vm.state.value.loading)
            fails = false
            vm.retry(); runCurrent()
            assertEquals(12, vm.state.value.comic?.num)
            assertNull(vm.state.value.failure)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun selectionAndDataSaverChangesInvalidateResolvedImage() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val settings = settings()
        val api = object : ComicApi {
            override suspend fun latest() = Result.success(comic(2000))
            override suspend fun byId(id: Int) = Result.success(comic(id))
        }
        val vm = ComicViewModel(ComicRepository(api), settings)
        try {
            runCurrent()
            vm.resolvedImage("https://imgs.xkcd.com/comics/2000_2x.png")
            assertNotNull(vm.state.value.resolvedImageUrl)
            settings.setDataSaver(true); runCurrent()
            assertNull(vm.state.value.resolvedImageUrl)
            vm.resolvedImage("https://imgs.xkcd.com/comics/2000_2x.png")
            assertNull(vm.state.value.resolvedImageUrl)
            vm.resolvedImage(comic(2000).img)
            assertNotNull(vm.state.value.resolvedImageUrl)
            vm.select(25)
            assertNull(vm.state.value.resolvedImageUrl)
            vm.resolvedImage(comic(2000).img)
            assertNull(vm.state.value.resolvedImageUrl)
            runCurrent()
            vm.resolvedImage(comic(2000).img)
            assertNull(vm.state.value.resolvedImageUrl)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun favoriteTogglePersistsAndCancellationDoesNotShowFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val settings = settings()
        val api = object : ComicApi {
            override suspend fun latest() = Result.success(comic(12))
            override suspend fun byId(id: Int): Result<Comic> = throw CancellationException()
        }
        val vm = ComicViewModel(ComicRepository(api), settings)
        try {
            runCurrent()
            vm.toggleFavorite(); runCurrent()
            assertEquals(listOf(12), settings.state.value.favorites)
            vm.select(25); runCurrent()
            assertNull(vm.state.value.failure)
            assertFalse(vm.state.value.loading)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    private fun comic(id: Int) = Comic(id, "Comic $id", "Alt", "https://imgs.xkcd.com/comics/$id.png", "2014", "1", "1")
}
