package de.stoupas.xkcd.comic

import kotlin.test.*

class ViewerGesturePolicyTest {
    @Test
    fun altTextRequiresSinglePointerAtBaseZoomAnd48DpUpward() {
        assertTrue(shouldRevealAltText(1, true, 48f))
        assertFalse(shouldRevealAltText(1, true, 47.9f))
        assertFalse(shouldRevealAltText(1, true, -60f))
        assertFalse(shouldRevealAltText(2, true, 60f))
        assertFalse(shouldRevealAltText(1, false, 60f))
        assertFalse(shouldRevealAltText(0, true, 60f))
    }
}
