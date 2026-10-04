package de.stoupas.xkcd.platform

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toOkioPath

fun createAndroidImageLoader(context: Context, client: HttpClient = HttpClient(OkHttp)): ImageLoader =
    ImageLoader.Builder(context.applicationContext)
        .components { add(KtorNetworkFetcherFactory(client)) }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("comic_images").toOkioPath())
                .maxSizeBytes(100L * 1024 * 1024)
                .build()
        }
        .build()

class AndroidImageCacheService(private val loader: ImageLoader) : ImageCacheService {
    override suspend fun stats(): CacheStats = withContext(Dispatchers.IO) {
        CacheStats(loader.memoryCache?.size ?: 0, loader.diskCache?.size ?: 0, null)
    }

    override suspend fun clear(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            loader.memoryCache?.clear()
            loader.diskCache?.clear()
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            Result.failure(failure)
        }
    }
}
