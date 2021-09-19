package com.github.jan222ik.floodit.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ThemePreview(theme: List<Color>, modifier: Modifier, isActive: Boolean) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier
            .fillMaxSize()
            .clip(shape)
            .let {
                if (isActive) {
                    it.border(3.dp, Color.White, shape = shape)
                } else it
            }
    ) {
        theme.forEach {
            Surface(
                Modifier
                    .fillMaxSize()
                    .weight(1f), color = it
            ) {}
        }
    }
}