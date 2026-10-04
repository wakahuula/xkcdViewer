package de.stoupas.xkcd

import androidx.core.view.WindowCompat
import androidx.test.core.app.ActivityScenario
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemBarsTest {
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
