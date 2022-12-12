typealias TopologyMap = Array<CharArray>

val TopologyMap.height: Int get() = this.size
val TopologyMap.width: Int get() = this[0].size
fun TopologyMap.at(p: Point): Char = this[p.y][p.x]
fun TopologyMap.findPoint(i: Char): Point = mapIndexedNotNull { y, row ->
    val x = row.indexOf(i)
    if (x < 0) null else Point(x, y)
}.firstOrNull() ?: error("Point not found for [$i]")

fun TopologyMap.forAll(block: (Point, Char) -> Unit) {
    this.forEachIndexed { y, row ->
        row.forEachIndexed { x, c -> block(Point(x, y), c) }
    }
}

fun main() {
    val sampleInput =
        """
            Sabqponm
            abcryxxl
            accszExk
            acctuvwj
            abdefghi
        """.trimIndent()

    val puzzleInput = "day12/input.txt".loadResource()

    fun String.parseData(): TopologyMap = this.split('\n').map { line -> line.toCharArray() }.toTypedArray()

    fun Char.fix(): Char = when (this) {
        'S' -> 'a'
        'E' -> 'z'
        else -> this
    }

    fun TopologyMap.canMove(from: Point, target: Point): Boolean {
        return if (target.x in 0 until width && target.y in 0 until height) {
            val heightDelta = (at(from).fix() - at(target).fix())
            heightDelta <= 1
        } else {
            false
        }
    }

    val directions = listOf(Direction.Right, Direction.Top, Direction.Bottom, Direction.Left)

    fun TopologyMap.walkPaths(
        from: Point,
        distanceMap: MutableMap<Point, Int>,
        distance: Int = 0,
        endCriteria: (Point) -> Boolean
    ): List<Int> {
        return if (endCriteria(from)) {
            listOf(distance)
        } else {
            directions.fold(emptyList()) { acc, direction ->
                val target = from.move(direction)
                if (canMove(from, target) && (distanceMap[target]?.let { it > distance + 1 } != false)) {
                    distanceMap[target] = distance + 1
                    acc + walkPaths(target, distanceMap, distance + 1, endCriteria)
                } else {
                    acc
                }
            }
        }
    }

    // Part 1
    fun TopologyMap.part1(): Int {
        val distanceMap = mutableMapOf<Point, Int>()
        val start = this.findPoint('E')
        val end = this.findPoint('S')
        val routes = walkPaths(start, distanceMap) { it == end }
        return routes.min()
    }

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 31)
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun TopologyMap.part2(): Int {
        val distanceMap = mutableMapOf<Point, Int>()
        val start = this.findPoint('E')
        val routes = walkPaths(start, distanceMap) { at(it) == 'a' }
        return routes.min()
    }

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 29)
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}