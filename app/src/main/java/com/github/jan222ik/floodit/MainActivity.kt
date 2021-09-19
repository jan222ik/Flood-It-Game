package com.github.jan222ik.floodit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.jan222ik.floodit.logic.FloodItGameState
import com.github.jan222ik.floodit.logic.Game
import com.github.jan222ik.floodit.logic.Point
import com.github.jan222ik.floodit.ui.theme.FloodItTheme
import com.github.jan222ik.floodit.ui.theme.theme1
import com.github.jan222ik.floodit.ui.theme.themes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FloodItTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var theme by remember { mutableStateOf(theme1) }
                    var game by remember {
                        mutableStateOf(
                            Game(IntSize(12, 7), colorCount = 5),
                            neverEqualPolicy()
                        )
                    }
                    val gameState by remember(game) { game.state }.collectAsState()
                    val showMap by remember(gameState) {
                        mutableStateOf(gameState != FloodItGameState.LOADING)
                    }
                    val steps = game.stepCount.collectAsState()
                    val solved = game.solved.collectAsState()
                    val scope = rememberCoroutineScope()
                    var showSettingsOverlay by remember {
                        mutableStateOf(false)
                    }
                    var showThemes by remember {
                        mutableStateOf(false)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var allowDecay by remember { mutableStateOf(true) }
                            var showInfo by remember(gameState) { mutableStateOf(when {
                                gameState == FloodItGameState.PLACE_SOURCE -> {
                                    allowDecay = false
                                    true
                                }
                                steps.value == 0 -> {
                                    allowDecay = false
                                    true
                                }
                                else -> false
                            }) }
                            LaunchedEffect(key1 = showInfo, key2 = allowDecay, block = {
                                if (allowDecay && showInfo) {
                                    delay(5000L)
                                    showInfo = false
                                }
                            })

                            Header(
                                isExpanded = showInfo,
                                expandingChip = {
                                    ExpandingInfoChip(
                                        text = when (gameState) {
                                            FloodItGameState.LOADING -> "Loading"
                                            FloodItGameState.PLACE_SOURCE -> "Click any field to place the flood source"
                                            FloodItGameState.RUNNING -> "Click any tile to switch to its color"
                                            FloodItGameState.FINISHED -> "Finished Game"
                                        },
                                        isExpanded = showInfo,
                                        onClick = { showInfo = !showInfo }
                                    )
                                },
                                gameInfo = {
                                    Row {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = "Steps")
                                            Text(text = steps.value.toString())
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = "Cleared")
                                            Text(
                                                text = solved.value?.let { (first, second) ->
                                                    first.div(second.toFloat()).times(100).toInt()
                                                }?.toString()?.plus("%") ?: ""
                                            )
                                        }
                                    }
                                },
                                settings = {
                                    Icon(
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .clickable {
                                                showSettingsOverlay = !showSettingsOverlay
                                            },
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                        if (showMap) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                PlayingBoard(game = game, theme = theme)
                                if (showSettingsOverlay || gameState == FloodItGameState.FINISHED) {
                                    Box(modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = MutableInteractionSource(),
                                            indication = null,
                                            onClick = {}
                                        ))
                                }
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = !showThemes && (showSettingsOverlay || gameState == FloodItGameState.FINISHED),
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                    enter = fadeIn() + expandIn(Alignment.BottomCenter),
                                    content = {
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
                                                            Text("Steps ${steps.value}")
                                                        }
                                                    } else {
                                                        Text(
                                                            text = "Settings",
                                                            style = MaterialTheme.typography.h6
                                                        )
                                                        Icon(
                                                            modifier = Modifier
                                                                .align(Alignment.CenterEnd)
                                                                .clickable {
                                                                    showSettingsOverlay = false
                                                                },
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
                                                                .clickable { showThemes = true },
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
                                                var widthSlider by remember(game) {
                                                    mutableStateOf(
                                                        game.size.height
                                                    )
                                                }
                                                var heightSlider by remember(game) {
                                                    mutableStateOf(
                                                        game.size.width
                                                    )
                                                }
                                                var colorCountSlider by remember(game) {
                                                    mutableStateOf(
                                                        game.colorCount
                                                    )
                                                }
                                                CellSlider(
                                                    title = { "Width: $it Cells" },
                                                    value = widthSlider,
                                                    onValueChange = { widthSlider = it },
                                                    valueRange = 3f..7f
                                                )
                                                CellSlider(
                                                    title = { "Height: $it Cells" },
                                                    value = heightSlider,
                                                    onValueChange = { heightSlider = it },
                                                    valueRange = 5f..12f
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
                                                                game = Game(
                                                                    size = game.size,
                                                                    colorCount = game.colorCount,
                                                                    seed = game.seed
                                                                )
                                                                showSettingsOverlay = false
                                                            }
                                                        }) {
                                                        Text(text = "Restart level")
                                                    }
                                                    Button(
                                                        onClick = {
                                                            scope.launch {
                                                                game = Game(
                                                                    size = IntSize(
                                                                        heightSlider,
                                                                        widthSlider
                                                                    ),
                                                                    colorCount = colorCountSlider
                                                                )
                                                                showSettingsOverlay = false
                                                            }
                                                        }) {
                                                        Text(text = "Next game!")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = showThemes,
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                    enter = fadeIn() + expandIn(Alignment.BottomCenter),
                                    content = {
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
                                                            .clickable {
                                                                showThemes = false
                                                            },
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
                                                                theme = it
                                                            },
                                                        isActive = it == theme
                                                    )
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

@Composable
fun Header(
    isExpanded: Boolean,
    expandingChip: @Composable () -> Unit,
    gameInfo: @Composable () -> Unit,
    settings: @Composable () -> Unit
) {
    val notExpandedWidth = remember {
        mutableStateOf<Int?>(null)
    }
    Layout(
        content = {
            expandingChip.invoke()
            gameInfo.invoke()
            settings.invoke()
        }) { measurables, constraints ->
        val p2 = measurables[2].measure(constraints)
        val p0 = measurables[0].measure(constraints.copy(maxWidth = constraints.maxWidth - p2.width)).also {
            if (!isExpanded && it.width < notExpandedWidth.value ?: Int.MAX_VALUE) {
                notExpandedWidth.value = it.width
            }
        }
        val p1 =
            measurables[1].measure(constraints.copy(maxWidth = maxOf(0, constraints.maxWidth - (notExpandedWidth.value ?: 0) - p2.width)))
        val actualHeight = maxOf(0, p0.height, p1.height, p2.height)
        layout(
            width = constraints.maxWidth,
            height = actualHeight
        ) {
            p1.place(x = notExpandedWidth.value ?: 0, y = actualHeight.minus(p1.height).div(2))
            p0.place(x = 0, actualHeight.minus(p0.height).div(2))
            p2.place(x = constraints.maxWidth - p2.width, y = actualHeight.minus(p2.height).div(2))
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun ExpandingInfoChip(text: String, isExpanded: Boolean, onClick: () -> Unit) {
    val iS = remember {
        MutableInteractionSource()
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isExpanded) MaterialTheme.colors.surface else MaterialTheme.colors.background
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .clickable(interactionSource = iS, indication = null, onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandIn(expandFrom = Alignment.CenterStart),
                exit = shrinkOut(shrinkTowards = Alignment.CenterStart) + fadeOut()
            ) {
                Text(text = text)
            }
        }
    }
}

@Composable
fun CellSlider(
    title: (Int) -> String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
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
