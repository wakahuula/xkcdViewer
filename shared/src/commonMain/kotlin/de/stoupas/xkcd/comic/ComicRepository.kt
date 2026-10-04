package de.stoupas.xkcd.comic

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class ComicRepository(private val api: ComicApi) {
    private val cache = linkedMapOf<Int, Comic>()
    private val mutex = Mutex()
    var latestKnownId: Int? = null
        private set

    suspend fun latest(): Result<Comic> {
        val result = api.latest()
        result.getOrNull()?.let { comic ->
            mutex.withLock {
                latestKnownId = comic.num
                remember(comic)
            }
        }
        return result
    }

    suspend fun byId(id: Int): Result<Comic> {
        if (id <= 0) return Result.failure(ComicFailure.InvalidNumber())
        if (id == 404) return Result.failure(ComicFailure.NotFound())
        mutex.withLock {
            cache.remove(id)?.let {
                cache[id] = it
                return Result.success(it)
            }
        }
        val result = api.byId(id)
        result.getOrNull()?.let { mutex.withLock { remember(it) } }
        return result
    }

    suspend fun random(random: Random): Result<Comic> {
        val latest = latestKnownId ?: latest().getOrElse { return Result.failure(it) }.num
        val count = latest - if (latest >= 404) 1 else 0
        val sampled = random.nextInt(count) + 1
        return byId(if (sampled >= 404) sampled + 1 else sampled)
    }

    private fun remember(comic: Comic) {
        cache.remove(comic.num)
        cache[comic.num] = comic
        while (cache.size > 128) cache.remove(cache.keys.first())
    }
}
