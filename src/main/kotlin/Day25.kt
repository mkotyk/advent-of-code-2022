typealias SNAFU = String

fun main() {
    val sampleInput =
        """
            1=-0-2
            12111
            2=0=
            21
            2=01
            111
            20012
            112
            1=-1=
            1-12
            12
            1=
            122
        """.trimIndent()

    val puzzleInput = "day25/input.txt".loadResource()

    fun String.parseData(): List<SNAFU> = this.split('\n')

    fun SNAFU.toLong(): Long = this.fold(0L) { acc, ch ->
        5 * acc  + when (ch) {
            '=' -> -2
            '-' -> -1
            '0' -> 0
            '1' -> 1
            '2' -> 2
            else -> error("Unknown character in base SNAFU $ch")
        }
    }

    fun Long.toSNAFU(): SNAFU = buildString {
        var v = this@toSNAFU
        while (v != 0L) {
            append("012=-"[v.mod(5)])
            v = (v + 2).floorDiv(5)
        }
    }.reversed()

    Assert.equals(1, "1".toLong())
    Assert.equals(2, "2".toLong())
    Assert.equals(3, "1=".toLong())
    Assert.equals(4, "1-".toLong())
    Assert.equals(5, "10".toLong())
    Assert.equals(6, "11".toLong())
    Assert.equals(7, "12".toLong())
    Assert.equals(8, "2=".toLong())
    Assert.equals(9, "2-".toLong())
    Assert.equals(10, "20".toLong())
    Assert.equals(15, "1=0".toLong())
    Assert.equals(20, "1-0".toLong())
    Assert.equals(2022, "1=11-2".toLong())
    Assert.equals(12345, "1-0---0".toLong())
    Assert.equals(314159265, "1121-1110-1=0".toLong())

    Assert.equals(1L.toSNAFU(), "1")
    Assert.equals(2L.toSNAFU(), "2")
    Assert.equals(3L.toSNAFU(), "1=")
    Assert.equals(4L.toSNAFU(), "1-")
    Assert.equals(5L.toSNAFU(), "10")
    Assert.equals(6L.toSNAFU(), "11")
    Assert.equals(7L.toSNAFU(), "12")
    Assert.equals(8L.toSNAFU(), "2=")
    Assert.equals(9L.toSNAFU(), "2-")
    Assert.equals(10L.toSNAFU(), "20")
    Assert.equals(15L.toSNAFU(), "1=0")
    Assert.equals(20L.toSNAFU(), "1-0")
    Assert.equals(2022L.toSNAFU(), "1=11-2")
    Assert.equals(12345L.toSNAFU(), "1-0---0")
    Assert.equals(314159265L.toSNAFU(), "1121-1110-1=0")

    // Part 1
    fun List<SNAFU>.part1(): Long = sumOf { it.toLong().toLong() }

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 4890)
        println("Sample passed part 1")
    }

    with(puzzleInput.parseData()) {
        val result = part1()
        println("Part 1 result: $result or ${result.toSNAFU()}")
    }

    // Part 2
    fun List<SNAFU>.part2(): Int = 0

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 0)
        println("Sample passed part 2")
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}