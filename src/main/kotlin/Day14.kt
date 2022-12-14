typealias RockWalls = List<List<Point>>
typealias Bitmap = Array<CharArray>

data class Bounds(val left: Int, val top: Int, val right: Int, val bottom: Int)

fun Point.lineTo(other: Point, plot: (Point) -> Unit) {
    if (this.x == other.x) {
        val yr = if (y < other.y) y..other.y else other.y..y
        for (yp in yr) {
            plot(Point(x, yp))
        }
    }
    if (this.y == other.y) {
        val xr = if (x < other.x) x..other.x else other.x..x
        for (xp in xr) {
            plot(Point(xp, y))
        }
    }
}

fun Bounds.translate(p: Point) = Point(p.x - left, p.y - top)

fun main() {
    val sampleInput =
        """
            498,4 -> 498,6 -> 496,6
            503,4 -> 502,4 -> 502,9 -> 494,9
        """.trimIndent()

    val puzzleInput = "day14/input.txt".loadResource()

    fun String.parsePoint(): Point {
        val (x, y) = split(',')
        return Point(x.toInt(), y.toInt())
    }

    fun String.parseData(): RockWalls = this.split('\n').map { it.split(" -> ").map { pt -> pt.parsePoint() } }

    fun RockWalls.findBounds(): Bounds {
        var left = Int.MAX_VALUE
        var right = Int.MIN_VALUE
        var top = Int.MAX_VALUE
        var bottom = Int.MIN_VALUE
        this.forEach {
            it.forEach { point ->
                if (point.x < left) left = point.x
                if (point.y < top) top = point.y
                if (point.x > right) right = point.x
                if (point.y > bottom) bottom = point.y
            }
        }
        return Bounds(left, top, right, bottom)
    }

    fun Bitmap.plot(p: Point, v: Char) {
        if (p.x >= 0 && p.y >= 0 && p.y < this.size && p.x < this[0].size) this[p.y][p.x] = v
    }

    fun Bitmap.at(p: Point): Char = if (p.x < 0 || p.y < 0 || p.y >= this.size || p.x >= this[0].size) '.' else this[p.y][p.x]

    fun RockWalls.drawWalls(bounds: Bounds): Bitmap {
        val width = (bounds.right - bounds.left) + 1
        val height = (bounds.bottom - bounds.top) + 1
        val bitmap = Array<CharArray>(height) { CharArray(width) { '.' } }
        this.forEach {
            it.zipWithNext { a, b -> a.lineTo(b) { p -> bitmap.plot(bounds.translate(p), '#') } }
        }
        return bitmap
    }

    fun Bitmap.dropSand(sandOrigin: Point, bounds: Bounds): Boolean {
        var sand = sandOrigin
        while (true) {
            if (sand.y > bounds.bottom)
                return false
            val below = sand.move(Direction.Bottom)
            when (at(bounds.translate(below))) {
                'O', '#' -> {
                    if (at(bounds.translate(below.move(Direction.Left))) == '.') {
                        sand = sand.move(Direction.Left)
                        continue
                    }
                    if (at(bounds.translate(below.move(Direction.Right))) == '.') {
                        sand = sand.move(Direction.Right)
                        continue
                    }
                    break
                }
                '.' -> sand = sand.move(Direction.Bottom)
            }
        }
        plot(bounds.translate(sand), 'O')
        return sand != sandOrigin
    }

    fun Bitmap.show() {
        println()
        this.forEach { println(it.joinToString("")) }
    }

    // Part 1
    fun RockWalls.part1(show: Boolean = false): Int {
        val bounds = findBounds().copy(top = 0)
        val bitmap = drawWalls(bounds)
        val sandOrigin = Point(500, 0)
        bitmap.plot(bounds.translate(sandOrigin), '+')
        if (show) bitmap.show()
        var count = 0;
        while (bitmap.dropSand(sandOrigin, bounds)) {
            count++
            if (show) bitmap.show()
        }
        return count
    }

    with(sampleInput.parseData()) {
        Assert.equals(part1(show = true), 24)
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun RockWalls.part2(show: Boolean = false, size: Int = 500): Int {
        val bounds = findBounds().copy(top = 0)
        return (this + listOf(listOf(Point(500 - size, bounds.bottom + 2), Point(500 + size, bounds.bottom + 2)))).part1(show) + 1
    }

    with(sampleInput.parseData()) {
        Assert.equals(part2(show = true, size = 100), 93)
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}