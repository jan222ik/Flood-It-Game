package com.github.jan222ik.floodit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.jan222ik.floodit.logic.FloodItGameState
import com.github.jan222ik.floodit.logic.Game
import com.github.jan222ik.floodit.logic.Point
import com.github.jan222ik.floodit.ui.theme.FloodItTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FloodItTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var game by remember { mutableStateOf(Game(IntSize(12, 7), colorCount = 5), neverEqualPolicy()) }
                    val gameState by remember(game) { game.state }.collectAsState()
                    var instructionText by remember { mutableStateOf("") }
                    val showMap by remember(gameState) {
                        mutableStateOf(gameState != FloodItGameState.LOADING)
                    }
                    val steps = game.stepCount.collectAsState()
                    val solved = game.solved.collectAsState()
                    instructionText = when (gameState) {
                        FloodItGameState.LOADING -> "Loading"
                        FloodItGameState.PLACE_SOURCE -> "Click any field to place the flood source"
                        FloodItGameState.RUNNING -> "Click any tile to switch to its color ${solved.value?.let { "${it.first} / ${it.second}" }} Step: ${steps.value}"
                        else -> ""
                    }
                    val scope = rememberCoroutineScope()
                    var showSettingsOverlay by remember {
                        mutableStateOf(false)
                    }
                    Column {
                        Row {
                            Text(text = instructionText)
                            Icon(
                                modifier = Modifier.clickable { showSettingsOverlay = !showSettingsOverlay },
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null
                            )
                        }
                        if (showMap) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                PlayingBoard(game)
                                if (gameState == FloodItGameState.FINISHED) {
                                    Box(modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = MutableInteractionSource(),
                                            indication = null,
                                            onClick = {}
                                        ))
                                }
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = showSettingsOverlay || gameState == FloodItGameState.FINISHED,
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                    enter = fadeIn() + expandIn(Alignment.BottomCenter),
                                    content = {
                                        Card(
                                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("You solved it!")
                                                Text("Steps ${steps.value}")
                                                var widthSlider by remember(game) { mutableStateOf(game.size.height) }
                                                var heightSlider by remember(game) { mutableStateOf(game.size.width) }
                                                var colorCountSlider by remember(game) { mutableStateOf(game.colorCount) }
                                                CellSlider(
                                                    title = { "Width: $it Cells" },
                                                    value = widthSlider,
                                                    onValueChange = { widthSlider = it },
                                                    valueRange = 3f..7f
                                                )
                                                CellSlider(
                                                    title = {"Height: $it Cells" },
                                                    value = heightSlider,
                                                    onValueChange = { heightSlider = it },
                                                    valueRange = 5f..12f
                                                )
                                                CellSlider(
                                                    title = {"Color Count: $it" },
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
                                                                game = Game(
                                                                    size = game.size,
                                                                    colorCount = game.colorCount,
                                                                    seed = game.seed
                                                                )

                                                            }
                                                        }) {
                                                        Text(text = "Restart level")
                                                    }
                                                    Button(
                                                        onClick = {
                                                            scope.launch {
                                                                game = Game(
                                                                    size = IntSize(heightSlider, widthSlider),
                                                                    colorCount = colorCountSlider
                                                                )
                                                            }
                                                        }) {
                                                        Text(text = "Next game!")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun CellSlider(title: (Int) -> String, value: Int, onValueChange: (Int) -> Unit, valueRange: ClosedFloatingPointRange<Float>) {
    Text(text = title(value))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = valueRange.start.toInt().toString(),
        )
        Slider(
            modifier = Modifier.weight(1f, false),
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = valueRange,
            steps = (valueRange.endInclusive - valueRange.start).dec().toInt()
        )
        Text(
            text = valueRange.endInclusive.toInt().toString(),
        )
    }
}

@Composable
fun PlayingBoard(game: Game, isDebug: Boolean = false) {
    val color1 = MaterialTheme.colors.primary
    val color2 = Color.Red
    val color3 = MaterialTheme.colors.secondary
    val color4 = Color.Yellow
    val color5 = Color.LightGray
    val color6 = Color.Green
    val colors = listOf(color1, color2, color3, color4, color5, color6)
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
                                    val c = MaterialTheme.colors.contentColorFor(color.value).takeUnless { it == Color.Unspecified } ?: Color.Black
                                    Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
                                        val fl = size.minDimension / 2
                                        drawCircle(color = c, center = center, radius = fl * innerRadiusAnim.value, style = Stroke())
                                        drawCircle(color = c, center = center, radius = fl * innerRadiusAnim.value.plus(0.5f), style = Stroke())
                                    })
                                }
                                if (isDebug) {
                                    Text(text = "$rowIdx:$colIdx ${colorUpdates.value?.second?.get(point)}" + if (spawn == point) "S" else "")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
