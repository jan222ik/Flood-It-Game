package com.github.jan222ik.floodit.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    background = Background,
    surface = Surface,
    primary = Primary,
    primaryVariant = PrimaryVariant,
    onPrimary = Color.White,
    secondary = Teal200
)

@Composable
fun FloodItTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = DarkColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}