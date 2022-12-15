import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun Point.manhattanDistance(other: Point) = abs(x - other.x) + abs(y - other.y)

data class ClosedPoly(val vertices: Set<Point>)
data class Line(val a: Point, val b: Point)

fun Line.slope(): Double = (b.y - a.y).toDouble() / (b.x - a.x).toDouble()
fun Line.intercept(): Int = a.y - (slope() * a.x).toInt()

fun Line.intersects(other: Line): Point? {
    val m1 = this.slope()
    val b1 = this.intercept()
    val m2 = other.slope()
    val b2 = other.intercept()
    if (m1 == m2) return null
    val x = (b2 - b1) / (m1 - m2)
    return if (min(a.x, b.x) <= x && x <= max(a.x, b.x) && min(other.a.x, other.b.x) <= x && x <= max(other.a.x, other.b.x))
        Point(x.toInt(), y = (m1 * x + b1).toInt())
    else null
}

fun ClosedPoly.intersects(other: Line): Set<Point> = (vertices.zipWithNext { a, b ->
    Line(a, b).intersects(other)
}.toSet() + Line(vertices.last(), vertices.first()).intersects(other)).filterNotNull().toSet()

fun main() {
    val sampleInput =
        """
            Sensor at x=2, y=18: closest beacon is at x=-2, y=15
            Sensor at x=9, y=16: closest beacon is at x=10, y=16
            Sensor at x=13, y=2: closest beacon is at x=15, y=3
            Sensor at x=12, y=14: closest beacon is at x=10, y=16
            Sensor at x=10, y=20: closest beacon is at x=10, y=16
            Sensor at x=14, y=17: closest beacon is at x=10, y=16
            Sensor at x=8, y=7: closest beacon is at x=2, y=10
            Sensor at x=2, y=0: closest beacon is at x=2, y=10
            Sensor at x=0, y=11: closest beacon is at x=2, y=10
            Sensor at x=20, y=14: closest beacon is at x=25, y=17
            Sensor at x=17, y=20: closest beacon is at x=21, y=22
            Sensor at x=16, y=7: closest beacon is at x=15, y=3
            Sensor at x=14, y=3: closest beacon is at x=15, y=3
            Sensor at x=20, y=1: closest beacon is at x=15, y=3
        """.trimIndent()

    val puzzleInput = "day15/input.txt".loadResource()

    fun String.parseData(): Map<Point, Point> = this.split('\n').map {
        val expr = "Sensor at x=([+-]?\\d+), y=([+-]?\\d+): closest beacon is at x=([+-]?\\d+), y=([+-]?\\d+)".toRegex()
        val result = expr.matchEntire(it) ?: error("Malformed inpput")
        Point(result.groupValues[1].toInt(), result.groupValues[2].toInt()) to Point(result.groupValues[3].toInt(), result.groupValues[4].toInt())
    }.toMap()

    fun expandSensorRanges(sensor: Point, beacon: Point): ClosedPoly {
        val dist = sensor.manhattanDistance(beacon)
        return ClosedPoly(
            vertices = setOf(
                Point(sensor.x - dist, sensor.y),
                Point(sensor.x, sensor.y + dist),
                Point(sensor.x + dist, sensor.y),
                Point(sensor.x, sensor.y - dist)
            )
        )
    }

    fun Map<Point, Point>.sensorCoverageAt(y: Int): List<IntRange> {
        val intersectingLine = Line(Point(Int.MIN_VALUE, y), Point(Int.MAX_VALUE, y))
        val sensorPolys = this.entries.map { expandSensorRanges(it.key, it.value) }
        val lines = sensorPolys.map { it.intersects(intersectingLine) }.filter { it.isNotEmpty() }
        return lines.map { seg ->
            if (seg.size == 1) {
                IntRange(seg.first().x, seg.first().x)
            } else {
                val a = seg.first().x
                val b = seg.drop(1).first().x
                IntRange(min(a, b), max(a, b))
            }
        }
    }

    // Part 1
    fun Map<Point, Point>.part1(y: Int): Int {
        val beaconsOnLine = this.values.toSet().count { it.y == y }
        return sensorCoverageAt(y).flatMap { (it.first..it.last).toSet() }.toSet().count() - beaconsOnLine
    }

    with(sampleInput.parseData()) {
        Assert.equals(part1(10), 26)
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1(2000000)}")
    }

    // Part 2
    fun Map<Point, Point>.part2(range: IntRange): Long {
        range.forEach { y ->
            val sensorCoverageAtY = sensorCoverageAt(y).sortedBy { it.first }.reduce { a, b ->
                if (b.first in a) {
                    IntRange(min(a.first, b.first), max(a.last, b.last))
                } else {
                    // We have a Gap
                    println("Gap at $y")
                    return (min(a.first, b.first)..max(a.last, b.last)).first { v -> v !in a && v !in b }.let { x ->
                        x * 4000000L + y
                    }
                }
            }
        }
        error("No gaps in sensor network detected")
    }

    with(sampleInput.parseData()) {
        Assert.equals(part2(0..20), 56000011)
    }

    // 29214561 too low
    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2(0..4000000)}")
    }
}