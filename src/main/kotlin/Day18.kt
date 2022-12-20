import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Point3D(val x: Int, val y: Int, val z: Int) {
    fun isAdjacent(other: Point3D): Boolean = isAdjacentX(other) || isAdjacentY(other) || isAdjacentZ(other)
    fun isAdjacentX(other: Point3D): Boolean = abs(x - other.x) == 1 && y == other.y && z == other.z
    fun isAdjacentY(other: Point3D): Boolean = abs(y - other.y) == 1 && x == other.x && z == other.z
    fun isAdjacentZ(other: Point3D): Boolean = abs(z - other.z) == 1 && y == other.y && x == other.x
}

data class Bounds3D(val minPoint: Point3D, val maxPoint: Point3D) {
    val width: Int get() = maxPoint.x - minPoint.x
    val height: Int get() = maxPoint.y - minPoint.y
    val depth: Int get() = maxPoint.z - minPoint.z
    fun fill(): Set<Point3D> = (minPoint.x until maxPoint.x).flatMap { x ->
        (minPoint.y until maxPoint.y).flatMap { y ->
            (minPoint.z until maxPoint.z).map { z ->
                Point3D(x, y, z)
            }
        }
    }.toSet()
}

fun Set<Point3D>.bounds(): Bounds3D {
    var xMin = Int.MAX_VALUE
    var yMin = Int.MAX_VALUE
    var zMin = Int.MAX_VALUE
    var xMax = Int.MIN_VALUE
    var yMax = Int.MIN_VALUE
    var zMax = Int.MIN_VALUE

    forEach {
        xMin = min(xMin, it.x)
        xMax = max(xMax, it.x)
        yMin = min(yMin, it.y)
        yMax = max(yMax, it.y)
        zMin = min(zMin, it.z)
        zMax = max(zMax, it.z)
    }
    return Bounds3D(Point3D(xMin, yMin, zMin), Point3D(xMax, yMax, zMax))
}

fun main() {
    val sampleInput =
        """
            2,2,2
            1,2,2
            3,2,2
            2,1,2
            2,3,2
            2,2,1
            2,2,3
            2,2,4
            2,2,6
            1,2,5
            3,2,5
            2,1,5
            2,3,5
        """.trimIndent()

    val puzzleInput = "day18/input.txt".loadResource()

    fun String.parseData(): List<Point3D> = this.split('\n').map {
        val r = it.split(',')
        Point3D(r[0].toInt(), r[1].toInt(), r[2].toInt())
    }

    data class Cube(val point: Point3D, val nextTo: MutableSet<Point3D> = mutableSetOf(), var visibleSides: Int = 6) {
        fun adjacent(other: Cube): Boolean {
            return if (other.point !in nextTo && point !in other.nextTo && point.isAdjacent(other.point)) {
                other.nextTo.add(this.point)
                nextTo.add(other.point)
                this.visibleSides--
                other.visibleSides--
                true
            } else false
        }
    }

    fun Set<Cube>.linkNeighbors(): Set<Cube> {
        this.forEach { a ->
            this.forEach { b -> if (a != b) a.adjacent(b) }
        }
        return this
    }

    // Part 1
    fun List<Point3D>.part1(): Int {
        val cubes = this.map { Cube(it) }.toSet().linkNeighbors()
        return cubes.sumOf { it.visibleSides }
    }

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 64)
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    fun Set<Cube>.allTouching(p: Point3D): Set<Cube> {
        val geoMap = this.associateBy { it.point }
        val result = mutableSetOf<Cube>()
        fun followNeighbors(start: Point3D) {
            val c = geoMap[start] ?: error("Point not a know cube")
            if (c in result) return
            result.add(c)
            c.nextTo.forEach { followNeighbors(it) }
        }
        followNeighbors(p)
        return result
    }

    // Part 2
    fun List<Point3D>.part2(): Int {
        val bounds = this.toSet().bounds().let {
            Bounds3D(
                Point3D(it.minPoint.x - 1, it.minPoint.y - 1, it.minPoint.z - 1),
                Point3D(it.maxPoint.x + 1, it.maxPoint.y + 1, it.maxPoint.z + 1)
            )
        }
        val allCubes = bounds.fill().map { Cube(it) }.toSet()
        val lavaCubes = this.map { Cube(it) }.toSet()
        val externalCubes = (allCubes - lavaCubes).linkNeighbors()
        val outsideCubes = externalCubes.allTouching(bounds.minPoint)
        val airAndLavaCubes = allCubes - outsideCubes
        val airCubes = airAndLavaCubes - lavaCubes
        return lavaCubes.linkNeighbors().sumOf { it.visibleSides } - airCubes.linkNeighbors().sumOf { it.visibleSides }
    }

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 58)
    }

    with(puzzleInput.parseData()) {
        val result = part2()
        println("Part 2 result: $result")
    }
}