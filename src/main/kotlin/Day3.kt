fun main() {
    val sampleInput =
        """
            vJrwpWtwJgWrhcsFMMfFFhFp
            jqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL
            PmmdzqPrVvPwwTWBwg
            wMqvLMZHhHMvwLHjbvcjnnSBnvTQFn
            ttgJtRGJQctTZtZT
            CrZsJsPPZsGzwwsLwLmpwMDw
        """.trimIndent()

    val puzzleInput = "day3/input.txt".loadResource()

    // Part 1
    fun String.parseData() = this.split('\n').map {
        val halfLength = it.length / 2
        Pair(it.substring(0, halfLength), it.substring(halfLength))
    }

    fun String.commonElements(other: String): List<Char> =
        this.filter { a -> other.any { b -> a == b } }.asIterable().distinct()

    fun Char.convertValue():Int = if(this.isUpperCase()) this.minus('A') + 27 else this.minus('a') + 1
    Assert.equals('a'.convertValue(), 1)
    Assert.equals('z'.convertValue(), 26)
    Assert.equals('A'.convertValue(), 27)
    Assert.equals('Z'.convertValue(), 52)

    fun List<Pair<String, String>>.part1(): Int = this.sumOf { it.first.commonElements(it.second).first().convertValue() }

    with(sampleInput.parseData()) {
        Assert.equals(this[0].first.commonElements(this[0].second).first(), 'p')
        Assert.equals(this[1].first.commonElements(this[1].second).first(), 'L')
        Assert.equals(this[2].first.commonElements(this[2].second).first(), 'P')
        Assert.equals(this[3].first.commonElements(this[3].second).first(), 'v')
        Assert.equals(this[4].first.commonElements(this[4].second).first(), 't')
        Assert.equals(this[5].first.commonElements(this[5].second).first(), 's')
        println("Sample Input result: $${part1()}")
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun Pair<String, String>.combined(): String = this.first + this.second

    fun List<Pair<String, String>>.part2(): Int {
        var sum = 0
        for(index in 0 until this.size step 3) {
            val commonItems1 = this[index].combined().commonElements(this[index + 1].combined()).joinToString("")
            val commonItems2 = this[index].combined().commonElements(this[index + 2].combined()).joinToString("")
            val common = commonItems1.commonElements(commonItems2)
            Assert.equals(common.size, 1)
            sum += common.first().convertValue()
        }
        return sum
    }

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 70)
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}