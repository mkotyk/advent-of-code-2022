import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

enum class Direction {
    Top,
    Bottom,
    Left,
    Right,
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight,
    None
}

fun Point.move(direction: Direction): Point = when (direction) {
    Direction.Top -> copy(y = y - 1)
    Direction.Bottom -> copy(y = y + 1)
    Direction.Left -> copy(x = x - 1)
    Direction.Right -> copy(x = x + 1)
    Direction.TopLeft -> copy(x = x - 1, y = y - 1)
    Direction.TopRight -> copy(x = x + 1, y = y - 1)
    Direction.BottomLeft -> copy(x = x - 1, y = y + 1)
    Direction.BottomRight -> copy(x = x + 1, y = y + 1)
    Direction.None -> this
}

fun Point.distance(other: Point): Int {
    val a = x - other.x
    val b = y - other.y
    return sqrt((a * a + b * b).toDouble()).toInt()
}

fun Point.heading(other: Point): Direction {
    val xDelta = max(min(x - other.x, 1), -1)
    val yDelta = max(min(y - other.y, 1), -1)
    return when (xDelta to yDelta) {
        0 to 0 -> Direction.None
        1 to 0 -> Direction.Left
        -1 to 0 -> Direction.Right
        0 to -1 -> Direction.Bottom
        0 to 1 -> Direction.Top
        1 to 1 -> Direction.TopLeft
        -1 to -1 -> Direction.BottomRight
        -1 to 1 -> Direction.TopRight
        1 to -1 -> Direction.BottomLeft
        else -> error("Unknown direction to move [$xDelta,$yDelta]")
    }
}

class Knot(start: Point = Point(0, 0), val followKnot: Knot? = null, val followDistance: Int = 1) {
    var currentPosition: Point = start
    val backtrack = mutableSetOf<Point>()

    fun move(direction: Direction) {
        currentPosition = currentPosition.move(direction)
        backtrack.add(currentPosition)
        followKnot?.let {
            val followDirection = followKnot.follow(currentPosition)
            followKnot.move(followDirection)
        }
    }

    fun follow(position: Point): Direction {
        return if (currentPosition.distance(position) > followDistance) {
            // Must move closer
            currentPosition.heading(position)
        } else {
            Direction.None
        }
    }
}

fun main() {
    val sampleInput =
        """
        R 4
        U 4
        L 3
        D 1
        R 4
        D 1
        L 5
        R 2
        """.trimIndent()

    val sampleInput2 =
        """
        R 5
        U 8
        L 8
        D 3
        R 17
        D 10
        L 25
        U 20
        """.trimIndent()

    val puzzleInput = "day9/input.txt".loadResource()

    fun String.parseData(): List<String> = this.split('\n')

    fun String.mapDirection(): Direction = when (this) {
        "R" -> Direction.Right
        "L" -> Direction.Left
        "U" -> Direction.Top
        "D" -> Direction.Bottom
        else -> error("Unknown direction")
    }

    fun List<String>.applyInstructions(knot: Knot) = forEach { instruction ->
        val (direction, count) = instruction.split(' ')
        val mappedDirection = direction.mapDirection()
        for (i in 0 until count.toInt()) knot.move(mappedDirection)
    }

    // Part 1
    fun List<String>.part1(): Int {
        val tailKnot = Knot()
        val headKnot = Knot(followKnot = tailKnot)
        this.applyInstructions(headKnot)
        return tailKnot.backtrack.size
    }

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 13)
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun List<String>.part2(): Int {
        val tailKnot = Knot()
        val headKnot = (1 until 10).fold(tailKnot) { acc, _ -> Knot(followKnot = acc) }
        this.applyInstructions(headKnot)
        return tailKnot.backtrack.size
    }

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 1)
    }

    with(sampleInput2.parseData()) {
        Assert.equals(part2(), 36)
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}