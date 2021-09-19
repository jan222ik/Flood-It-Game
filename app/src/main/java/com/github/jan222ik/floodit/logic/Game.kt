package com.github.jan222ik.floodit.logic

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(DelicateCoroutinesApi::class)
class Game(
    val size: IntSize,
    val colorCount: Int,
    val seed: Long = Random.nextLong(),
    initStepCount: Int = 0,
    initMap: Array<Array<Int>>? = null,
    initSourcePoint: Point? = null
) : Parcelable {
    private val rng = Random(this.seed)

    val state = MutableStateFlow(FloodItGameState.LOADING)
    val colorUpdates = MutableStateFlow<Pair<Int, Map<Point, Int>>?>(null)
    val stepCount = MutableStateFlow(initStepCount)
    val solved = MutableStateFlow<Pair<Int, Int>?>(0 to size.height * size.width)

    val map = FloodItMap(
        size = size,
        colorCount = colorCount,
        map = initMap ?: size.let { s ->
            Array(size = s.width) {
                Array(size = s.height) {
                    rng.nextInt(from = 0, until = colorCount)
                }
            }
        },
        initSourcePoint = initSourcePoint
    )

    init {
        GlobalScope.launch {
            state.emit(FloodItGameState.PLACE_SOURCE)
        }
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

    constructor(parcel: Parcel) : this(
        size = IntSize(
            width = parcel.readInt(),
            height = parcel.readInt()
        ),
        colorCount = parcel.readInt(),
        seed = parcel.readLong(),
        initStepCount = parcel.readInt(),
        initMap = parcel.readInt().let { height ->
            Array(parcel.readInt()) {
                val intArray = IntArray(height)
                parcel.readIntArray(intArray)
                intArray.toTypedArray()
            }
        },
        initSourcePoint = if (parcel.readInt() == 0) {
            Point(
                x = parcel.readInt(),
                y = parcel.readInt()
            )
        } else null
    ) {
        GlobalScope.launch {
            if (map.source != null) {
                state.emit(FloodItGameState.PLACE_SOURCE)
            } else {
                state.emit(FloodItGameState.RUNNING)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(size.width)
        parcel.writeInt(size.height)
        parcel.writeInt(colorCount)
        parcel.writeLong(seed)
        parcel.writeInt(stepCount.value)
        // InitMap
        // Height
        parcel.writeInt(size.height)
        // Width
        parcel.writeInt(size.width)
        // Content
        map.map.forEach { row ->
            parcel.writeArray(row)
        }
        // Source Point
        val source = map.source
        if (source != null) {
            parcel.writeInt(0)
            parcel.writeInt(source.x)
            parcel.writeInt(source.y)
        } else {
            parcel.writeInt(1)
        }

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

