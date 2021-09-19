package com.github.jan222ik.floodit.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    background = Color(0x17, 0x17, 0x19),
    surface = Color(0x23, 0x22, 0x29),
    primary = Color(0x31, 0x68, 0xE0),
    primaryVariant = Color(0x19, 0x1E, 0x2A),
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