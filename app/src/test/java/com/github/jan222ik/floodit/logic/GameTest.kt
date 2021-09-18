package com.github.jan222ik.floodit.logic

import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

class GameTest {
    @Test
    fun boundsTest() {
        runBlocking {
            Game.generateBoard(seed = 1L, IntSize(3, 3))
            with(Game.map) {
                val middle = Point(1, 1)
                assertEquals(middle.copy(y = 0), middle.north)
                assertEquals(middle.copy(x = 0), middle.west)
                assertEquals(middle.copy(x = 2), middle.east)
                assertEquals(middle.copy(y = 2), middle.south)

                val topLeft = Point(0, 0)
                assertNull(topLeft.north)
                assertNull(topLeft.west)
                assertEquals(topLeft.copy(x = 1), topLeft.east)
                assertEquals(topLeft.copy(y = 1), topLeft.south)

                val topRight = Point(3, 0)
                assertNull(topRight.north)
                assertNull(topRight.east)
                assertEquals(topRight.copy(x = 2), topRight.west)
                assertEquals(topRight.copy(y = 1), topRight.south)

                val bottomLeft = Point(0, 3)
                assertNull(bottomLeft.south)
                assertNull(bottomLeft.west)
                assertEquals(bottomLeft.copy(x = 1), bottomLeft.east)
                assertEquals(bottomLeft.copy(y = 2), bottomLeft.north)

                val bottomRight = Point(3, 3)
                assertNull(bottomRight.south)
                assertNull(bottomRight.east)
                assertEquals(bottomRight.copy(x = 2), bottomRight.west)
                assertEquals(bottomRight.copy(y = 2), bottomRight.north)
            }
        }
    }
}