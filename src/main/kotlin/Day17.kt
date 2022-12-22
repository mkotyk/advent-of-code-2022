fun main() {
    val sampleInput = ">>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>"

    val puzzleInput = "day17/input.txt".loadResource()

    fun String.parseData(): String = this

    val rockTypes = listOf(
        intArrayOf(30),           // _
        intArrayOf(8, 28, 8),     // +
        intArrayOf(4, 4, 28),     // ⌟
        intArrayOf(16, 16, 16, 16),   // |
        intArrayOf(24, 24),       // .
    )

    fun IntArray.shiftLeft(i: Int): IntArray = this.map { it shl i }.toIntArray()
    fun IntArray.shiftRight(i: Int): IntArray = this.map { it shr i }.toIntArray()
    fun IntArray.canShiftRight(caveAt: (Int) -> Int): Boolean = this.foldIndexed(true) { x, a, r -> a && (r and 1) == 0 && ((r shr 1) and caveAt(x)) == 0 }
    fun IntArray.canShiftLeft(caveAt: (Int) -> Int): Boolean = this.foldIndexed(true) { x, a, r -> a && (r and 64) == 0 && ((r shl 1) and caveAt(x)) == 0 }
    fun Int.printRow() {
        println("|" + (6 downTo 0).map { if (this and (1 shl it) == 0) '·' else '#' }.joinToString("") + "|")
    }

    fun IntArray.anyIndexed(predicate: (Int, Int) -> Boolean): Boolean {
        this.forEachIndexed { index, it -> if (predicate(index, it)) return@anyIndexed true }
        return false
    }

    data class Stats(val rockCount: Long, val top: Long, val rockIndex: Int, val jetIndex: Int)

    // Part 1
    fun String.part1(rockCount: Long = 2022): Long {
        val bufferSize = 127
        val cave = IntArray(bufferSize) { 0 }
        var jetIndex = 0
        var top = 0L
        val pattern = intArrayOf(124, 20, 20)
        val patternSeen = mutableListOf<Stats>()
        var currentRockCount = 0L

        while (currentRockCount < rockCount) {
            val rockIndex = (currentRockCount % rockTypes.size).toInt()
            val nextRock = rockTypes[rockIndex]

            // Reset blank lines
            while (top > 0 && cave[top.mod(bufferSize)] == 0) top--

            // Look for a known pattern (at least in this data set)
            if (cave[currentRockCount.mod(bufferSize)] == 120 && cave[(currentRockCount + 1).mod(bufferSize)] == 80 &&
                cave[(currentRockCount + 2).mod(bufferSize)] == 80 && cave[(currentRockCount + 3).mod(bufferSize)] == 64
            ) {
                if (patternSeen.isEmpty() || patternSeen.firstOrNull()?.let { it.rockIndex == rockIndex && it.jetIndex == jetIndex.mod(length) } == true) {
                    patternSeen.add(Stats(currentRockCount, top, rockIndex, jetIndex.mod(length)))
                    if (patternSeen.size > 2) {
                        val deltaRockCount = (patternSeen[1].rockCount - patternSeen[0].rockCount)
                        val deltaHeight = (patternSeen[1].top - patternSeen[0].top)
                        val iterationsToSkip = (rockCount - currentRockCount) / deltaRockCount
                        val skipHeight = iterationsToSkip * deltaHeight
                        val skipRocks = iterationsToSkip * deltaRockCount
                        top += skipHeight
                        currentRockCount += skipRocks
                    }
                }
            }

            // Add space at top for new rock
            val newTop = top + nextRock.size + 3
            for (x in top + 1..newTop) cave[x.mod(bufferSize)] = 0
            top = newTop

            // Position rock
            var rockDropping = nextRock.clone()

            // Drop rock
            for (y in 0..top) {
                // Shift rock
                val jet = this[jetIndex % this.length]
                jetIndex++
                rockDropping = when (jet) {
                    '>' -> if (rockDropping.canShiftRight { x -> cave[(top - (y + x)).mod(bufferSize)] }) rockDropping.shiftRight(1) else rockDropping
                    '<' -> if (rockDropping.canShiftLeft { x -> cave[(top - (y + x)).mod(bufferSize)] }) rockDropping.shiftLeft(1) else rockDropping
                    else -> rockDropping
                }

                if (top - (y + 1 + rockDropping.size) < 0 || rockDropping.anyIndexed { x, mask -> mask and cave[(top - (y + 1 + x)).mod(bufferSize)] != 0 }) {
                    // Floor or Collision would occur
                    rockDropping.forEachIndexed { x, mask -> cave[(top - (x + y)).mod(bufferSize)] = cave[(top - (x + y)).mod(bufferSize)] or mask }
                    break
                }
            }
            currentRockCount++
        }
        while (cave[top.mod(bufferSize)] == 0) top--
        return top
    }

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 3068)
        println("Sample passed part 1")
    }

    with(puzzleInput.parseData()) {
        val result = part1()
        Assert.equals(result, 3059)
        println("Part 1 result: $result")
    }

    // Part 2
    fun String.part2(): Long = part1(rockCount = 1000000000000)

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 1514285714288L)
        println("Sample passed part 2")
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}