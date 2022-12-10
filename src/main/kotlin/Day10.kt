import kotlin.math.abs

fun main() {
    val sampleInput =
        """
            addx 15
            addx -11
            addx 6
            addx -3
            addx 5
            addx -1
            addx -8
            addx 13
            addx 4
            noop
            addx -1
            addx 5
            addx -1
            addx 5
            addx -1
            addx 5
            addx -1
            addx 5
            addx -1
            addx -35
            addx 1
            addx 24
            addx -19
            addx 1
            addx 16
            addx -11
            noop
            noop
            addx 21
            addx -15
            noop
            noop
            addx -3
            addx 9
            addx 1
            addx -3
            addx 8
            addx 1
            addx 5
            noop
            noop
            noop
            noop
            noop
            addx -36
            noop
            addx 1
            addx 7
            noop
            noop
            noop
            addx 2
            addx 6
            noop
            noop
            noop
            noop
            noop
            addx 1
            noop
            noop
            addx 7
            addx 1
            noop
            addx -13
            addx 13
            addx 7
            noop
            addx 1
            addx -33
            noop
            noop
            noop
            addx 2
            noop
            noop
            noop
            addx 8
            noop
            addx -1
            addx 2
            addx 1
            noop
            addx 17
            addx -9
            addx 1
            addx 1
            addx -3
            addx 11
            noop
            noop
            addx 1
            noop
            addx 1
            noop
            noop
            addx -13
            addx -19
            addx 1
            addx 3
            addx 26
            addx -30
            addx 12
            addx -1
            addx 3
            addx 1
            noop
            noop
            noop
            addx -9
            addx 18
            addx 1
            addx 2
            noop
            noop
            addx 9
            noop
            noop
            noop
            addx -1
            addx 2
            addx -37
            addx 1
            addx 3
            noop
            addx 15
            addx -21
            addx 22
            addx -6
            addx 1
            noop
            addx 2
            addx 1
            noop
            addx -10
            noop
            noop
            addx 20
            addx 1
            addx 2
            addx 2
            addx -6
            addx -11
            noop
            noop
            noop
        """.trimIndent()
    val puzzleInput = "day10/input.txt".loadResource()

    fun String.parseData(): List<String> = this.split('\n')

    data class Registers(val X: Int = 1, val PC: Int = 1)

    fun Registers.processInstruction(instruction: String): List<Registers> {
        return when {
            instruction.startsWith("noop") -> listOf(this.copy(PC = PC + 1))
            instruction.startsWith("addx") -> listOf(
                this.copy(PC = PC + 1),
                this.copy(X = X + instruction.substring(5).toInt(), PC = PC + 2)
            )
            else -> error("Unknown opcode")
        }
    }

    // Part 1
    fun List<String>.part1(): Int {
        var registers = Registers()
        val output = this.flatMap { instruction ->
            val result = registers.processInstruction(instruction)
            registers = result.last()
            result
        }

        return output.filter { it.PC % 40 == 20 }.sumOf { it.X * it.PC }
    }

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 13140)
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun Registers.isSpriteVisible(): Boolean {
        val h = (PC - 1 )% 40
        return h == X - 1 || h == X || h == X + 1
    }

    fun List<String>.part2(): String {
        var registers = Registers()
        val output = listOf(registers) + this.flatMap { instruction ->
            val result = registers.processInstruction(instruction)
            registers = result.last()
            result
        }.dropLast(1)

        return output.joinToString("") {
            val pixel = if (it.isSpriteVisible()) '#' else '.'
            if (it.PC % 40 == 0) "$pixel\n" else "$pixel"
        }.trimEnd()
    }

    with(sampleInput.parseData()) {
        val expectedOutput =
            """
                ##..##..##..##..##..##..##..##..##..##..
                ###...###...###...###...###...###...###.
                ####....####....####....####....####....
                #####.....#####.....#####.....#####.....
                ######......######......######......####
                #######.......#######.......#######.....
            """.trimIndent()
        Assert.equals(part2(), expectedOutput)
    }

    // R Z E K E F H A
    with(puzzleInput.parseData()) {
        println("Part 2 result:\n${part2()}")
    }
}