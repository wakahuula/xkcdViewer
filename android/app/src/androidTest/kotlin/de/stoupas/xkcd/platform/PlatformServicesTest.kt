package de.stoupas.xkcd.platform

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import de.stoupas.xkcd.comic.Comic
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PlatformServicesTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    @Test
    fun generatedLicenseSourceContainsSharedRuntimeDependencies() = runBlocking {
        val source = AndroidLicenseSource(context, de.stoupas.xkcd.R.raw.aboutlibraries)
        val libraries = com.mikepenz.aboutlibraries.Libs.Builder().withJson(source.readJson()).build().libraries
        assertTrue(libraries.any { it.uniqueId.startsWith("io.coil-kt.coil3") })
        assertTrue(libraries.any { it.uniqueId.contains("zoomimage") })
        assertTrue(libraries.any { it.uniqueId.startsWith("io.ktor") })
        assertTrue(libraries.any { it.uniqueId.startsWith("org.jetbrains.kotlin") })
        assertTrue(libraries.filter { it.uniqueId.contains("zoomimage") }.all { it.licenses.isNotEmpty() })
    }

    @Test
    fun shareUsesSystemChooserWithPlainTextComicLink() = runBlocking {
        var launched: Intent? = null
        AndroidShareService(context) { launched = it }.shareComic(Comic(12, "Comic", "Alt", "https://example.test/12.png", "2006", "1", "1")).getOrThrow()
        assertEquals(Intent.ACTION_CHOOSER, launched?.action)
        @Suppress("DEPRECATION")
        val payload = launched!!.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, payload.action)
        assertEquals("text/plain", payload.type)
        assertEquals("https://www.xkcd.com/12/", payload.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun browserAllowsOnlyWebUrlsAndReportsMissingHandler() = runBlocking {
        var launched: Intent? = null
        val browser = AndroidBrowserService(context) { launched = it }
        browser.open("https://www.explainxkcd.com/wiki/index.php/12").getOrThrow()
        assertEquals(Intent.ACTION_VIEW, launched?.action)
        assertEquals("https://www.explainxkcd.com/wiki/index.php/12", launched?.dataString)
        for (url in listOf("javascript:alert(1)", "file:///data/private", "https:", "content://private")) {
            assertTrue(browser.open(url).exceptionOrNull() is PlatformFailure.InvalidUrl)
        }
        val absent = AndroidBrowserService(context) { throw ActivityNotFoundException() }
        assertTrue(absent.open("https://xkcd.com/").isFailure)
    }
}
