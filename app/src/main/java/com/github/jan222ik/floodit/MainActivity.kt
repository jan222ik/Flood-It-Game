package com.github.jan222ik.floodit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.github.jan222ik.floodit.logic.FloodItGameState
import com.github.jan222ik.floodit.logic.Game
import com.github.jan222ik.floodit.logic.Point
import com.github.jan222ik.floodit.ui.theme.FloodItTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FloodItTheme(
                darkTheme = true
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val game by remember { mutableStateOf(Game()) }
                    val gameState by game.state.collectAsState()
                    var instructionText by remember { mutableStateOf("") }
                    val showMap by remember(gameState) {
                        mutableStateOf(gameState != FloodItGameState.LOADING)
                    }
                    LaunchedEffect(key1 = Unit) {
                        game.generateBoard(null, IntSize(12, 7))
                    }
                    val solved = game.solved.collectAsState()
                    when (gameState) {
                        FloodItGameState.LOADING -> {
                            instructionText = "Loading"
                        }
                        FloodItGameState.PLACE_SOURCE -> {
                            instructionText = "Click any field to place the flood source"
                        }
                        FloodItGameState.RUNNING -> {
                            instructionText =
                                "Click any tile to switch to its color ${solved.value?.let { "${it.first} / ${it.second}" }}"
                        }
                        FloodItGameState.FINISHED -> {
                            instructionText = "Solved"
                        }
                    }
                    val scope = rememberCoroutineScope()
                    Column {
                        Text(text = instructionText)
                        if (showMap) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (gameState == FloodItGameState.FINISHED) {
                                    Button(onClick = {
                                        scope.launch { game.generateBoard(null, IntSize(12, 7)) }
                                    }) {
                                        Text(text = "Next game!")
                                    }
                                } else {
                                    PlayingBoard(game)
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun PlayingBoard(game: Game) {
    val color1 = MaterialTheme.colors.primary
    val color2 = Color.Red
    val color3 = MaterialTheme.colors.secondary
    val color4 = Color.Yellow
    val color5 = Color.LightGray
    val colors = remember(game) { listOf(color1, color2, color3, color4, color5) }
    val gamestate = game.state.collectAsState()
    val map = remember(game) {
        game.map.map
    }
    val colorUpdates = game.colorUpdates.collectAsState()

    val spawn: Point? = remember(game, gamestate.value) {
        if (gamestate.value == FloodItGameState.RUNNING) {
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
                            remember(game, colorIdx, colors) { mutableStateOf(colors[colorIdx]) }
                        val animColor = animateColorAsState(targetValue = color.value)

                        val job: Job? = remember(game, point, colors, colorUpdates.value) {
                            colorUpdates.value?.second?.get(point)?.let { depth ->
                                scope.launch {
                                    launch(Dispatchers.IO) {
                                        delay(100L * depth)
                                        colorUpdates.value?.first?.let {
                                            color.value = colors[it]
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
                                        }
                                    } else {
                                        scope.launch(Dispatchers.IO) { game.nextColor(point) }
                                    }
                                },
                            backgroundColor = animColor.value
                        ) {
                            Text(
                                text = "$rowIdx:$colIdx ${colorUpdates.value?.second?.get(point)}" + if (spawn == point) "S" else "",
                            )
                        }
                    }
                }
            }
        }
    }
}
