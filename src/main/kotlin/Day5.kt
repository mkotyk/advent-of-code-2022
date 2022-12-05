import java.util.*


typealias CrateStacks = List<Stack<Char>>

fun main() {
    val sampleInput =
        """
    [D]    
[N] [C]    
[Z] [M] [P]
 1   2   3 

move 1 from 2 to 1
move 3 from 1 to 3
move 2 from 2 to 1
move 1 from 1 to 2
"""
    val puzzleInput = "day5/input.txt".loadResource()

    data class Instruction(val count: Int, val from: Int, val to: Int)
    data class Input(val state: CrateStacks, val instructions: List<Instruction>)

    fun String.parseState(): CrateStacks {
        val lines = this.split('\n')
        val cols = lines.last().split(' ').maxOfOrNull { if (it.isNotBlank()) it.toInt() else 0 }
            ?: error("Unable to determine columns")
        val crateStacks = MutableList(cols) { Stack<Char>() }
        lines.reversed().drop(1).forEach { line ->
            for (index in 0 until cols) {
                val position = index * 4 + 1
                val crateType = if (position < line.length) line[position] else ' '
                if (crateType != ' ') {
                    crateStacks[index].push(crateType)
                }
            }
        }
        return crateStacks
    }

    fun String.parseInstructions(): List<Instruction> = this.split('\n').mapNotNull { line ->
        val parts = line.split(' ')
        if (parts.size != 6) {
            null
        } else {
            Assert.equals(parts[0], "move")
            Assert.equals(parts[2], "from")
            Assert.equals(parts[4], "to")

            Instruction(count = parts[1].toInt(), from = parts[3].toInt(), to = parts[5].toInt())
        }
    }

    fun String.parseData(): Input {
        val (state, instructions) = split("\n\n")
        return Input(state = state.parseState(), instructions.parseInstructions())
    }

    // Part 1
    fun Input.part1(): String {
        instructions.forEach { instruction ->
            for (i in 1..instruction.count) {
                state[instruction.to - 1].push(state[instruction.from - 1].pop())
            }
        }
        return state.fold("") { acc, column -> acc + column.peek() }
    }

    with(sampleInput.parseData()) {
        Assert.equals(state[0].peek(), 'N')
        Assert.equals(state[1].peek(), 'D')
        Assert.equals(state[2].peek(), 'P')
        Assert.equals(instructions[0].count, 1)
        Assert.equals(instructions[0].from, 2)
        Assert.equals(instructions[0].to, 1)
        Assert.equals(part1(), "CMZ")
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun <T> Stack<T>.popMany(count: Int): List<T> = (0 until count).fold(emptyList()) { acc, _ -> acc + this.pop() }
    fun <T> Stack<T>.pushMany(items: List<T>) {
        items.forEach { this.push(it) }
    }

    fun Input.part2(): String {
        instructions.forEach { instruction ->
            state[instruction.to - 1].pushMany(state[instruction.from - 1].popMany(instruction.count).reversed())
        }
        return state.fold("") { acc, column -> acc + column.peek() }
    }

    with(sampleInput.parseData()) {
        Assert.equals(part2(), "MCD")
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}