package de.stoupas.xkcd

import androidx.core.view.WindowCompat
import androidx.test.core.app.ActivityScenario
import androidx.lifecycle.ViewModelProvider
import android.os.Build
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assume.assumeTrue

class SystemBarsTest {
    @Test
    fun changingAppThemeUpdatesStatusIconsAfterCompositionHasSettled() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var model: AndroidAppModel
            scenario.onActivity { model = ViewModelProvider(it)[AndroidAppModel::class.java] }
            runBlocking { withTimeout(5_000) { while (model.startup.value == StartupState.Loading) delay(50) } }
            val original = model.settings.state.value.darkTheme
            try {
                for (dark in listOf(false, true, false)) {
                    runBlocking {
                        model.settings.setTheme(dark).getOrThrow()
                        withTimeout(5_000) {
                            var matched = false
                            while (!matched) {
                                scenario.onActivity {
                                    val controller = WindowCompat.getInsetsController(it.window, it.window.decorView)
                                    matched = controller.isAppearanceLightStatusBars == !dark &&
                                        (Build.VERSION.SDK_INT < 26 || controller.isAppearanceLightNavigationBars == !dark)
                                }
                                if (!matched) delay(50)
                            }
                        }
                    }
                }
            } finally {
                runBlocking { model.settings.setTheme(original).getOrThrow() }
            }
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun legacyNavigationBarKeepsWhiteIconsOnDarkScrimInLightTheme() {
        assumeTrue(Build.VERSION.SDK_INT < 26)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity {
                it.updateSystemBars(false)
                assertTrue("API 23-25 cannot draw dark navigation icons and need an opaque scrim",
                    android.graphics.Color.alpha(it.window.navigationBarColor) == 255)
                assertTrue(androidx.core.graphics.ColorUtils.calculateLuminance(it.window.navigationBarColor) < 0.2)
            }
        }
    }

    @Test
    fun statusBarIconsFollowPersistedAppTheme() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.updateSystemBars(dark = true)
                assertFalse(WindowCompat.getInsetsController(activity.window, activity.window.decorView).isAppearanceLightStatusBars)
                activity.updateSystemBars(dark = false)
                assertTrue(WindowCompat.getInsetsController(activity.window, activity.window.decorView).isAppearanceLightStatusBars)
            }
        }
    }
}
