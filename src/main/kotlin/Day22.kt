import java.util.*

sealed class Instruction {
    data class Forward(val count: Int) : Instruction()
    object Left : Instruction()
    object Right : Instruction()
}

data class Board(
    val map: Bitmap,
    val initialPosition: Point,
    val perimeter: List<Location>,
    val walls: Set<Point>,
    val adjacent: Map<Location, Location> = emptyMap()
)

data class Location(val position: Point, val direction: Direction = Direction.None)

infix operator fun Point.rangeTo(end: Point) = Line(this, end)

operator fun Direction.plus(other: Direction): Direction {
    return when (other) {
        Direction.Left -> when (this) {
            Direction.Left -> Direction.Bottom
            Direction.Right -> Direction.Top
            Direction.Top -> Direction.Left
            Direction.Bottom -> Direction.Right
            Direction.None -> this
            else -> error("Unhandled directional change")
        }

        Direction.Right -> when (this) {
            Direction.Left -> Direction.Top
            Direction.Right -> Direction.Bottom
            Direction.Top -> Direction.Right
            Direction.Bottom -> Direction.Left
            Direction.None -> this
            else -> error("Unhandled directional change")
        }

        Direction.None -> this

        else -> error("Unhandled directional change")
    }
}

fun main() {
    val sampleInput =
        """
        |        ...#
        |        .#..
        |        #...
        |        ....
        |...#.......#
        |........#...
        |..#....#....
        |..........#.
        |        ...#....
        |        .....#..
        |        .#......
        |        ......#.
        |
        |10R5L5R10L4R5L5            
        """.trimMargin()

    val puzzleInput = "day22/input.txt".loadResource()

    fun String.parseInstructions(): List<Instruction> = sequence<Instruction> {
        val st = StringTokenizer(this@parseInstructions, "LR", true)
        while (st.hasMoreTokens()) {
            yield(
                when (val token = st.nextToken().trim()) {
                    "R" -> Instruction.Right
                    "L" -> Instruction.Left
                    else -> Instruction.Forward(token.toInt())
                }
            )
        }
    }.toList()

    fun CharArray.wallPoints(y: Int): Set<Point> = toList().mapIndexedNotNull { x, cell -> if (cell == '#') Point(x, y) else null }.toSet()

    fun Bitmap.at(p: Point): Char? = if (p.x < 0 || p.y < 0 || p.y >= this.size || p.x >= this[p.y].size) null else this[p.y][p.x]

    fun Bitmap.walkPerimeter(initialPosition: Point): List<Location> = buildList {
        var position = initialPosition
        var heading = Direction.Right
        do {
            add(Location(position, heading))
            val forward = position.move(heading)
            val cell = at(forward)
            if (cell == null || cell == ' ') {
                heading += Direction.Right
            } else {
                val left = forward.move(heading + Direction.Left)
                val leftCell = at(left)
                if (leftCell == null || leftCell == ' ') {
                    position = forward
                } else {
                    position = left
                    heading += Direction.Left
                }
            }
        } while (position != initialPosition || heading != Direction.Right)
    }

    fun Board.wrap2D(): Board = copy(adjacent = perimeter.associate { location ->
        val p = location.position
        val d = location.direction + Direction.Left
        val wrapPosition = when (d) {
            Direction.Right -> Point(x = map[p.y].indexOfFirst { it != ' ' }, y = p.y)
            Direction.Bottom -> Point(x = p.x, y = map.indexOfFirst { p.x in it.indices && it[p.x] != ' ' })
            Direction.Left -> Point(x = map[p.y].indexOfLast { it != ' ' }, y = p.y)
            Direction.Top -> Point(x = p.x, y = map.indexOfLast { p.x in it.indices && it[p.x] != ' ' })
            else -> error("Unknown direction")
        }
        location.copy(direction = d) to Location(wrapPosition, d)
    })

    fun String.parseData(): Pair<Board, List<Instruction>> = this.split("\n\n").zipWithNext { m, i ->
        val map: Bitmap = m.split('\n').map { it.toCharArray() }.toTypedArray()
        val initialPosition = Point(map[0].indexOf('.'), 0)
        val perimeter = map.walkPerimeter(initialPosition)
        val walls = map.flatMapIndexed { y, line -> line.wallPoints(y) }.toSet()
        Board(map, initialPosition, perimeter, walls) to i.parseInstructions()
    }.first()

    fun Direction.toInt(): Int = when (this) {
        Direction.Right -> 0
        Direction.Bottom -> 1
        Direction.Left -> 2
        Direction.Top -> 3
        else -> error("Where am I?")
    }

    fun Board.show(location: Location) {
        println("==========")
        map.forEachIndexed { index, row ->
            println(
                if (index == location.position.y) {
                    row.copyOf().also {
                        it[location.position.x] = when (location.direction) {
                            Direction.Top -> '^'
                            Direction.Left -> '<'
                            Direction.Right -> '>'
                            Direction.Bottom -> 'v'
                            else -> '*'
                        }
                    }
                } else {
                    row
                }.joinToString("")
            )
        }
    }

    fun List<Instruction>.walk(board: Board): Int {
        return fold(Location(board.initialPosition, Direction.Right)) outer@{ location, instruction ->
            when (instruction) {
                is Instruction.Forward -> {
                    (1..instruction.count).fold(location) { location, _ ->
                        val next = board.adjacent[location] ?: location.copy(position = location.position.move(location.direction))
                        if (board.map.at(next.position) != '.') location else next
                    }
                }

                Instruction.Left -> location.copy(direction = location.direction + Direction.Left)
                Instruction.Right -> location.copy(direction = location.direction + Direction.Right)
            }
        }.let {
            1000 * (it.position.y + 1) + 4 * (it.position.x + 1) + it.direction.toInt()
        }
    }

    // Part 1
    fun Pair<Board, List<Instruction>>.part1(): Int = second.walk(first.wrap2D())

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 6032)
        println("Sample passed part 1")
    }

    with(puzzleInput.parseData()) {
        val result = part1()
        Assert.equals(result, 73346)
        println("Part 1 result: $result")
    }

    fun Board.wrapCube(): Board = copy(adjacent = buildMap {
        var sideLength = Int.MAX_VALUE
        var previousDirection = Direction.Right
        var directionCount = 0
        for ((_, direction) in perimeter) {
            if (direction == previousDirection) {
                directionCount++
            } else {
                sideLength = minOf(sideLength, directionCount)
                previousDirection = direction
                directionCount = 1
            }
        }
        sideLength = minOf(sideLength, directionCount)
        val unpairedEdges = MutableList(perimeter.size / sideLength) { i ->
            perimeter[i * sideLength].direction to perimeter.subList(i * sideLength, (i + 1) * sideLength)
        }
        while (unpairedEdges.isNotEmpty()) {
            var i = 0
            while (i < unpairedEdges.lastIndex) {
                val a = unpairedEdges[i]
                val b = unpairedEdges[i + 1]
                if (a.first + Direction.Left == b.first) {
                    unpairedEdges.subList(i, i + 2).clear()
                    for (j in i..unpairedEdges.lastIndex) {
                        val edge = unpairedEdges[j]
                        unpairedEdges[j] = edge.copy(first = edge.first + Direction.Left)
                    }
                    val edge1 = a.second
                    val edge2 = b.second
                    for (j in 0 until sideLength) {
                        val (position1, direction1) = edge1[j]
                        val (position2, direction2) = edge2[sideLength - j - 1]
                        this[Location(position1, direction1 + Direction.Left)] = Location(position2, direction2 + Direction.Right)
                        this[Location(position2, direction2 + Direction.Left)] = Location(position1, direction1 + Direction.Right)
                    }
                } else {
                    i++
                }
            }
        }
    })

    fun Pair<Board, List<Instruction>>.part2(): Int = second.walk(first.wrapCube())

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 5031)
        println("Sample passed part 2")
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}