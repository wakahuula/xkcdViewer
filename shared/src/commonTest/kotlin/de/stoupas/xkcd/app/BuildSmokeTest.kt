package de.stoupas.xkcd.app

import kotlin.test.Test
import kotlin.test.assertEquals

class BuildSmokeTest {
    @Test
    fun appIdentity() {
        assertEquals("de.stoupas.xkcd", APP_ID)
        assertEquals("xkcdViewer", APP_NAME)
    }
}
