package de.stoupas.xkcd.platform

import android.content.ContentValues
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import de.stoupas.xkcd.comic.Comic
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ImageSaveServiceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun realGalleryEntryContainsCompletePngAndIsNotPending() = runBlocking {
        org.junit.Assume.assumeTrue(android.os.Build.VERSION.SDK_INT >= 29)
        val name = "xkcd_test_${System.nanoTime()}.png"
        val bytes = instrumentation.context.assets.open("viewer_wide.png").use { it.readBytes() }
        val collection = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        try {
            MediaStoreImageWriter(context).write(bytes, "image/png", name).getOrThrow()
            context.contentResolver.query(collection, arrayOf("_id", "is_pending", "mime_type"), "_display_name = ?", arrayOf(name), null)!!.use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(1))
                assertEquals("image/png", cursor.getString(2))
                val uri = android.content.ContentUris.withAppendedId(collection, cursor.getLong(0))
                assertArrayEquals(bytes, context.contentResolver.openInputStream(uri)!!.use { it.readBytes() })
            }
        } finally { context.contentResolver.delete(collection, "_display_name = ?", arrayOf(name)) }
    }

    @Test
    fun failedWriteAfterInsertDeletesThePendingGalleryEntry() = runBlocking {
        val backend = object : ImageStoreBackend {
            var entry: Uri? = null
            override fun insert(values: ContentValues): Uri = Uri.parse("content://images/12").also { entry = it }
            override fun open(uri: Uri): OutputStream = object : OutputStream() { override fun write(b: Int) { throw java.io.IOException("Disk full") } }
            override fun publish(uri: Uri): Boolean = error("Must not publish failed image")
            override fun delete(uri: Uri) { entry = null }
        }
        val result = MediaStoreImageWriter(backend).write(byteArrayOf(1, 2, 3), "image/png", "comic.png")
        assertTrue(result.isFailure)
        assertNull(backend.entry)
    }

    @Test
    fun successfulGalleryWritePublishesOnlyAfterAllBytesAreWritten() = runBlocking {
        val bytes = ByteArrayOutputStream()
        val backend = object : ImageStoreBackend {
            var published = false
            override fun insert(values: ContentValues) = Uri.parse("content://images/12")
            override fun open(uri: Uri): OutputStream = bytes
            override fun publish(uri: Uri): Boolean { assertArrayEquals(byteArrayOf(1, 2, 3), bytes.toByteArray()); published = true; return true }
            override fun delete(uri: Uri) { error("Successful write must not delete") }
        }
        MediaStoreImageWriter(backend).write(byteArrayOf(1, 2, 3), "image/png", "comic.png").getOrThrow()
        assertTrue(backend.published)
    }

    @Test
    fun documentCancellationIsNotSaveSuccessAndExtensionUsesRealBytes() = runBlocking {
        val bytes = instrumentation.context.assets.open("viewer_wide.png").use { it.readBytes() }
        val client = HttpClient(MockEngine { respond(bytes, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "image/jpeg")) })
        var selectedMime: String? = null
        var selectedName: String? = null
        val service = AndroidImageSaveService(context, client, apiLevel = 23, exportDocument = { _, mime, name ->
            selectedMime = mime; selectedName = name
            Result.failure(PlatformFailure.UserCancelled)
        })
        try {
            val result = service.save(Comic(12, "Comic", "Alt", "https://example.test/12.jpg", "2006", "1", "1"), "https://example.test/12.jpg")
            assertSame(PlatformFailure.UserCancelled, result.exceptionOrNull())
            assertEquals("image/png", selectedMime)
            assertEquals("xkcd_12.png", selectedName)
        } finally { service.close(); client.close() }
    }
}
