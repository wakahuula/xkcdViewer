package de.stoupas.xkcd.platform

data class CacheStats(val memoryBytes: Long, val diskBytes: Long, val entryCount: Int?)

interface ImageCacheService {
    suspend fun stats(): CacheStats
    suspend fun clear(): Result<Unit>
}
