package de.stoupas.xkcd.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    private fun outline(name: String, draw: PathBuilder.() -> Unit) = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathBuilder = draw)
    }.build()

    val Search = outline("Search") { moveTo(21f, 21f); lineTo(16.5f, 16.5f); moveTo(18f, 10.5f); arcTo(7.5f, 7.5f, 0f, true, true, 3f, 10.5f); arcTo(7.5f, 7.5f, 0f, true, true, 18f, 10.5f) }
    val Previous = outline("Previous") { moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f) }
    val Next = outline("Next") { moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f) }
    val Heart = outline("Favorite") { moveTo(12f, 21f); lineTo(3f, 12f); curveTo(0f, 5f, 7f, 1f, 12f, 7f); curveTo(17f, 1f, 24f, 5f, 21f, 12f); close() }
    val HeartFilled = ImageVector.Builder("Saved favorite", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) { moveTo(12f, 21f); lineTo(3f, 12f); curveTo(0f, 5f, 7f, 1f, 12f, 7f); curveTo(17f, 1f, 24f, 5f, 21f, 12f); close() }
    }.build()
    val Favorites = outline("Favorites") { moveTo(7f, 5f); lineTo(19f, 5f); lineTo(19f, 22f); lineTo(13f, 18f); lineTo(7f, 22f); close(); moveTo(4f, 18f); lineTo(4f, 2f); lineTo(16f, 2f) }
    val More = outline("More actions") { moveTo(12f, 5f); lineTo(12f, 5.01f); moveTo(12f, 12f); lineTo(12f, 12.01f); moveTo(12f, 19f); lineTo(12f, 19.01f) }
}
