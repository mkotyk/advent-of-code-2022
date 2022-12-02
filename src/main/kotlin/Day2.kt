fun main() {
    val sampleInput = """
        A Y
        B X
        C Z
    """.trimIndent()
    val puzzleInput = "day2/input.txt".loadResource()
    fun String.parseInput() = this.split('\n').map { it.split(' ') }

    fun scoreRound(opponent: String, response: String): Int {
        val draw = 3
        val loss = 0
        val win = 6
        val rock = 1
        val paper = 2
        val scissors = 3

        // Nothing fancy, just a lookup
        return when (opponent) {
            "A" -> when (response) {
                "X" -> draw + rock
                "Y" -> win + paper
                "Z" -> loss + scissors
                else -> error("Unknown response gesture")
            }
            "B" -> when (response) {
                "X" -> loss + rock
                "Y" -> draw + paper
                "Z" -> win + scissors
                else -> error("Unknown response gesture")
            }
            "C" -> when (response) {
                "X" -> win + rock
                "Y" -> loss + paper
                "Z" -> draw + scissors
                else -> error("Unknown response gesture")
            }
            else -> error("Unknown opponent gesture")
        }
    }

    // Part 1
    fun List<List<String>>.part1(): Int = this.sumOf { scoreRound(it[0], it[1]) }
    Assert.equals(sampleInput.parseInput().part1(),  15)
    val score = puzzleInput.parseInput().part1()
    println("Part 1 final score is: $score")

    // Part 2
    fun translateResponse(opponent: String, outcome: String): String {
        val lose = "X"
        val draw = "Y"
        val win = "Z"
        val rock = "X"
        val paper = "Y"
        val scissors = "Z"

        // Simple lookup for readability
        return when (opponent) {
            "A" -> when (outcome) { // Rock
                lose -> scissors
                draw -> rock
                win -> paper
                else -> error("Unknown outcome")
            }
            "B" -> when (outcome) { // Paper
                lose -> rock
                draw -> paper
                win -> scissors
                else -> error("Unknown outcome")
            }
            "C" -> when (outcome) { // Scissors
                lose -> paper
                draw -> scissors
                win -> rock
                else -> error("Unknown outcome")
            }
            else -> error("Unknown opponent gesture")
        }
    }

    fun List<List<String>>.part2(): Int = this.sumOf { scoreRound(it[0], translateResponse(it[0], it[1])) }
    Assert.equals(sampleInput.parseInput().part2(), 12)
    val score2 = puzzleInput.parseInput().part2()
    println("Part 2 final score is: $score2")
}