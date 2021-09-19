package com.github.jan222ik.floodit.ui.screens.floodit.parts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.jan222ik.floodit.logic.FloodItGameState
import com.github.jan222ik.floodit.logic.Game
import com.github.jan222ik.floodit.ui.components.CellSlider
import com.github.jan222ik.floodit.ui.components.ThemePreview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SettingsSheet(
    scope: CoroutineScope,
    game: Game,
    gameState: FloodItGameState,
    theme: List<Color>,
    steps: Int,
    onClose: () -> Unit,
    openThemesSelection: () -> Unit,
    gameChange: (Game) -> Unit
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
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (gameState == FloodItGameState.FINISHED) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            8.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("You solved it!")
                        Text("Steps $steps")
                    }
                } else {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.h6
                    )
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable(onClick = onClose),
                        imageVector = Icons.Filled.Close,
                        contentDescription = null
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Theme: ")
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    ThemePreview(
                        theme = theme,
                        modifier = Modifier
                            .height(50.dp)
                            .clickable(onClick = openThemesSelection),
                        isActive = false
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null
                        )
                        Text("Click to change")
                    }
                }
            }
            var widthSlider by remember(game) { mutableStateOf(game.size.height) }
            var heightSlider by remember(game) { mutableStateOf(game.size.width) }
            var colorCountSlider by remember(game) { mutableStateOf(game.colorCount) }
            CellSlider(
                title = { "Width: $it Cells" },
                value = widthSlider,
                onValueChange = { widthSlider = it },
                valueRange = 3f..12f
            )
            CellSlider(
                title = { "Height: $it Cells" },
                value = heightSlider,
                onValueChange = { heightSlider = it },
                valueRange = 5f..18f
            )
            CellSlider(
                title = { "Color Count: $it" },
                value = colorCountSlider,
                onValueChange = { colorCountSlider = it },
                valueRange = 3f..6f
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            gameChange.invoke(Game(
                                size = game.size,
                                colorCount = game.colorCount,
                                seed = game.seed
                            ))
                            onClose.invoke()
                        }
                    }) {
                    Text(text = "Restart level")
                }
                Button(
                    onClick = {
                        scope.launch {
                            gameChange.invoke(Game(
                                size = IntSize(
                                    heightSlider,
                                    widthSlider
                                ),
                                colorCount = colorCountSlider
                            ))
                            onClose.invoke()
                        }
                    }) {
                    Text(text = "Next game!")
                }
            }
        }
    }
}