package de.stoupas.xkcd.comic

import kotlin.test.*

class ImageVariantsTest {
    private fun comic(id: Int) = Comic(id, "Comic", "Alt", "https://imgs.xkcd.com/comics/test.jpg", "2014", "1", "1")

    @Test
    fun originalIsUsedForOlderComicsDataSaverAndExceptions() {
        for (id in listOf(1, 1083, 1193, 1446, 1667, 1735, 1739, 1744, 1778)) {
            assertEquals(listOf("https://imgs.xkcd.com/comics/test.jpg"), imageCandidates(comic(id), false))
        }
        assertEquals(listOf("https://imgs.xkcd.com/comics/test.jpg"), imageCandidates(comic(2000), true))
    }

    @Test
    fun highResolutionPrecedesOriginal() {
        assertEquals(listOf("https://imgs.xkcd.com/comics/test_2x.png", "https://imgs.xkcd.com/comics/test.jpg"), imageCandidates(comic(1084), false))
    }
}
