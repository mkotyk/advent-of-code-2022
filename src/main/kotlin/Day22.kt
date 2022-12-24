import java.util.*

sealed class Instruction {
    data class Move(val amount: Int) : Instruction()
    data class Turn(val direction: Direction) : Instruction()
}

operator fun Direction.plus(other: Direction): Direction {
    return when (other) {
        Direction.Left -> when (this) {
            Direction.Left -> Direction.Bottom
            Direction.Right -> Direction.Top
            Direction.Top -> Direction.Left
            Direction.Bottom -> Direction.Right
            else -> error("Unhandled directional change")
        }

        Direction.Right -> when (this) {
            Direction.Left -> Direction.Top
            Direction.Right -> Direction.Bottom
            Direction.Top -> Direction.Right
            Direction.Bottom -> Direction.Left
            else -> error("Unhandled directional change")
        }

        else -> error("Unhandled directional change")
    }
}

fun Direction.opposite(): Direction {
    return when (this) {
        Direction.Left -> Direction.Right
        Direction.Right -> Direction.Left
        Direction.Top -> Direction.Bottom
        Direction.Bottom -> Direction.Top
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

    fun String.parseData(): Pair<Bitmap, String> = this.split("\n\n").zipWithNext { m, i ->
        m.split('\n').map { it.toCharArray() }.toTypedArray() to i
    }.first()

    fun Bitmap.tileAt(p: Point): Char? = if (p.x < 0 || p.y < 0 || p.y >= size || p.x >= this[p.y].size) null else this[p.y][p.x]

    fun Bitmap.printMapAndPosition(point: Point, side: Int = 0) {
        val mapCopy = Array(this.size) { this[it].copyOf() }
        mapCopy[point.y][point.x] = '*'
        println("Side: $side")
        mapCopy.forEach { println(it) }
    }

    fun <T> List<T>.countUntil(predicate: (T) -> Boolean): Int {
        var count = 0
        while (!predicate(this[count])) count++
        return count
    }

    fun Bitmap.sliceY(x: Int) = map { if (x >= it.size) ' ' else it[x] }

    fun List<Char>.trim(): List<Char> = this.filter { it != ' ' }

    fun CharArray.countUntil(predicate: (Char) -> Boolean): Int {
        var count = 0
        while (!predicate(this[count])) count++
        return count
    }

    fun Bitmap.walk(instructions: List<Instruction>): Int {
        var position = Point(this[0].indexOf('.'), 0)
        var heading = Direction.Right

        instructions.forEach { instruction ->
            when (instruction) {
                is Instruction.Move -> {
                    var count = 0
                    while (count < instruction.amount) {
                        println("Pos: $position Heading: $heading")
                        var newPosition = position.move(heading)
                        while (true) {
                            when (tileAt(newPosition)) {
                                '.' -> {
                                    position = newPosition
                                    break
                                }

                                '#' -> {
                                    println("Bump wall")
                                    break
                                }

                                null, ' ' -> {
                                    println("Wrapped $newPosition")
                                    when (heading) {
                                        Direction.Bottom, Direction.Top -> {
                                            // Wrap Y
                                            val yslice = sliceY(newPosition.x)
                                            val leadingYSpaces = yslice.countUntil { it != ' ' }
                                            val newY = newPosition.y.mod(yslice.trim().size) + leadingYSpaces
                                            newPosition = Point(newPosition.x, newY)
                                        }

                                        Direction.Left, Direction.Right -> {
                                            // Wrap X
                                            val leadingXSpaces = this[newPosition.y].countUntil { it != ' ' }
                                            val newX = newPosition.x.mod(this[newPosition.y].size - leadingXSpaces) + leadingXSpaces
                                            newPosition = Point(newX, newPosition.y)
                                        }

                                        else -> error("Where am I?")
                                    }
                                    // Try checking new position again
                                }
                            }
                        }
                        count++
                    }
                }

                is Instruction.Turn -> {
                    heading += instruction.direction
                    println("Turn ${instruction.direction.name} now facing ${heading.name}")
                }
            }
        }

        return (position.y + 1) * 1000 + 4 * (position.x + 1) + when (heading) {
            Direction.Right -> 0
            Direction.Bottom -> 1
            Direction.Left -> 2
            Direction.Top -> 3
            else -> error("Where am I?")
        }
    }

    fun String.splitInstructions(): List<Instruction> = sequence<Instruction> {
        val st = StringTokenizer(this@splitInstructions, "LR", true)
        while (st.hasMoreTokens()) {
            val token = st.nextToken().trim()
            if (token == "R" || token == "L") {
                val direction = when (token) {
                    "R" -> Direction.Right
                    "L" -> Direction.Left
                    else -> error("Unknown direction")
                }
                yield(Instruction.Turn(direction))
            } else {
                yield(Instruction.Move(token.toInt()))
            }
        }
    }.toList()

    // Part 1
    fun Pair<Bitmap, String>.part1(): Int = first.walk(second.splitInstructions())

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 6032)
        println("Sample passed part 1")
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun Bitmap.toCube(): Array<Bitmap> {
        val cubeSize = size / 3
        var y = 0
        val cube = Array<Bitmap>(6) { Bitmap(cubeSize) { CharArray(cubeSize) } }
        // Top
        for (y in 0 until cubeSize) {
            cube[0][y] = this[y].toList().trim().toCharArray()
            cube[1][y] = this[y + cubeSize].toList().trim().take(cubeSize).toCharArray()
            cube[2][y] = this[y + cubeSize].toList().trim().drop(cubeSize).take(cubeSize).toCharArray()
            cube[3][y] = this[y + cubeSize].toList().trim().drop(cubeSize + cubeSize).take(cubeSize).toCharArray()
            cube[4][y] = this[y + cubeSize + cubeSize].toList().trim().take(cubeSize).toCharArray()
            cube[5][y] = this[y + cubeSize + cubeSize].toList().trim().drop(cubeSize).take(cubeSize).toCharArray()
        }
        return cube
    }

    fun cube(direction: Direction, prevSide: Int): Pair<Int, Direction> {
        val lookup = mapOf(
            Direction.Left to mapOf(
                1 to (3 to Direction.Right),
                2 to (6 to Direction.Left),
                3 to (2 to Direction.Top),
                4 to (3 to Direction.Top),
                5 to (3 to Direction.Left),
                6 to (5 to Direction.Top),
            ),
            Direction.Right to mapOf(
                1 to (6 to Direction.Bottom),
                2 to (3 to Direction.Top),
                3 to (4 to Direction.Top),
                4 to (6 to Direction.Left),
                5 to (6 to Direction.Top),
                6 to (1 to Direction.Bottom),
            ),
            Direction.Top to mapOf(
                1 to (2 to Direction.Bottom),
                2 to (5 to Direction.Top),
                3 to (1 to Direction.Left),
                4 to (1 to Direction.Top),
                5 to (4 to Direction.Top),
                6 to (4 to Direction.Right),
            ),
            Direction.Bottom to mapOf(
                1 to (4 to Direction.Top),
                2 to (5 to Direction.Bottom),
                3 to (5 to Direction.Right),
                4 to (5 to Direction.Top),
                5 to (2 to Direction.Bottom),
                6 to (2 to Direction.Right),
            )
        )
        return lookup[direction]!![prevSide + 1]?.let { it.first - 1 to it.second } ?: error("Wut")
    }

    fun Point.wrapCube(side: Int, cubeSize: Int): Pair<Point, Pair<Int, Direction>> {
        return when {
            x < 0 -> Point(cubeSize - 1, y) to cube(Direction.Left, side)
            x >= cubeSize -> Point(0, y) to cube(Direction.Right, side)
            y < 0 -> Point(x, cubeSize - 1) to cube(Direction.Top, side)
            y >= cubeSize -> Point(x, 0) to cube(Direction.Bottom, side)
            else -> this to (side to Direction.Top)
        }
    }

    fun Array<Bitmap>.walk(instructions: List<Instruction>): Int {
        var side = 0
        var position = Point(0, 0)
        var heading = Direction.Right
        val cubeSize = this[0].size

        instructions.forEach { instruction ->
            when (instruction) {
                is Instruction.Move -> {
                    var count = 0
                    while (count < instruction.amount) {
                        println("Pos: $position Heading: $heading")
                        this[side].printMapAndPosition(position, side)
                        val (newPosition, newSideAndDirection) = position.move(heading).wrapCube(side, cubeSize)
                        val newSide = newSideAndDirection.first
                        val newDirection = newSideAndDirection.second
                        heading = when (newDirection) {
                            Direction.Top -> heading // No change 0 deg
                            Direction.Left -> heading + Direction.Right
                            Direction.Right -> heading + Direction.Left
                            Direction.Bottom -> heading + Direction.Right + Direction.Right
                            else -> error("Unexpected change in direction")
                        }
                        while (true) {
                            when (val tile = this[newSide].tileAt(newPosition)) {
                                '.' -> {
                                    position = newPosition
                                    side = newSide
                                    break
                                }

                                '#' -> {
                                    println("Bump wall")
                                    break
                                }

                                else -> error("Unknown tile [$tile] Side: $newSide Position: $newPosition")
                            }
                        }
                        count++
                    }
                }

                is Instruction.Turn -> {
                    heading += instruction.direction
                    println("Turn ${instruction.direction.name} now facing ${heading.name}")
                }
            }
        }

        return (position.y + 1) * 1000 + 4 * (position.x + 1) + when (heading) {
            Direction.Right -> 0
            Direction.Bottom -> 1
            Direction.Left -> 2
            Direction.Top -> 3
            else -> error("Where am I?")
        }
    }

    fun Pair<Bitmap, String>.part2(): Int = first.toCube().walk(second.splitInstructions())

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 5031)
        println("Sample passed part 2")
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}