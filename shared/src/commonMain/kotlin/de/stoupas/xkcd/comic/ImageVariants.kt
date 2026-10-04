package de.stoupas.xkcd.comic

private val originalOnly = setOf(1193, 1446, 1667, 1735, 1739, 1744, 1778)

fun imageCandidates(comic: Comic, dataSaver: Boolean): List<String> {
    if (dataSaver || comic.num < 1084 || comic.num in originalOnly) return listOf(comic.img)
    val dot = comic.img.lastIndexOf('.')
    if (dot <= comic.img.lastIndexOf('/')) return listOf(comic.img)
    return listOf(comic.img.substring(0, dot) + "_2x.png", comic.img).distinct()
}
