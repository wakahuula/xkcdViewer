package de.stoupas.xkcd.app

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import de.stoupas.xkcd.settings.UserSettings

private fun contrast(a: Color, b: Color): Float = (maxOf(a.luminance(), b.luminance()) + 0.05f) / (minOf(a.luminance(), b.luminance()) + 0.05f)
private fun foreground(color: Color) = if (contrast(color, Color.White) > contrast(color, Color.Black)) Color.White else Color.Black

fun xkcdColorScheme(dark: Boolean, accentArgb: Long): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    val seed = Color(accentArgb.toInt()).copy(alpha = 1f)
    val direction = if (dark) Color.White else Color.Black
    var primary = seed
    for (step in 0..100) {
        primary = lerp(seed, direction, step / 100f)
        if (contrast(primary, base.surface) >= 4.5f) break
    }
    val container = lerp(primary, base.surface, 0.82f)
    return base.copy(
        primary = primary, onPrimary = foreground(primary),
        primaryContainer = container, onPrimaryContainer = foreground(container),
        secondary = primary, onSecondary = foreground(primary),
        secondaryContainer = container, onSecondaryContainer = foreground(container),
        tertiary = primary, onTertiary = foreground(primary),
        tertiaryContainer = container, onTertiaryContainer = foreground(container),
        inversePrimary = if (dark) lerp(seed, Color.Black, 0.4f) else lerp(seed, Color.White, 0.4f),
    )
}

@Composable
fun XkcdTheme(settings: UserSettings, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = xkcdColorScheme(settings.darkTheme, settings.accentArgb), content = content)
}
