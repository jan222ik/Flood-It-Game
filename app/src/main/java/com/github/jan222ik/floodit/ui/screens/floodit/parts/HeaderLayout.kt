package com.github.jan222ik.floodit.ui.screens.floodit.parts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.Layout

@Composable
fun HeaderLayout(
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