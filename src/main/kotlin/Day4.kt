fun main() {
    val sampleInput =
        """
        2-4,6-8
        2-3,4-5
        5-7,7-9
        2-8,3-7
        6-6,4-6
        2-6,4-8
        """.trimIndent()

    val puzzleInput = "day4/input.txt".loadResource()
    fun String.parseRange(): IntRange {
        val (start, end) = this.split('-')
        return IntRange(start.toInt(), end.toInt())
    }

    fun String.parsePair(): Pair<IntRange, IntRange> {
        val (a, b) = this.split(',')
        return Pair(a.parseRange(), b.parseRange())
    }

    fun String.parseData() = this.split('\n').map { it.parsePair() }

    // Part 1
    fun IntRange.fullyContains(other: IntRange): Boolean {
        return (other.first >= this.first && other.last <= this.last)
    }

    fun List<Pair<IntRange, IntRange>>.part1(): Int =
        this.count { it.first.fullyContains(it.second) || it.second.fullyContains(it.first) }

    with(sampleInput.parseData()) {
        Assert.equals(this[0].first, IntRange(2, 4))
        Assert.equals(this[0].second, IntRange(6, 8))
        Assert.equals(part1(), 2)
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun IntRange.overlaps(other: IntRange): Boolean {
        return (other.first >= this.first && other.first <= this.last) ||
                (other.last >= this.first && other.last <= this.last)
    }

    fun List<Pair<IntRange, IntRange>>.part2(): Int =
        this.count { it.first.overlaps(it.second) || it.second.overlaps(it.first) }

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 4)
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}