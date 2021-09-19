package com.github.jan222ik.floodit.ui.screens.floodit

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.jan222ik.floodit.logic.FloodItGameState
import com.github.jan222ik.floodit.logic.Game
import com.github.jan222ik.floodit.ui.components.*
import com.github.jan222ik.floodit.ui.screens.floodit.parts.HeaderLayout
import com.github.jan222ik.floodit.ui.screens.floodit.parts.PlayingBoard
import com.github.jan222ik.floodit.ui.screens.floodit.parts.SettingsSheet
import com.github.jan222ik.floodit.ui.screens.floodit.parts.ThemeSelectionSheet
import com.github.jan222ik.floodit.ui.theme.theme1
import com.github.jan222ik.floodit.ui.theme.themes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun FloodItScreen() {
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
            var prevShow by remember { mutableStateOf(false) }
            var showInfo by remember(gameState, steps.value) { mutableStateOf(when {
                gameState == FloodItGameState.PLACE_SOURCE -> {
                    allowDecay = false
                    prevShow = true
                    true
                }
                steps.value == 0 -> {
                    allowDecay = false
                    prevShow = true
                    true
                }
                steps.value == 1  -> {
                    allowDecay = true
                    prevShow = false
                    true
                }
                else -> {
                    allowDecay = true
                    prevShow
                }
            }) }
            LaunchedEffect(key1 = showInfo, key2 = allowDecay, block = {
                if (allowDecay && showInfo) {
                    delay(5000L)
                    showInfo = false
                }
            })

            HeaderLayout(
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
                        onClick = {
                            val b = !showInfo
                            showInfo = b
                            prevShow = b
                            allowDecay = true
                        }
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
                        SettingsSheet(
                            scope = scope,
                            game = game,
                            gameState = gameState,
                            theme = theme,
                            steps = steps.value,
                            onClose = { showSettingsOverlay = false },
                            openThemesSelection = {
                                showThemes = true
                            },
                            gameChange = { game = it }
                        )
                    }
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = showThemes,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = fadeIn() + expandIn(Alignment.BottomCenter),
                    content = {
                        ThemeSelectionSheet(
                            theme = theme,
                            onChange = { theme = it},
                            onClose = { showThemes = false }
                        )
                    }
                )
            }
        }

    }
}