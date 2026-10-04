package de.stoupas.xkcd.comic

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

class KtorComicApi(client: HttpClient) : ComicApi, AutoCloseable {
    private val client = client.config {
        expectSuccess = false
        install(HttpTimeout) { requestTimeoutMillis = 15_000 }
    }
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun latest() = request("https://www.xkcd.com/info.0.json", null)
    override suspend fun byId(id: Int): Result<Comic> = when {
        id <= 0 -> Result.failure(ComicFailure.InvalidNumber())
        id == 404 -> Result.failure(ComicFailure.NotFound())
        else -> request("https://www.xkcd.com/$id/info.0.json", id)
    }

    private suspend fun request(url: String, expectedId: Int?): Result<Comic> = try {
        val response = client.get(url)
        when (response.status.value) {
            404 -> Result.failure(ComicFailure.NotFound())
            in 200..299 -> {
                try {
                    val comic = json.decodeFromString<Comic>(response.bodyAsText())
                    require(comic.num > 0 && comic.num != 404 && (expectedId == null || comic.num == expectedId))
                    require(comic.title.isNotBlank() && comic.img.startsWith("https://"))
                    require(comic.year.toIntOrNull() != null && comic.month.toIntOrNull() in 1..12 && comic.day.toIntOrNull() in 1..31)
                    Result.success(comic)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    Result.failure(ComicFailure.InvalidResponse())
                }
            }
            else -> Result.failure(ComicFailure.Network())
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        Result.failure(ComicFailure.Network())
    }

    override fun close() = client.close()
}
