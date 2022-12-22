fun main() {
    val sampleInput =
        """
        """.trimIndent()

    val puzzleInput = "dayXX/input.txt".loadResource()

    fun String.parseData(): List<String> = this.split('\n')

    // Part 1
    fun List<String>.part1(): Int =0

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 0)
        println("Sample passed part 1")
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun List<String>.part2(): Int  = 0

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 0)
        println("Sample passed part 2")
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}