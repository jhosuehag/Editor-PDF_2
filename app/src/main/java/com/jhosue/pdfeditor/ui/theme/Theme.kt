package com.jhosue.pdfeditor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = TextSecondary,
    tertiary = Purple80,
    background = BackgroundColor,
    surface = BackgroundColor, // Forced same as background
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    secondaryContainer = SurfaceColor,
    onSecondaryContainer = TextPrimary
)

@Composable
fun PDFEditorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}