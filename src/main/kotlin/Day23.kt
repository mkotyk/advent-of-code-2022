import kotlin.math.max
import kotlin.math.min

fun Set<Point>.findBounds(): Bounds {
    var left = Int.MAX_VALUE
    var right = Int.MIN_VALUE
    var top = Int.MAX_VALUE
    var bottom = Int.MIN_VALUE
    this.forEach { point ->
        if (point.x < left) left = point.x
        if (point.y < top) top = point.y
        if (point.x > right) right = point.x
        if (point.y > bottom) bottom = point.y
    }
    return Bounds(left, top, right, bottom)
}

fun Pair<Int, Int>.range(): IntRange = (min(first, second)..max(first, second))

fun <A, B> List<Pair<A, B>>.firstToSet(): Set<A> = map { it.first }.toSet()
fun <A, B> List<Pair<A, B>>.secondToSet(): Set<B> = map { it.second }.toSet()

fun Bounds.allPoints(): Set<Point> = (this.top to this.bottom).range().flatMap { y ->
    (this.left to this.right).range().map { x ->
        Point(x, y)
    }
}.toSet()

fun main() {
    val sampleInput =
        """
            ....#..
            ..###.#
            #...#.#
            .#...##
            #.###..
            ##.#.##
            .#..#..
        """.trimIndent()

    val puzzleInput = "day23/input.txt".loadResource()

    fun String.parseData(): Set<Point> = this.split('\n').flatMapIndexed { y, line ->
        line.mapIndexedNotNull { x, cell -> if (cell == '#') Point(x, y) else null }
    }.toSet()

    val northCheck = setOf(Direction.TopLeft, Direction.Top, Direction.TopRight) to Direction.Top
    val southCheck = setOf(Direction.BottomLeft, Direction.Bottom, Direction.BottomRight) to Direction.Bottom
    val westCheck = setOf(Direction.BottomLeft, Direction.Left, Direction.TopLeft) to Direction.Left
    val eastCheck = setOf(Direction.BottomRight, Direction.Right, Direction.TopRight) to Direction.Right
    val directionChecks = listOf(northCheck, southCheck, westCheck, eastCheck)

    val rotatingDirectionChecks = sequence {
        val directionCheck = ArrayDeque(directionChecks)
        while (true) {
            yield(directionCheck.toList())
            directionCheck.add(directionCheck.removeFirst())
        }
    }

    fun performRounds(initial: Set<Point>): Sequence<Set<Point>> {
        val allDirections = directionChecks.flatMap { it.first }
        return rotatingDirectionChecks.scan(initial) { elves, directionChecks ->
            val proposals = elves.filter { elf -> !allDirections.none { d -> elf.move(d) in elves } }.mapNotNull { position ->
                directionChecks.firstOrNull { check ->
                    check.first.none { position.move(it) in elves }
                }?.let { check -> position to position.move(check.second) }
            }
            val filtered = proposals.filter {
                proposals.none { (otherPosition, otherProposed) -> otherPosition != it.first && otherProposed == it.second }
            }
            elves - filtered.firstToSet() + filtered.secondToSet()
        }
    }

    // Part 1
    fun Set<Point>.part1(): Int {
        val elves = performRounds(this).elementAt(10)
        val bounds = elves.findBounds()
        return (bounds.bottom - bounds.top + 1) * (bounds.right - bounds.left + 1) - elves.size
    }

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 110)
        println("Sample passed part 1")
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun Set<Point>.part2(): Int = performRounds(this).zipWithNext().indexOfFirst { (a, b) -> a == b } + 1

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 20)
        println("Sample passed part 2")
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}