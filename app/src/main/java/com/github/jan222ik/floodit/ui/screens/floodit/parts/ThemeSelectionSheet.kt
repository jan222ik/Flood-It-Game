package com.github.jan222ik.floodit.ui.screens.floodit.parts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.jan222ik.floodit.ui.components.ThemePreview
import com.github.jan222ik.floodit.ui.theme.themes

@Composable
fun ThemeSelectionSheet(
    theme: List<Color>,
    onChange: (theme: List<Color>) -> Unit,
    onClose: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    text = "Select a theme:",
                    modifier = Modifier.align(Alignment.Center)
                )
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable(onClick = onClose),
                    imageVector = Icons.Filled.Close,
                    contentDescription = null
                )
            }
            themes.forEach {
                ThemePreview(
                    theme = it,
                    modifier = Modifier
                        .height(50.dp)
                        .clickable {
                            onChange.invoke(it)
                        },
                    isActive = it == theme
                )
            }
        }
    }
}