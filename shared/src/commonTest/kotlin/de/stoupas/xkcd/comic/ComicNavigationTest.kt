package de.stoupas.xkcd.comic

import kotlin.test.*

class ComicNavigationTest {
    @Test
    fun skips404AndStopsAtBounds() {
        assertEquals(405, adjacentId(403, 1, 500))
        assertEquals(403, adjacentId(405, -1, 500))
        assertEquals(1, adjacentId(1, -1, 500))
        assertEquals(500, adjacentId(500, 1, 500))
        assertEquals(Int.MAX_VALUE, adjacentId(Int.MAX_VALUE, 1, Int.MAX_VALUE))
    }

    @Test
    fun acceptsOnlyPositiveAvailableComicNumbers() {
        for (query in listOf("", " ", "abc", "0", "-1", "404", "2147483648", "12.0")) {
            assertNull(parseComicId(query), query)
        }
        assertEquals(12, parseComicId(" 12 "))
    }
}
