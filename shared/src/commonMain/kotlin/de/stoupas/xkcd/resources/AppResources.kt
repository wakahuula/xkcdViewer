package de.stoupas.xkcd.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font

// Preserve the original typeface for optional use, without imposing the old UI typography.
@Composable
fun firaMonoFontFamily(): FontFamily = FontFamily(
    Font(Res.font.fira_mono_regular, FontWeight.Normal),
    Font(Res.font.fira_mono_medium, FontWeight.Medium),
    Font(Res.font.fira_mono_bold, FontWeight.Bold),
)
