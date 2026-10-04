package de.stoupas.xkcd.platform

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.OutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

interface ImageStoreBackend {
    fun insert(values: ContentValues): Uri?
    fun open(uri: Uri): OutputStream?
    fun publish(uri: Uri): Boolean
    fun delete(uri: Uri)
}

class MediaStoreImageWriter(private val backend: ImageStoreBackend) {
    constructor(context: Context) : this(object : ImageStoreBackend {
        private val resolver = context.applicationContext.contentResolver
        override fun insert(values: ContentValues) = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        override fun open(uri: Uri) = resolver.openOutputStream(uri, "w")
        override fun publish(uri: Uri) = resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null) > 0
        override fun delete(uri: Uri) { resolver.delete(uri, null, null) }
    })

    suspend fun write(bytes: ByteArray, mimeType: String, displayName: String): Result<Unit> = withContext(Dispatchers.IO) {
        var inserted: Uri? = null
        try {
            require(bytes.isNotEmpty())
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/xkcdViewer")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = requireNotNull(backend.insert(values)) { "Gallery insert failed" }
            inserted = uri
            requireNotNull(backend.open(uri)).use { it.write(bytes) }
            check(backend.publish(uri)) { "Gallery publish failed" }
            Result.success(Unit)
        } catch (failure: Exception) {
            withContext(NonCancellable) {
                inserted?.let { try { backend.delete(it) } catch (cleanup: Exception) { failure.addSuppressed(cleanup) } }
            }
            if (failure is CancellationException) throw failure
            Result.failure(PlatformFailure.WriteFailed())
        }
    }
}
