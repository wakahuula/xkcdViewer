package de.stoupas.xkcd.comic

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.platform.app.InstrumentationRegistry
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.network.ktor3.KtorNetworkFetcherFactory
import de.stoupas.xkcd.platform.AndroidImageCacheService
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

@OptIn(coil3.annotation.ExperimentalCoilApi::class, ExperimentalTestApi::class)
class ComicViewerTest {
    @get:Rule val compose = createComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private fun comic(id: Int = 2000, file: String = "viewer_wide.png") =
        Comic(id, "Test comic", "A long accessible alt text ❤", "https://example.test/$file", "2014", "1", "1")

    @Test
    fun wideComicKeepsAltTextBesideNaturalImageHeight() {
        val client = HttpClient(MockEngine {
            respond(instrumentation.context.assets.open("viewer_wide.png").use { it.readBytes() }, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "image/png"))
        })
        val loader = ImageLoader.Builder(context).diskCache(null).components { add(KtorNetworkFetcherFactory(client)) }.build()
        var loaded = false
        try {
            compose.setContent {
                MaterialTheme { CompositionLocalProvider(LocalComicImageLoader provides loader) {
                    ComicViewer(comic(), true, { loaded = true })
                } }
            }
            compose.waitUntil(15_000) { loaded }
            compose.waitForIdle()
            val image = compose.onNodeWithTag("comic_image").fetchSemanticsNode().boundsInRoot
            val alt = compose.onNodeWithTag("alt_text_button").fetchSemanticsNode().boundsInRoot
            // Top-aligned 1400x300 content fits inside a stable, full-height zoom viewport.
            val contentBottom = image.top + image.width * 300f / 1400f
            assertTrue("Alt text must follow the fitted image, not the viewport bottom", kotlin.math.abs(alt.top - contentBottom) < 24f)
            compose.onNodeWithTag("alt_text_button").assertIsDisplayed().performClick()
            compose.onNodeWithText("A long accessible alt text ❤").assertIsDisplayed()
        } finally { loader.shutdown(); client.close() }
    }

    @Test
    fun wideComicZoomUsesAvailableSpaceWithoutOpeningAltText() {
        val client = HttpClient(MockEngine {
            respond(instrumentation.context.assets.open("viewer_wide.png").use { it.readBytes() }, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "image/png"))
        })
        val loader = ImageLoader.Builder(context).diskCache(null).components { add(KtorNetworkFetcherFactory(client)) }.build()
        var loaded = false
        try {
            compose.setContent {
                MaterialTheme { CompositionLocalProvider(LocalComicImageLoader provides loader) {
                    ComicViewer(comic(), true, { loaded = true })
                } }
            }
            compose.waitUntil(15_000) { loaded }
            compose.waitForIdle()
            val originalHeight = compose.onNodeWithTag("comic_image").fetchSemanticsNode().boundsInRoot.height
            compose.onNodeWithTag("comic_image").performTouchInput {
                pinch(Offset(center.x - 20, center.y), Offset(center.x + 20, center.y), Offset(center.x - 100, center.y), Offset(center.x + 100, center.y))
            }
            compose.waitForIdle()
            val image = compose.onNodeWithTag("comic_image").fetchSemanticsNode()
            val scale = image.config[SemanticsProperties.StateDescription].removeSuffix("%").toInt()
            assertTrue("Wide image must remain zoomed", scale > 100)
            assertEquals("Zoom must not resize its viewport and interrupt gestures", originalHeight, image.boundsInRoot.height, 1f)
            val alt = compose.onNodeWithTag("alt_text_button").fetchSemanticsNode().boundsInRoot
            assertTrue("Zoomed image must have the full reading area before the alt button", alt.top >= image.boundsInRoot.bottom)
            compose.onNodeWithText("A long accessible alt text ❤").assertDoesNotExist()
            compose.onNodeWithTag("alt_text_button").assertIsDisplayed()
        } finally { loader.shutdown(); client.close() }
    }

    @Test
    fun wideComicDoubleTapSettlesAtZoomTarget() {
        val client = HttpClient(MockEngine {
            respond(instrumentation.context.assets.open("viewer_wide.png").use { it.readBytes() }, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "image/png"))
        })
        val loader = ImageLoader.Builder(context).diskCache(null).components { add(KtorNetworkFetcherFactory(client)) }.build()
        var loaded = false
        try {
            compose.setContent { MaterialTheme { CompositionLocalProvider(LocalComicImageLoader provides loader) {
                ComicViewer(comic(), true, { loaded = true })
            } } }
            compose.waitUntil(15_000) { loaded }
            compose.waitForIdle()
            compose.onNodeWithTag("comic_image").performTouchInput { doubleClick(Offset(center.x, 40f)) }
            compose.waitForIdle()
            val scale = compose.onNodeWithTag("comic_image").fetchSemanticsNode().config[SemanticsProperties.StateDescription].removeSuffix("%").toInt()
            assertTrue("Double tap must finish its animation, not stop just above fit scale: $scale%", scale >= 200)
            compose.onNodeWithText("A long accessible alt text ❤").assertDoesNotExist()
        } finally { loader.shutdown(); client.close() }
    }

    @Test
    fun tallComicLeavesRoomForLargeAltTextLabel() {
        val client = HttpClient(MockEngine {
            respond(instrumentation.context.assets.open("viewer_tall.png").use { it.readBytes() }, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "image/png"))
        })
        val loader = ImageLoader.Builder(context).diskCache(null).components { add(KtorNetworkFetcherFactory(client)) }.build()
        var loaded = false
        try {
            compose.setContent {
                DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(240.dp, 500.dp)) then DeviceConfigurationOverride.FontScale(4f)) {
                    MaterialTheme { CompositionLocalProvider(LocalComicImageLoader provides loader) {
                        ComicViewer(comic(12, "viewer_tall.png"), true, { loaded = true })
                    } }
                }
            }
            compose.waitUntil(15_000) { loaded }
            compose.waitForIdle()
            val button = compose.onNodeWithTag("alt_text_button")
            val bounds = button.getUnclippedBoundsInRoot()
            assertTrue("Large wrapped label must measure independently of the image", bounds.bottom - bounds.top > 64.dp)
            button.assertIsDisplayed().performClick()
            compose.onNodeWithText("A long accessible alt text ❤").assertExists()
        } finally { loader.shutdown(); client.close() }
    }

    @Test
    fun fallsBackOnceAndReportsOnlyOriginalSuccess() {
        val calls = CopyOnWriteArrayList<String>()
        val client = HttpClient(MockEngine { request ->
            calls.add(request.url.toString())
            if (request.url.encodedPath.endsWith("_2x.png")) respond("missing", HttpStatusCode.NotFound)
            else respond(instrumentation.context.assets.open("viewer_wide.png").use { it.readBytes() }, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "image/png"))
        })
        val loader = ImageLoader.Builder(context).diskCache(null).components { add(KtorNetworkFetcherFactory(client)) }.build()
        var resolved: String? = null
        try {
            compose.setContent {
                MaterialTheme {
                    CompositionLocalProvider(LocalComicImageLoader provides loader) {
                        ComicViewer(comic(), false, { resolved = it })
                    }
                }
            }
            compose.waitUntil(15_000) { resolved != null }
            assertEquals("https://example.test/viewer_wide.png", resolved)
            assertEquals(1, calls.count { it.endsWith("viewer_wide_2x.png") })
            compose.onNodeWithTag("alt_text_button").performClick()
            compose.onNodeWithText("A long accessible alt text ❤").assertIsDisplayed()
        } finally { loader.shutdown(); client.close() }
    }

    @Test
    fun bothImageFailuresExposeRetryWithoutSuccess() {
        val client = HttpClient(MockEngine { respond("missing", HttpStatusCode.NotFound) })
        val loader = ImageLoader.Builder(context).diskCache(null).components { add(KtorNetworkFetcherFactory(client)) }.build()
        var resolved = false
        try {
            compose.setContent {
                MaterialTheme { CompositionLocalProvider(LocalComicImageLoader provides loader) {
                    ComicViewer(comic(), false, { resolved = true })
                } }
            }
            compose.waitUntil(15_000) { compose.onAllNodesWithTag("image_retry").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("image_retry").assertIsDisplayed().performClick()
            assertFalse(resolved)
        } finally { loader.shutdown(); client.close() }
    }

    @Test
    fun pinchThenPanDoesNotRevealAltTextAndTallImageLoads() {
        val client = HttpClient(MockEngine { respond(instrumentation.context.assets.open("viewer_tall.png").use { it.readBytes() }, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "image/png")) })
        val loader = ImageLoader.Builder(context).diskCache(null).components { add(KtorNetworkFetcherFactory(client)) }.build()
        var loaded = false
        try {
            compose.setContent {
                MaterialTheme { CompositionLocalProvider(LocalComicImageLoader provides loader) {
                    ComicViewer(comic(12, "viewer_tall.png"), true, { loaded = true })
                } }
            }
            compose.waitUntil(15_000) { loaded }
            compose.onNodeWithTag("comic_image").performTouchInput {
                pinch(Offset(center.x - 20, center.y), Offset(center.x + 20, center.y), Offset(center.x - 100, center.y), Offset(center.x + 100, center.y))
            }
            compose.waitForIdle()
            val scale = compose.onNodeWithTag("comic_image").fetchSemanticsNode().config[SemanticsProperties.StateDescription].removeSuffix("%").toInt()
            assertTrue("Pinch must actually zoom the image", scale > 100)
            compose.onNodeWithTag("comic_image").performTouchInput { swipeUp() }
            compose.onNodeWithText("A long accessible alt text ❤").assertDoesNotExist()
            compose.onNodeWithTag("alt_text_button").performClick()
            compose.onNodeWithText("A long accessible alt text ❤").assertIsDisplayed()
        } finally { loader.shutdown(); client.close() }
    }

    @Test
    fun cacheClearRemovesMemoryAndDiskEntries() = runBlocking {
        val client = HttpClient(MockEngine { respond(instrumentation.context.assets.open("viewer_wide.png").use { it.readBytes() }, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "image/png")) })
        val loader = de.stoupas.xkcd.platform.createAndroidImageLoader(context, client)
        try {
            assertTrue(loader.execute(ImageRequest.Builder(context).data("https://example.test/cache.png").build()) is SuccessResult)
            val cache = AndroidImageCacheService(loader)
            val populated = cache.stats()
            assertTrue(populated.memoryBytes > 0)
            assertTrue(populated.diskBytes > 0)
            cache.clear().getOrThrow()
            val empty = cache.stats()
            assertEquals(0L, empty.memoryBytes)
            assertEquals(0L, empty.diskBytes)
        } finally { loader.shutdown(); client.close() }
    }
}
