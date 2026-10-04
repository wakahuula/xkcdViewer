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
    val Menu = outline("Menu") { moveTo(4f, 6f); lineTo(20f, 6f); moveTo(4f, 12f); lineTo(20f, 12f); moveTo(4f, 18f); lineTo(20f, 18f) }
}
