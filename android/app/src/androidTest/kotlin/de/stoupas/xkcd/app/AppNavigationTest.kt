package de.stoupas.xkcd.app

import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.test.platform.app.InstrumentationRegistry
import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import de.stoupas.xkcd.comic.*
import de.stoupas.xkcd.platform.*
import de.stoupas.xkcd.settings.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(coil3.annotation.ExperimentalCoilApi::class, ExperimentalTestApi::class)
class AppNavigationTest {
    @get:Rule val compose = createComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val preferenceName = "navigation_${System.nanoTime()}"

    @Test
    fun largeFontAndLandscapeKeepReaderActionsReachableWithoutLosingComic() {
        val settings = settings()
        val deps = dependencies(settings)
        val (loader, client) = loader()
        val landscape = mutableStateOf(false)
        try {
            compose.setContent {
                val size = if (landscape.value) DpSize(640.dp, 320.dp) else DpSize(360.dp, 640.dp)
                DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(size) then DeviceConfigurationOverride.FontScale(2f)) {
                    CompositionLocalProvider(LocalComicImageLoader provides loader) { XkcdApp(deps) }
                }
            }
            compose.waitUntil(15_000) { compose.onAllNodesWithText("Comic 12").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("open_comic_search").assertIsDisplayed()
            compose.onNodeWithTag("toggle_favorite").assertIsDisplayed()
            compose.runOnIdle { landscape.value = true }
            compose.onNodeWithText("Comic 12").assertIsDisplayed()
            compose.onNodeWithTag("open_comic_search").assertIsDisplayed()
            compose.onNodeWithTag("toggle_favorite").assertIsDisplayed()
            compose.onNodeWithTag("alt_text_button").assertIsDisplayed().performClick()
            compose.onNodeWithText("Alt").assertIsDisplayed()
        } finally { loader.shutdown(); client.close() }
    }

    @Test
    fun favoritesDetailBackAndRestorationKeepIndependentComicSelection() {
        val settings = settings()
        val deps = dependencies(settings)
        val (loader, client) = loader()
        try {
            val restoration = StateRestorationTester(compose)
            restoration.setContent { CompositionLocalProvider(LocalComicImageLoader provides loader) { XkcdApp(deps) } }
            compose.waitUntil(15_000) { compose.onAllNodesWithText("Comic 12").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("comic_menu").performClick()
            compose.onNodeWithText("Favorites").performClick()
            compose.waitUntil(15_000) { compose.onAllNodesWithTag("favorite_25").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("favorite_25").performClick()
            compose.waitUntil(15_000) { compose.onAllNodesWithText("Comic 25").fetchSemanticsNodes().isNotEmpty() }
            restoration.emulateSavedInstanceStateRestore()
            compose.onNodeWithText("Comic 25").assertIsDisplayed()
            compose.onNodeWithTag("navigate_back").performClick()
            compose.onNodeWithTag("navigate_back").performClick()
            compose.onNodeWithText("Comic 12").assertIsDisplayed()
        } finally { loader.shutdown(); client.close() }
    }

    @Test
    fun cancelledAccentDoesNotWriteAndCreditsRemainReachable() {
        val settings = settings()
        val deps = dependencies(settings)
        val original = settings.state.value.accentArgb
        val (loader, client) = loader()
        try {
            compose.setContent { CompositionLocalProvider(LocalComicImageLoader provides loader) { XkcdApp(deps) } }
            compose.waitUntil(15_000) { compose.onAllNodesWithTag("comic_menu").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("comic_menu").performClick()
            compose.onNodeWithText("Settings").performClick()
            compose.onNodeWithTag("accent_color").performClick()
            compose.onNodeWithTag("accent_FF006C4C").performClick()
            compose.onNodeWithText("Cancel").performClick()
            assertEquals(original, settings.state.value.accentArgb)
            compose.onNodeWithTag("dark_theme_switch").performClick()
            compose.onNodeWithTag("data_saver_switch").performClick()
            compose.waitUntil(10_000) { !settings.state.value.darkTheme && settings.state.value.dataSaver }
            val restarted = settings()
            assertFalse(restarted.state.value.darkTheme)
            assertTrue(restarted.state.value.dataSaver)
            compose.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("open_contributors"))
            compose.onNodeWithTag("open_contributors").performClick()
            compose.onNodeWithTag("contributors_list").performScrollToNode(hasText("wakahuula"))
            compose.onNodeWithText("wakahuula").assertIsDisplayed()
        } finally { loader.shutdown(); client.close() }
    }

    private fun settings() = SettingsRepository(AndroidSettingsStorage(context.getSharedPreferences(preferenceName, 0))).also {
        runBlocking { it.initialize(LegacySettings(favorites = listOf(25))).getOrThrow() }
    }

    private fun loader(): Pair<ImageLoader, HttpClient> {
        val client = HttpClient(MockEngine { respond(instrumentation.context.assets.open("viewer_wide.png").use { it.readBytes() }, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "image/png")) })
        return ImageLoader.Builder(context).diskCache(null).components { add(KtorNetworkFetcherFactory(client)) }.build() to client
    }

    private fun dependencies(settings: SettingsRepository) = AppDependencies(
        ComicRepository(object : ComicApi {
            override suspend fun latest() = Result.success(comic(12))
            override suspend fun byId(id: Int) = Result.success(comic(id))
            private fun comic(id: Int) = Comic(id, "Comic $id", "Alt", "https://example.test/$id.png", "2014", "1", "1")
        }), settings,
        object : ShareService { override suspend fun shareComic(comic: Comic) = Result.success(Unit) },
        object : ImageSaveService { override suspend fun save(comic: Comic, imageUrl: String) = Result.success(Unit) },
        object : BrowserService { override suspend fun open(url: String) = Result.success(Unit) },
        object : ImageCacheService {
            override suspend fun stats() = CacheStats(0, 0, null)
            override suspend fun clear() = Result.success(Unit)
        },
        object : LicenseSource { override suspend fun readJson() = """{"libraries":[],"licenses":{}}""" },
    )
}
