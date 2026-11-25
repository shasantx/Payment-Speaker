package com.paisabol.paymentspeaker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PaisaBolDarkScheme = darkColorScheme(
    primary = InkPrimary,
    onPrimary = BgCanvas,
    secondary = BrassAccent,
    onSecondary = BgCanvas,
    background = BgCanvas,
    onBackground = InkPrimary,
    surface = SurfaceCard,
    onSurface = InkPrimary,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = InkSecondary,
    outline = Hairline,
    error = DangerRed
)

@Composable
fun PaisaBolTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PaisaBolDarkScheme,
        typography = PaisaBolTypography,
        content = content
    )
}
