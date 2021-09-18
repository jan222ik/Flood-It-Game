package com.github.jan222ik.floodit.logic

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.properties.Delegates
import kotlin.random.Random

@OptIn(DelicateCoroutinesApi::class)
class Game(
    val size: IntSize,
    val colorCount: Int,
    val seed: Long = Random.nextLong()
) : Parcelable {
    private val rng = Random(this.seed)

    val state = MutableStateFlow(FloodItGameState.LOADING)
    val colorUpdates = MutableStateFlow<Pair<Int, Map<Point, Int>>?>(null)
    val stepCount = MutableStateFlow(0)
    val solved = MutableStateFlow<Pair<Int, Int>?>(0 to size.height * size.width)
    val map = FloodItMap(size, colorCount, rng)

    init {
        GlobalScope.launch {
            state.emit(FloodItGameState.PLACE_SOURCE)
        }
    }

    constructor(parcel: Parcel) : this(
        IntSize(
            parcel.readInt(),
            parcel.readInt()
        ),
        parcel.readInt(),
        parcel.readLong()
    ) {

    }

    suspend fun nextColor(point: Point) {
        val color = map.map[point.x][point.y]
        val nextColorUpdates: Pair<Int, Map<Point, Int>> = map.nextColor(color)
        val oldColor = colorUpdates.value
        if (oldColor != null && nextColorUpdates.first != oldColor.first) {
            stepCount.emit(stepCount.value.inc())
        }
        val solveState = solved.value?.copy(first = nextColorUpdates.second.count())
        solved.emit(solveState)
        if (solveState != null && solveState.first == solveState.second) {
            state.emit(FloodItGameState.FINISHED)
        }
        colorUpdates.emit(nextColorUpdates)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(seed)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Game> {
        override fun createFromParcel(parcel: Parcel): Game {
            return Game(parcel)
        }

        override fun newArray(size: Int): Array<Game?> {
            return arrayOfNulls(size)
        }
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
    var source: Point? = null

    val map = Array(size = size.width) {
        Array(size = size.height) {
            rng.nextInt(from = 0, until = colorCount)
        }
    }

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

data class Point(val x: Int, val y: Int)