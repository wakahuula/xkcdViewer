package de.stoupas.xkcd.platform

import android.content.Context
import android.net.Uri
import android.os.Build
import de.stoupas.xkcd.comic.Comic
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException

class AndroidImageSaveService(
    context: Context,
    client: HttpClient,
    private val writer: MediaStoreImageWriter = MediaStoreImageWriter(context),
    private val apiLevel: Int = Build.VERSION.SDK_INT,
    private val exportDocument: suspend (ByteArray, String, String) -> Result<Unit> = { _, _, _ -> Result.failure(PlatformFailure.Unavailable()) },
) : ImageSaveService, AutoCloseable {
    private val client = client.config { expectSuccess = false; install(HttpTimeout) { requestTimeoutMillis = 15_000 } }

    override suspend fun save(comic: Comic, imageUrl: String): Result<Unit> {
        val uri = Uri.parse(imageUrl)
        if (uri.scheme?.lowercase() !in setOf("http", "https") || uri.host.isNullOrBlank()) return Result.failure(PlatformFailure.InvalidUrl())
        return try {
            val response = client.get(imageUrl)
            check(response.status.value in 200..299)
            val contentLength = response.headers["Content-Length"]?.toLongOrNull()
            require(contentLength == null || contentLength <= 30L * 1024 * 1024)
            val bytes: ByteArray = response.body()
            require(bytes.size in 1..30 * 1024 * 1024)
            val (mime, extension) = when {
                bytes.size >= 8 && bytes.copyOfRange(0, 8).contentEquals(byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10)) -> "image/png" to "png"
                bytes.size >= 3 && bytes[0] == (-1).toByte() && bytes[1] == (-40).toByte() && bytes[2] == (-1).toByte() -> "image/jpeg" to "jpg"
                bytes.size >= 6 && bytes.copyOfRange(0, 6).decodeToString() in listOf("GIF87a", "GIF89a") -> "image/gif" to "gif"
                bytes.size >= 12 && bytes.copyOfRange(0, 4).decodeToString() == "RIFF" && bytes.copyOfRange(8, 12).decodeToString() == "WEBP" -> "image/webp" to "webp"
                else -> return Result.failure(PlatformFailure.WriteFailed())
            }
            val name = "xkcd_${comic.num}.$extension"
            if (apiLevel >= 29) writer.write(bytes, mime, name) else exportDocument(bytes, mime, name)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { Result.failure(PlatformFailure.WriteFailed()) }
    }
    override fun close() = client.close()
}
