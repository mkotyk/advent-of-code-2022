fun <T> List<T>.cycle(): Sequence<T> = sequence {
    var index = 0
    while (true) {
        yield(get(index))
        index = (index + 1) % size
    }
}

fun main() {
    val sampleInput =
        """
            1
            2
            -3
            3
            -2
            0
            4
        """.trimIndent()

    val puzzleInput = "day20/input.txt".loadResource()

    fun String.parseData(): List<Long> = this.split('\n').map { it.toLong() }

    class Wrapper(val value: Long)

    fun List<Long>.mix(decryptionKey: Int = 1, times: Int = 1): List<Long> {
        val uniqueList = map { Wrapper(it * decryptionKey) } // Make each element an object, there-by making it unique
        val shuffleList = uniqueList.toMutableList()
        repeat(times) {
            uniqueList.forEach { wrapper ->
                val from = shuffleList.indexOf(wrapper)
                val item = shuffleList.removeAt(from)
                val to = (from + wrapper.value).mod(shuffleList.size)
                shuffleList.add(to, item)
            }
        }
        return shuffleList.map { it.value }
    }

    fun List<Long>.sumCoords() = cycle().dropWhile { it != 0L }.take(3001).filterIndexed { index, _ -> index % 1000 == 0 }.sum()

    // Part 1
    fun List<Long>.part1(): Long = mix().sumCoords()

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 3)
        println("Sample passed part 1")
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun List<Long>.part2(): Long = mix(decryptionKey = 811589153, times = 10).sumCoords()

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 1623178306)
        println("Sample passed part 2")
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}
