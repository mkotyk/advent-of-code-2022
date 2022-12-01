fun main() {
    val sampleInput =
        """
            1000
            2000
            3000

            4000

            5000
            6000

            7000
            8000
            9000

            10000
        """.trimIndent()

    val puzzleInput = "day1/input.txt".loadResource()
    val dataSource = puzzleInput

    val inputData = dataSource.split("\n\n").mapIndexed { index, cals -> index + 1 to cals.split('\n').map { it.toInt() } }

    // Part 1
    val highestSingleCals = inputData.maxBy { it.second.sum() }
    println("Elf: $highestSingleCals -> ${highestSingleCals.second.sum()}")

    // Part 2
    val highestCals = inputData.sortedByDescending { it.second.sum() }.take(3)
    println("Elf: $highestCals -> ${highestCals.sumOf { it.second.sum() }}")
}