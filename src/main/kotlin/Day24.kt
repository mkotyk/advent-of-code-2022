import java.util.*
import kotlin.math.abs

data class TemporalIdentity(val minute: Int, val position: Point)
data class Blizzard(val position: Point, val direction: Direction) {
    fun isAt(ti: TemporalIdentity, bounds: Bounds): Boolean = when (direction) {
        Direction.Left -> (position.x - bounds.left - ti.minute).mod(bounds.width) + bounds.left == ti.position.x && ti.position.y == position.y
        Direction.Right -> (position.x - bounds.left + ti.minute).mod(bounds.width) + bounds.left == ti.position.x && ti.position.y == position.y
        Direction.Top -> (position.y - bounds.top - ti.minute).mod(bounds.height) + bounds.top == ti.position.y && ti.position.x == position.x
        Direction.Bottom -> (position.y - bounds.top + ti.minute).mod(bounds.height) + bounds.top == ti.position.y && ti.position.x == position.x
        else -> error("Unhandled blizzard direction $direction")
    }
}

operator fun Bounds.contains(point: Point): Boolean = point.x in left..right && point.y in top..bottom
data class State(val start: Point, val end: Point, val bounds: Bounds, val blizzards: Set<Blizzard>)

fun main() {
    val sampleInput =
        """
            #.######
            #>>.<^<#
            #.<..<<#
            #>v.><>#
            #<^v^^>#
            ######.#
        """.trimIndent()

    val puzzleInput = "day24/input.txt".loadResource()

    fun String.parseData(): State = this.split('\n').let {
        val bounds = Bounds(left = 1, top = 1, bottom = it.size - 1, right = it[0].length - 1)
        it.foldIndexed(State(Point(0, 0), Point(0, 0), bounds, emptySet())) { y, stateOuter, line ->
            line.foldIndexed(stateOuter) { x, state, cell ->
                when (cell) {
                    '.' -> when (y) {
                        0 -> state.copy(start = Point(x, y))
                        bounds.bottom -> state.copy(end = Point(x, y))
                        else -> state
                    }

                    '^' -> state.copy(blizzards = state.blizzards + Blizzard(position = Point(x, y), direction = Direction.Top))
                    'v' -> state.copy(blizzards = state.blizzards + Blizzard(position = Point(x, y), direction = Direction.Bottom))
                    '<' -> state.copy(blizzards = state.blizzards + Blizzard(position = Point(x, y), direction = Direction.Left))
                    '>' -> state.copy(blizzards = state.blizzards + Blizzard(position = Point(x, y), direction = Direction.Right))

                    else -> state
                }
            }
        }
    }

    val options = listOf(Direction.Left, Direction.Top, Direction.None, Direction.Bottom, Direction.Right)

    fun search(state: State, startTime: Int = 0): Int {
        fun isSafe(ti: TemporalIdentity): Boolean = ti.position == state.end ||
                ti.position == state.start ||
                (ti.position in state.bounds && state.blizzards.none { it.isAt(ti, state.bounds) })

        val initial = TemporalIdentity(startTime, state.start)
        val seen = mutableSetOf(initial)
        val queue = PriorityQueue(compareBy(IndexedValue<TemporalIdentity>::index))
        queue.add(IndexedValue(0, initial))
        while (!queue.isEmpty()) {
            val entry = queue.remove().value
            if (entry.position == state.end) return entry.minute
            options.forEach { direction ->
                val next = TemporalIdentity(entry.minute + 1, entry.position.move(direction))
                if (isSafe(next) && seen.add(next)) {
                    queue.add(IndexedValue(entry.minute + abs(next.position.x - state.end.x) + abs(next.position.y - state.end.y), next))
                }
            }
        }
        error("No path to end found")
    }

    fun State.part1(): Int = search(this)

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 18)
        println("Sample passed part 1")
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun State.part2(): Int = search(this, search(this.copy(start = this.end, end = this.start), search(this)))

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 54)
        println("Sample passed part 2")
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}