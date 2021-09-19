package com.github.jan222ik.floodit.ui.screens.floodit.parts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import com.github.jan222ik.floodit.logic.FloodItGameState
import com.github.jan222ik.floodit.logic.Game
import com.github.jan222ik.floodit.logic.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlayingBoard(game: Game, theme: List<Color>, isDebug: Boolean = false) {
    val gamestate by remember(game, game.state) { game.state }.collectAsState()
    val map = remember(game) {
        game.map.map
    }
    val colorUpdates = game.colorUpdates.collectAsState()

    val spawn: Point? = remember(game, gamestate) {
        if (gamestate == FloodItGameState.RUNNING) {
            game.map.source
        } else null
    }

    BoxWithConstraints(
        Modifier.fillMaxSize()
    ) {
        Column {
            map.mapIndexed { rowIdx, row ->
                Row(
                    Modifier
                        .weight(1f, true)
                        .fillMaxSize()
                ) {
                    row.mapIndexed { colIdx, colorIdx ->
                        val scope = rememberCoroutineScope()
                        val point = remember(game, rowIdx, colIdx) { Point(rowIdx, colIdx) }
                        val color =
                            remember(game, colorIdx, theme) { mutableStateOf(theme[colorIdx]) }
                        val animColor = animateColorAsState(targetValue = color.value)

                        val job: Job? = remember(game, point, theme, colorUpdates.value) {
                            colorUpdates.value?.second?.get(point)?.let { depth ->
                                scope.launch {
                                    launch(Dispatchers.IO) {
                                        delay(100L * depth)
                                        colorUpdates.value?.first?.let {
                                            color.value = theme[it]
                                        }
                                    }
                                }
                            }
                        }
                        DisposableEffect(key1 = game, effect = {
                            onDispose {
                                job?.cancel()
                            }
                        })
                        Card(
                            modifier = Modifier
                                .weight(1f, true)
                                .fillMaxSize()
                                .clickable {
                                    if (spawn == null) {
                                        scope.launch(Dispatchers.IO) {
                                            game.map.placeSource(point)
                                            game.state.emit(FloodItGameState.RUNNING)
                                            game.nextColor(point)
                                        }
                                    } else {
                                        scope.launch(Dispatchers.IO) { game.nextColor(point) }
                                    }
                                },
                            backgroundColor = animColor.value,
                            shape = RectangleShape
                        ) {
                            Column {
                                if (spawn == point) {
                                    val transition = rememberInfiniteTransition()
                                    val innerRadiusAnim = transition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 0.5f,
                                        animationSpec = infiniteRepeatable(
                                            animation = TweenSpec(
                                                durationMillis = 1000
                                            ),
                                            repeatMode = RepeatMode.Restart
                                        )
                                    )
                                    val c = MaterialTheme.colors.contentColorFor(color.value)
                                        .takeUnless { it == Color.Unspecified } ?: Color.Black
                                    Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
                                        val fl = size.minDimension / 2
                                        drawCircle(
                                            color = c,
                                            center = center,
                                            radius = fl * innerRadiusAnim.value,
                                            style = Stroke()
                                        )
                                        drawCircle(
                                            color = c,
                                            center = center,
                                            radius = fl * innerRadiusAnim.value.plus(0.5f),
                                            style = Stroke()
                                        )
                                    })
                                }
                                if (isDebug) {
                                    Text(
                                        text = "$rowIdx:$colIdx ${
                                            colorUpdates.value?.second?.get(
                                                point
                                            )
                                        }" + if (spawn == point) "S" else ""
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}