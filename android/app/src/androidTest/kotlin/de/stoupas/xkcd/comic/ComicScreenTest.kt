package de.stoupas.xkcd.comic

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.viewModelScope
import androidx.test.platform.app.InstrumentationRegistry
import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import de.stoupas.xkcd.settings.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(coil3.annotation.ExperimentalCoilApi::class)
class ComicScreenTest {
    @get:Rule val compose = createComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    private fun settings() = SettingsRepository(AndroidSettingsStorage(context.getSharedPreferences("screen_test_${System.nanoTime()}", 0))).also {
        runBlocking { it.initialize(LegacySettings()).getOrThrow() }
    }

    private fun loader(): Pair<ImageLoader, HttpClient> {
        val client = HttpClient(MockEngine {
            respond(instrumentation.context.assets.open("viewer_wide.png").use { it.readBytes() }, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "image/png"))
        })
        return ImageLoader.Builder(context).diskCache(null).components { add(KtorNetworkFetcherFactory(client)) }.build() to client
    }

    @Test
    fun emptyLookupDoesNotRequestAndFavoritesSurviveScreenRecreation() {
        val api = TestApi()
        val settings = settings()
        val vm = ComicViewModel(ComicRepository(api), settings)
        val (loader, client) = loader()
        val visible = mutableStateOf(true)
        try {
            compose.setContent {
                val current by settings.state.collectAsState()
                MaterialTheme { CompositionLocalProvider(LocalComicImageLoader provides loader) {
                    if (visible.value) ComicScreen(vm, current, {}, {}, {}, { _, _ -> }, {})
                } }
            }
            compose.waitUntil(10_000) { vm.state.value.comic != null }
            compose.onNodeWithTag("open_comic_search").performClick()
            compose.onNodeWithTag("comic_number_confirm").performClick()
            compose.onNodeWithTag("comic_number_input").assertIsDisplayed()
            assertTrue(api.ids.isEmpty())
            compose.onNodeWithTag("comic_number_input").performTextInput("25")
            compose.onNodeWithTag("comic_number_confirm").performClick()
            compose.waitUntil(10_000) { vm.state.value.comic?.num == 25 }
            compose.onNodeWithTag("toggle_favorite").performClick()
            compose.waitUntil(10_000) { settings.state.value.favorites == listOf(25) }
            compose.runOnIdle { visible.value = false }
            compose.runOnIdle { visible.value = true }
            compose.onNodeWithTag("toggle_favorite").assertContentDescriptionEquals("Remove from favorites")
        } finally { vm.viewModelScope.cancel(); loader.shutdown(); client.close() }
    }

    @Test
    fun failureIsVisibleAndRetryRecovers() {
        val api = TestApi().apply { fail = true }
        val settings = settings()
        val vm = ComicViewModel(ComicRepository(api), settings)
        val (loader, client) = loader()
        try {
            compose.setContent { MaterialTheme { CompositionLocalProvider(LocalComicImageLoader provides loader) {
                val current by settings.state.collectAsState()
                ComicScreen(vm, current, {}, {}, {}, { _, _ -> }, {})
            } } }
            compose.waitUntil(10_000) { vm.state.value.failure != null }
            compose.onNodeWithTag("comic_failure").assertIsDisplayed()
            api.fail = false
            compose.onNodeWithTag("comic_retry").performClick()
            compose.waitUntil(10_000) { vm.state.value.comic?.num == 12 }
            compose.onNodeWithTag("comic_failure").assertDoesNotExist()
        } finally { vm.viewModelScope.cancel(); loader.shutdown(); client.close() }
    }

    private class TestApi : ComicApi {
        val ids = java.util.concurrent.CopyOnWriteArrayList<Int>()
        @Volatile var fail = false
        override suspend fun latest(): Result<Comic> = if (fail) Result.failure(ComicFailure.Network()) else Result.success(comic(12))
        override suspend fun byId(id: Int): Result<Comic> { ids.add(id); return Result.success(comic(id)) }
        private fun comic(id: Int) = Comic(id, "Comic $id", "Alt", "https://example.test/$id.png", "2014", "1", "1")
    }
}
