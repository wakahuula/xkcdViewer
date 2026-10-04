package de.stoupas.xkcd.comic

import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.*

class ComicRepositoryTest {
    @Test
    fun reusesMetadataButRefreshesLatestExplicitly() = runTest {
        val api = RecordingApi()
        val repository = ComicRepository(api)
        repository.byId(12).getOrThrow()
        repository.byId(12).getOrThrow()
        assertEquals(listOf(12), api.ids)
        assertEquals(500, repository.latest().getOrThrow().num)
        api.latestNumber = 501
        assertEquals(501, repository.latest().getOrThrow().num)
        assertEquals(501, repository.latestKnownId)
    }

    @Test
    fun leastRecentlyUsedEntryIsEvictedAt128() = runTest {
        val api = RecordingApi()
        val repository = ComicRepository(api)
        for (id in 1..128) repository.byId(id).getOrThrow()
        repository.byId(1).getOrThrow()
        repository.byId(129).getOrThrow()
        repository.byId(1).getOrThrow()
        repository.byId(2).getOrThrow()
        assertEquals(1, api.ids.count { it == 1 })
        assertEquals(2, api.ids.count { it == 2 })
    }

    @Test
    fun randomStaysInBoundsAndNeverRequests404() = runTest {
        val api = RecordingApi()
        val repository = ComicRepository(api)
        val random = Random(1)
        repeat(1500) {
            val id = repository.random(random).getOrThrow().num
            assertTrue(id in 1..500 && id != 404)
        }
    }

    @Test
    fun apiFailureDoesNotFallbackToRandomAndInvalidIdsDoNotRequest() = runTest {
        val api = RecordingApi().apply { fail = true }
        val repository = ComicRepository(api)
        assertIs<ComicFailure.InvalidNumber>(repository.byId(-1).exceptionOrNull())
        assertTrue(api.ids.isEmpty())
        assertIs<ComicFailure.Network>(repository.random(Random(1)).exceptionOrNull())
        assertTrue(api.ids.isEmpty())
    }

    private class RecordingApi : ComicApi {
        val ids = mutableListOf<Int>()
        var latestNumber = 500
        var fail = false
        override suspend fun latest(): Result<Comic> = if (fail) Result.failure(ComicFailure.Network()) else Result.success(comic(latestNumber))
        override suspend fun byId(id: Int): Result<Comic> {
            ids.add(id)
            return Result.success(comic(id))
        }
        private fun comic(id: Int) = Comic(id, "Comic $id", "Alt", "https://imgs.xkcd.com/comics/test.png", "2006", "1", "1")
    }
}
