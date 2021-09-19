package com.github.jan222ik.floodit.logic

import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.runBlocking
import java.util.*

class FloodItMap(
    val size: IntSize,
    val colorCount: Int,
    val map: Array<Array<Int>>,
    val initSourcePoint: Point? = null
) {
    var source: Point? = initSourcePoint

    fun placeSource(point: Point) {
        this.source = point
    }

    fun nextColor(newColorIdx: Int): Pair<Int, Map<Point, Int>> {
        val oldColorIdx = map[source!!.x][source!!.y]
        val updates = mutableMapOf<Point, Int>()
        val visited = mutableMapOf<Point, Boolean>()
        val expansion: Queue<Triple<Int, Point, Boolean>> = LinkedList()
        expansion.offer(Triple(0, source!!, true))
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