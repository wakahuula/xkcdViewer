package de.stoupas.xkcd.comic

fun parseComicId(query: String): Int? = query.trim().toIntOrNull()?.takeIf { it > 0 && it != 404 }

fun adjacentId(current: Int, delta: Int, latest: Int): Int {
    require(latest > 0)
    var next = (current.toLong() + delta).coerceIn(1, latest.toLong()).toInt()
    if (next == 404) next = if (delta < 0 || latest == 404) 403 else 405
    return next
}
