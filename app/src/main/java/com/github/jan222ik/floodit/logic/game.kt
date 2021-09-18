package com.github.jan222ik.floodit.logic

import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.properties.Delegates
import kotlin.random.Random

class Game {
    private var rng by Delegates.notNull<Random>()
    var map by Delegates.notNull<FloodItMap>()

    val state = MutableStateFlow(FloodItGameState.LOADING)
    val colorUpdates = MutableStateFlow<Pair<Int, Map<Point, Int>>?>(null)
    val solved = MutableStateFlow<Pair<Int, Int>?>(null)

    var seed by Delegates.notNull<Long>()

    suspend fun generateBoard(seed: Long?, size: IntSize) {
        this.seed = seed ?: Random.nextLong()
        state.emit(FloodItGameState.LOADING)
        rng = Random(this.seed)
        map = FloodItMap(size, 5, rng)
        println("map = ${map}")
        solved.emit(0 to size.height * size.width)
        colorUpdates.emit(null)
        state.emit(FloodItGameState.PLACE_SOURCE)
    }

    suspend fun nextColor(point: Point) {
        val color = map.map[point.x][point.y]
        val nextColorUpdates: Pair<Int, Map<Point, Int>> = map.nextColor(color)
        val solveState = solved.value?.copy(first = nextColorUpdates.second.count())
        solved.emit(solveState)
        if (solveState != null && solveState.first == solveState.second) {
            state.emit(FloodItGameState.FINISHED)
        }
        colorUpdates.emit(nextColorUpdates)
    }
}

enum class FloodItGameState {
    LOADING, PLACE_SOURCE, RUNNING, FINISHED
}

class FloodItMap(
    val size: IntSize,
    val colorCount: Int,
    rng: Random
) {
    lateinit var source: Point

    val map = Array(size = size.width) {
        Array(size = size.height) {
            rng.nextInt(from = 0, until = colorCount)
        }
    }

    fun placeSource(point: Point) {
        this.source = point
    }

    fun nextColor(newColorIdx: Int): Pair<Int, Map<Point, Int>> {
        val oldColorIdx = map[source.x][source.y]
        val updates = mutableMapOf<Point, Int>()
        val visited = mutableMapOf<Point, Boolean>()
        val expansion: Queue<Triple<Int, Point, Boolean>> = LinkedList()
        expansion.offer(Triple(0, source, true))
        var countNewColored = 1
        while (expansion.isNotEmpty()) {
            val (depth, point, notEdge) = expansion.poll()!!
            // Add to list of visited points
            visited[point] = notEdge
            val fieldColorIdx = map[point.x][point.y]
            if (notEdge && fieldColorIdx == oldColorIdx) {
                // Increment count to check if board is solved
                countNewColored += 1
                // Add point to list of points updated in this depth
                updates[point] = depth
                // Update color index of point on map
                map[point.x][point.y] = newColorIdx
                // Expand upon cardinal directions of point
                depth.inc().let { newDepth ->
                    listOf(point.north, point.east, point.south, point.west)
                        .forEach { cardinal ->
                            cardinal?.takeUnless { visited[it] == true }
                                ?.let { expansion.offer(Triple(newDepth, it, true)) }
                        }
                }
            } else if (fieldColorIdx == newColorIdx) {
                countNewColored += 1
                updates[point] = depth
                depth.inc().let { newDepth ->
                    listOf(point.north, point.east, point.south, point.west)
                        .forEach { cardinal ->
                            cardinal?.takeUnless(visited::contains)
                                ?.let { expansion.offer(Triple(newDepth, it, false)) }
                        }
                }
            }
        }
        println("countNewColored = ${countNewColored} need: ${size.width * size.height}")
        runBlocking {
           // map.emit(map.value)
        }
        if (countNewColored == size.width * size.height) {
            println("Solved")
        }
        return newColorIdx to updates
    }

    val Point.north: Point?
        get() = y.dec().takeIf { it >= 0 }?.let { this.copy(y = it) }
    val Point.east: Point?
        get() = x.inc().takeIf { it < size.width }?.let { this.copy(x = it) }
    val Point.south: Point?
        get() = y.inc().takeIf { it < size.height }?.let { this.copy(y = it) }
    val Point.west: Point?
        get() = x.dec().takeIf { it >= 0 }?.let { this.copy(x = it) }

    override fun toString(): String {
        return "FloodItMap(size=$size, colorCount=$colorCount, map=\n${map.joinToString(separator = "\n") { it.contentToString() }})"
    }


}

data class Point(val x: Int, val y: Int)