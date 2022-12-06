fun main() {
    val sampleInput =
        """
            mjqjpqmgbljsphdztnvjfqwrcgsmlb    
            bvwbjplbgvbhsrlpgdmjqwftvncz
            nppdvjthqldpwncqszvftbrmjlhg
            nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg
            zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw
        """.trimIndent()

    val puzzleInput = "day6/input.txt".loadResource()

    fun String.parseData(): String = this.trim()

    // Part 1
    fun String.allDifferent(): Boolean =
        this.filterIndexed { index, c -> this.substring(index + 1).contains(c) }.isBlank()

    fun String.scanUnique(windowSize: Int): Int {
        for (index in 0 until length - windowSize) {
            val window = this.substring(index, index + windowSize)
            if (window.allDifferent()) return index + windowSize
        }
        return -1
    }

    fun String.part1(): Int = scanUnique(4)

    with(sampleInput.split('\n').map { it.parseData() }) {
        Assert.equals(this[0].part1(), 7)
        Assert.equals(this[1].part1(), 5)
        Assert.equals(this[2].part1(), 6)
        Assert.equals(this[3].part1(), 10)
        Assert.equals(this[4].part1(), 11)
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun String.part2(): Int = scanUnique(14)

    with(sampleInput.split('\n').map { it.parseData() }) {
        Assert.equals(this[0].part2(), 19)
        Assert.equals(this[1].part2(), 23)
        Assert.equals(this[2].part2(), 23)
        Assert.equals(this[3].part2(), 29)
        Assert.equals(this[4].part2(), 26)
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}