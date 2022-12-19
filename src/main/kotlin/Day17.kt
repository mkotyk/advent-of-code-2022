val Int.MEG: Int get() = this * 1024 * 1024

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

    // Part 1
    fun String.part1(rockCount: Long = 2022, debug: Int = 0): Long {
        val bufferSize = 1.MEG
        val cave = IntArray(bufferSize) { 0 }
        var jetIndex = 0
        var top = 0

        fun cyc(i: Int): Int = (if (i < 0) bufferSize - i else i) % bufferSize
        for (c in 0 until rockCount) {
            if (c % rockTypes.size == 0L && jetIndex % this.length == 0) {
                println("The cycle begins again? $c $top")
            }
            val nextRock = rockTypes[(c % rockTypes.size).toInt()]

            if (c % 1000000L == 0L) println("${c * 100.0 / rockCount}% = $top")

            // Keep blank lines at top of cave
            while (top > 0 && cave[cyc(top)] == 0) top--
            val newTop = top + nextRock.size + 3
            for (x in top + 1..newTop) cave[cyc(x)] = 0
            top = newTop

            // Position rock
            var rockDropping = nextRock.clone()

            // Drop rock
            for (y in 0..top) {
                // Shift rock
                val jet = this[jetIndex % this.length]
                jetIndex++
                rockDropping = when (jet) {
                    '>' -> if (rockDropping.canShiftRight { x -> cave[cyc(top - (y + x))] }) rockDropping.shiftRight(1) else rockDropping
                    '<' -> if (rockDropping.canShiftLeft { x -> cave[cyc(top - (y + x))] }) rockDropping.shiftLeft(1) else rockDropping
                    else -> rockDropping
                }

                if (top - (y + 1 + rockDropping.size) < 0 || rockDropping.anyIndexed { x, mask -> mask and cave[cyc(top - (y + 1 + x))] != 0 }) {
                    // Floor or Collision would occur
                    rockDropping.forEachIndexed { x, mask -> cave[cyc(top - (x + y))] = cave[cyc(top - (x + y))] or mask }
                    break
                }
            }
        }
        return top - 3L
    }

    with(sampleInput.parseData()) {
        Assert.equals(part1(debug = 0), 3068)
        println("Sample passed part 1")
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun String.part2(): Long = part1(1000000000000L)

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 1514285714288L)
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}