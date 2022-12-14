import kotlin.math.max

sealed class Packet {
    data class Element(val value: Int) : Packet() {
        override fun toString(): String = value.toString()
    }

    data class Sequence(val values: List<Packet>) : Packet() {
        override fun toString(): String = "[" + values.joinToString(",") + "]"
    }
}

fun main() {
    val sampleInput =
        """
            [1,1,3,1,1]
            [1,1,5,1,1]

            [[1],[2,3,4]]
            [[1],4]

            [9]
            [[8,7,6]]

            [[4,4],4,4]
            [[4,4],4,4,4]

            [7,7,7,7]
            [7,7,7]

            []
            [3]

            [[[]]]
            [[]]

            [1,[2,[3,[4,[5,6,7]]]],8,9]
            [1,[2,[3,[4,[5,6,0]]]],8,9]
        """.trimIndent()

    val puzzleInput = "day13/input.txt".loadResource()

    data class PacketPair(val left: Packet, val right: Packet)

    fun String.parts(a: Int, b: Int): List<String> {
        return (if (a > 0) listOf(substring(0, a)) else emptyList()) +
                listOf(substring(a + 1, b)) +
                (if (b + 1 < length) listOf(substring(b + 1)) else emptyList())
    }

    fun String.unwrapArray(): List<String> {
        val cleaned = this.drop(1).dropLast(1)
        var level = 0
        var start = 0
        return sequence {
            for (x in cleaned.indices) {
                when (cleaned[x]) {
                    '[' -> level++
                    ']' -> level--
                    ',' -> {
                        if (level == 0) {
                            yield(cleaned.substring(start, x))
                            start = x + 1
                        }
                    }
                }
            }
            yield(cleaned.substring(start))
        }.toList().filter { it.isNotBlank() }
    }

    fun String.parsePacket(): Packet? {
        return if (startsWith('[') && endsWith(']')) {
            Packet.Sequence(unwrapArray().mapNotNull { it.parsePacket() })
        } else {
            Packet.Element(toInt())
        }
    }

    fun String.parseData(): List<PacketPair> = this.split("\n\n").map {
        val (left, right) = it.split('\n')
        PacketPair(left.parsePacket()!!, right.parsePacket()!!)
    }

    fun Packet.compare(other: Packet, indent: Int = 0): Int {
        repeat(indent) { print("  ") }
        println(" - Compare $this vs $other")

        return when {
            this is Packet.Sequence && other is Packet.Sequence -> {
                var cmp = 0
                for (x in 0 until max(this.values.size, other.values.size)) {
                    if (x >= this.values.size) {
                        cmp = -1
                        break
                    }
                    if (x >= other.values.size) {
                        cmp = 1
                        break
                    }
                    cmp = this.values[x].compare(other.values[x], indent + 1)
                    if (cmp != 0) break
                }
                cmp
            }
            this is Packet.Sequence && other is Packet.Element -> this.compare(Packet.Sequence(listOf(other)), indent + 1)
            this is Packet.Element && other is Packet.Sequence -> Packet.Sequence(listOf(this)).compare(other, indent + 1)
            this is Packet.Element && other is Packet.Element -> this.value - other.value
            else -> error("Unknown combo")
        }
    }

    fun PacketPair.isCorrectOrder(): Boolean = left.compare(right) < 0

    // Part 1
    fun List<PacketPair>.part1(): Int = mapIndexed { index, value -> index + 1 to value }
        .filter { it.second.isCorrectOrder() }
        .sumOf { it.first }

    with(sampleInput.parseData()) {
        Assert.equals(this[0].isCorrectOrder(), true)
        Assert.equals(this[1].isCorrectOrder(), true)
        Assert.equals(this[2].isCorrectOrder(), false)
        Assert.equals(this[3].isCorrectOrder(), true)
        Assert.equals(this[4].isCorrectOrder(), false)
        Assert.equals(this[5].isCorrectOrder(), true)
        Assert.equals(this[6].isCorrectOrder(), false)
        Assert.equals(this[7].isCorrectOrder(), false)
        Assert.equals(part1(), 13)
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    val dividerPackets = listOf(
        Packet.Sequence(listOf(Packet.Sequence(listOf(Packet.Element(2))))),
        Packet.Sequence(listOf(Packet.Sequence(listOf(Packet.Element(6)))))
    )

    fun Packet.firstValue(i: Int = 0): Int = when (this) {
        is Packet.Sequence -> if (this.values.isEmpty()) i else this.values.first().firstValue(i + 1)
        is Packet.Element -> this.value * 100
    }

    // Part 2
    fun List<PacketPair>.part2(): Int {
        val sortedSignalList = (flatMap { listOf(it.left, it.right) } + dividerPackets).sortedWith { a, b -> a.compare(b) }
        return dividerPackets.fold(1) { acc, div -> acc * (sortedSignalList.indexOf(div) + 1) }
    }

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 140)
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}