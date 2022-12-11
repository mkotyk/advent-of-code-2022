import java.util.*

data class Monkey(
    val id: Int,
    val startingItems: Queue<Long>,
    val operationExpression: String,
    val testDivisor: Int,
    val trueTestMonkey: Int,
    val falseTestMonkey: Int,
    var inspectCount: Int = 0
) {
    fun eval(inValue: Long): Long {
        inspectCount++
        val expression = "new = old ([+*]) (old|\\d+)".toRegex()
        val result = expression.matchEntire(operationExpression) ?: error("Operation is invalid [$operationExpression]")
        val rightVal = when (result.groupValues[2]) {
            "old" -> inValue
            else -> result.groupValues[2].toLong()
        }

        return when (result.groupValues[1]) {
            "*" -> inValue * rightVal
            "+" -> inValue + rightVal
            else -> error("Unknown operand")
        }
    }
}

fun main() {
    val sampleInput =
        """
        Monkey 0:
          Starting items: 79, 98
          Operation: new = old * 19
          Test: divisible by 23
            If true: throw to monkey 2
            If false: throw to monkey 3
        
        Monkey 1:
          Starting items: 54, 65, 75, 74
          Operation: new = old + 6
          Test: divisible by 19
            If true: throw to monkey 2
            If false: throw to monkey 0
        
        Monkey 2:
          Starting items: 79, 60, 97
          Operation: new = old * old
          Test: divisible by 13
            If true: throw to monkey 1
            If false: throw to monkey 3
        
        Monkey 3:
          Starting items: 74
          Operation: new = old + 3
          Test: divisible by 17
            If true: throw to monkey 0
            If false: throw to monkey 1
        """.trimIndent()

    val puzzleInput = "day11/input.txt".loadResource()

    fun String.parseMonkey(): Monkey {
        val expression =
            """
                |Monkey (\d+):
                |\s{2}Starting items: (.*?)
                |\s{2}Operation: (.*?)
                |\s{2}Test: divisible by (\d+)
                |\s{4}If true: throw to monkey (\d+)
                |\s{4}If false: throw to monkey (\d+)
            """.trimMargin().toRegex()
        val result = expression.matchEntire(this) ?: error("No result")
        return Monkey(
            id = result.groupValues[1].toInt(),
            startingItems = LinkedList<Long>(result.groupValues[2].split(", ").map { it.toLong() }),
            operationExpression = result.groupValues[3],
            testDivisor = result.groupValues[4].toInt(),
            trueTestMonkey = result.groupValues[5].toInt(),
            falseTestMonkey = result.groupValues[6].toInt()
        )
    }

    fun String.parseData(): List<Monkey> = this.split("\n\n").map { it.parseMonkey() }

    fun <T> Queue<T>.popEach(block: (T) -> Unit) {
        while (!this.isEmpty()) block(this.remove())
    }

    // Part 1
    fun List<Monkey>.part1(): Int {
        val rounds = 20
        for (x in 1..rounds) {
            this.forEach { monkey ->
                monkey.startingItems.popEach { item ->
                    val newItemWorry = monkey.eval(item) / 3L
                    val throwToMonkey = if (newItemWorry % monkey.testDivisor == 0L)
                        monkey.trueTestMonkey else monkey.falseTestMonkey
                    this[throwToMonkey].startingItems.add(newItemWorry)
                }
            }
        }

        return this.sortedByDescending { it.inspectCount }.take(2).fold(1) { acc, monkey -> acc * monkey.inspectCount }
    }

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 10605)
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun List<Monkey>.part2(): Long {
        val rounds = 10000
        val factor = this.fold(1) { acc, monkey -> acc * monkey.testDivisor }
        for (x in 1..rounds) {
            this.forEach { monkey ->
                monkey.startingItems.popEach { item ->
                    val newItemWorry = monkey.eval(item)
                    val throwToMonkey = if (newItemWorry % monkey.testDivisor == 0L)
                        monkey.trueTestMonkey else monkey.falseTestMonkey
                    this[throwToMonkey].startingItems.add(newItemWorry % factor)
                }
            }
        }
        return this.sortedByDescending { it.inspectCount }.take(2).fold(1) { acc, monkey -> acc * monkey.inspectCount }
    }

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 2713310158)
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}