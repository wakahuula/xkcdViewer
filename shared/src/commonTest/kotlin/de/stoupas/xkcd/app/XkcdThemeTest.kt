package de.stoupas.xkcd.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.*

class XkcdThemeTest {
    @Test
    fun primaryTextAndControlsRemainReadableForExtremeAccentColors() {
        for (dark in listOf(false, true)) {
            for (accent in listOf(0xFFFFFFFF, 0xFF000000, 0xFFFFFF00, 0xFF673AB7, 0xFF00FF00, 0L)) {
                val colors = xkcdColorScheme(dark, accent)
                assertTrue(contrast(colors.primary, colors.onPrimary) >= 4.5f)
                assertTrue(contrast(colors.primary, colors.surface) >= 4.5f)
                assertTrue(contrast(colors.primaryContainer, colors.onPrimaryContainer) >= 4.5f)
            }
        }
    }
    private fun contrast(a: Color, b: Color): Float = (maxOf(a.luminance(), b.luminance()) + 0.05f) / (minOf(a.luminance(), b.luminance()) + 0.05f)
}
