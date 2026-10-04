package de.stoupas.xkcd.comic

fun shouldRevealAltText(pointerCount: Int, atBaseScale: Boolean, upwardDragDp: Float): Boolean =
    pointerCount == 1 && atBaseScale && upwardDragDp >= 48f
