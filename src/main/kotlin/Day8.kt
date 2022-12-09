import Directions.LEFT
import Directions.RIGHT
import Directions.UP
import Directions.DOWN
import Directions.ALL

typealias TreeGrid = Array<IntArray>

object Directions {
    const val RIGHT = 8
    const val LEFT = 4
    const val UP = 2
    const val DOWN = 1
    const val ALL = 15
}

enum class Axis(val x: Int, val y: Int) {
    X_POS(1, 0),
    X_NEG(-1, 0),
    Y_POS(0, 1),
    Y_NEG(0, -1);

    fun abs(): Axis = when (this) {
        X_POS, Y_POS -> this
        X_NEG -> X_POS
        Y_NEG -> Y_POS
    }
}

data class Point(val x: Int, val y: Int) {
    operator fun times(axis: Axis): Int = x * axis.x + y * axis.y
    fun replaceAxis(axis: Axis, i: Int): Point = when (axis) {
        Axis.X_POS, Axis.X_NEG -> Point(i, y)
        Axis.Y_POS, Axis.Y_NEG -> Point(x, i)
    }

    fun walk(dest: Point, axis: Axis, predicate: (Point) -> Boolean): Int {
        var count = 0
        var i = this * axis.abs()
        val step = Point(1, 1) * axis
        while (i != dest * axis.abs()) {
            i += step
            count++
            if (!predicate(this.replaceAxis(axis, i))) break
        }
        return count
    }
}

fun main() {
    val sampleInput =
        """
          30373
          25512
          65332
          33549
          35390
        """.trimIndent()

    val puzzleInput = "day8/input.txt".loadResource()
    fun String.parseData(): TreeGrid =
        this.split('\n').map { line -> line.map { it - '0' }.toIntArray() }.toTypedArray()

    fun TreeGrid.at(point: Point): Int =
        if (point.y < 0 || point.y >= this.size || point.x < 0 || point.x >= this[0].size)
            -1
        else
            this[point.y][point.x]

    fun <R> TreeGrid.mapNotNull(block: (Point, Int) -> R?): List<R> = sequence {
        for (y in 0 until this@mapNotNull.size) {
            for (x in 0 until this@mapNotNull[0].size) {
                val p = Point(x, y)
                block(p, at(p))?.let { yield(it) }
            }
        }
    }.toList()

    // Part 1
    fun TreeGrid.part1(): Set<Point> = mapNotNull { p, treeHeight ->
        var blocked = 0
        for (i in 1 until this.size) {
            blocked = blocked or ((if (treeHeight <= at(p.copy(x = p.x + i))) RIGHT else 0)
                    + (if (treeHeight <= at(p.copy(x = p.x - i))) LEFT else 0)
                    + (if (treeHeight <= at(p.copy(y = p.y - i))) UP else 0)
                    + (if (treeHeight <= at(p.copy(y = p.y + i))) DOWN else 0))
            if (blocked == ALL) break // Blocked in all directions
        }
        if (blocked != ALL) p else null
    }.toSet()

    with(sampleInput.parseData()) {
        val expectedVisibleSet = setOf(
            Point(0, 0),
            Point(1, 0),
            Point(2, 0),
            Point(3, 0),
            Point(4, 0),
            Point(0, 1),
            Point(0, 2),
            Point(0, 3),
            Point(4, 1),
            Point(4, 2),
            Point(4, 3),
            Point(0, 4),
            Point(1, 4),
            Point(2, 4),
            Point(3, 4),
            Point(4, 4),

            Point(1, 1), // 5 Top
            Point(2, 1), // 5 Top
            Point(2, 3), // 5 Bottom
            Point(3, 2), // 3 Middle
            Point(1, 2) // 5 *visible from the right side!*
        )
        Assert.equals(expectedVisibleSet.size, 21)
        val result = part1()
        Assert.equals(emptySet(), expectedVisibleSet - result)
        Assert.equals(emptySet(), result - expectedVisibleSet)
        Assert.equals(result.size, 21)
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1().size}")
    }

    // Part 2
    fun TreeGrid.score(point: Point): Int {
        val treeHeight = at(point)
        val upDistance = point.walk(point.copy(y = 0), Axis.Y_NEG) { treeHeight > at(it) }
        val downDistance = point.walk(point.copy(y = this.size - 1), Axis.Y_POS) { treeHeight > at(it) }
        val leftDistance = point.walk(point.copy(x = 0), Axis.X_NEG) { treeHeight > at(it) }
        val rightDistance = point.walk(point.copy(x = this[0].size - 1), Axis.X_POS) { treeHeight > at(it) }
        return upDistance * downDistance * leftDistance * rightDistance
    }

    fun TreeGrid.part2(): Pair<Int, Point> = mapNotNull { p, _ -> Pair(score(p), p) }.maxBy { it.first }

    with(sampleInput.parseData()) {
        val result = part2()
        Assert.equals(result.first, 8)
        Assert.equals(result.second, Point(2, 3))
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}