typealias Identifier = String
typealias  Value = Long

sealed class Expression(val name: Identifier) {
    abstract fun interpret(lookup: Map<Identifier, Expression>): Value
    class Assignment(name: Identifier, val value: Value) : Expression(name) {
        override fun interpret(lookup: Map<Identifier, Expression>): Value = value
    }

    class Operation(name: Identifier, val lname: Identifier, val operand: String, val rname: Identifier) : Expression(name) {
        override fun interpret(lookup: Map<Identifier, Expression>): Value {
            val leftExpression = lookup[lname] ?: error("Unknown identifier named [$lname")
            val rightExpression = lookup[rname] ?: error("Unknown identifier named [$rname")
            return when (operand) {
                "+" -> leftExpression.interpret(lookup) + rightExpression.interpret(lookup)
                "-" -> leftExpression.interpret(lookup) - rightExpression.interpret(lookup)
                "*" -> leftExpression.interpret(lookup) * rightExpression.interpret(lookup)
                "/" -> leftExpression.interpret(lookup) / rightExpression.interpret(lookup)
                else -> error("Unknown operand [$operand]")
            }
        }
    }
}

fun main() {
    val sampleInput =
        """
            root: pppw + sjmn
            dbpl: 5
            cczh: sllz + lgvd
            zczc: 2
            ptdq: humn - dvpt
            dvpt: 3
            lfqf: 4
            humn: 5
            ljgn: 2
            sjmn: drzm * dbpl
            sllz: 4
            pppw: cczh / lfqf
            lgvd: ljgn * ptdq
            drzm: hmdt - zczc
            hmdt: 32
        """.trimIndent()

    val puzzleInput = "day21/input.txt".loadResource()

    fun String.parseData(): Map<Identifier, Expression> = this.split('\n').associate { line ->
        val (name, expr) = line.split(':')
        name to if (expr.trim().all { it.isDigit() }) {
            Expression.Assignment(name, expr.trim().toLong())
        } else {
            val (lname, operand, rname) = expr.trim().split(' ')
            Expression.Operation(name, lname, operand, rname)
        }
    }

    // Part 1
    fun Map<Identifier, Expression>.part1(): Value = this["root"]?.interpret(this) ?: error("No root expression")

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 152)
        println("Sample passed part 1")
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun Map<Identifier, Expression>.part2(): Value {
        val rootExpr = this["root"] ?: error("No root expression")
        if (rootExpr is Expression.Operation) {
            val leftExpression = this[rootExpr.lname] ?: error("Unable to find left name")
            val rightExpression = this[rootExpr.rname] ?: error("Unable to find right name")
            val clone = this.toMutableMap()
            val rvalue = rightExpression.interpret(clone) // Right is static

            val y1 = 0L
            clone["humn"] = Expression.Assignment("humn", y1)
            val l1 = leftExpression.interpret(clone)

            val y2 = 10000000000000000L
            clone["humn"] = Expression.Assignment("humn", y2)
            val l2 = leftExpression.interpret(clone)
            val m1 = (y2 - y1).toDouble() / (l2 - l1).toDouble()
            val b1 = y2 - (m1 * l2)
            val y = (m1 * rvalue + b1).toLong()

            // In the ballpark... narrow
            for (t in y - 1000..y + 1000) {
                clone["humn"] = Expression.Assignment("humn", t)
                if (leftExpression.interpret(clone) == rvalue) return t
            }
            error("Unable to verify")
        } else error("Expected root to be an operation")
    }

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 301)
        println("Sample passed part 2")
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}