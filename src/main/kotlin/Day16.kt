import kotlin.math.max

fun main() {
    val sampleInput =
        """
            Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
            Valve BB has flow rate=13; tunnels lead to valves CC, AA
            Valve CC has flow rate=2; tunnels lead to valves DD, BB
            Valve DD has flow rate=20; tunnels lead to valves CC, AA, EE
            Valve EE has flow rate=3; tunnels lead to valves FF, DD
            Valve FF has flow rate=0; tunnels lead to valves EE, GG
            Valve GG has flow rate=0; tunnels lead to valves FF, HH
            Valve HH has flow rate=22; tunnel leads to valve GG
            Valve II has flow rate=0; tunnels lead to valves AA, JJ
            Valve JJ has flow rate=21; tunnel leads to valve II
        """.trimIndent()

    val puzzleInput = "day16/input.txt".loadResource()

    data class Node(val name: String, val rate: Int, val edges: List<String>)

    fun String.parseData(): List<Node> = this.split('\n').map { line ->
        val expr = "^Valve (\\w+) has flow rate=(\\d+); tunnel(s?) lead(s?) to valve(s?) (.*?)$".toRegex()
        val result = expr.matchEntire(line) ?: error("Parse failure")
        val edges = result.groupValues.last().split(',').map { it.trim() }
        Node(name = result.groupValues[1], rate = result.groupValues[2].toInt(), edges)
    }

    fun List<Node>.createGraph(): Graph<Node, Edge<Node>> {
        val lookup = this.associateBy { it.name }
        return Graph(
            vertices = this.toSet(),
            edges = this.flatMap { from ->
                from.edges.map { toName -> Edge(from, lookup[toName] ?: error("Edge points to unknown vertex")) }
            }.toSet()
        )
    }

    data class CacheKey(val timeLeft: Int, val node: Node, val openValves: Set<Node>)

    fun depthFirstSearch(
        distances: Map<Node, Map<Node, Pair<Int, Node?>>>,
        timeLeft: Int,
        currentNode: Node,
        valesToOpen: Set<Node>,
        cache: MutableMap<CacheKey, Int> = mutableMapOf()
    ): Int {
        cache[CacheKey(timeLeft, currentNode, valesToOpen)]?.let { return it }
        return (valesToOpen.maxOfOrNull { target ->
            val timeLeftAfterValveOpened = timeLeft - ((distances[currentNode]!![target]?.first ?: error("No path")) + 1)
            if (timeLeftAfterValveOpened > 0)
                depthFirstSearch(distances, timeLeftAfterValveOpened, target, valesToOpen - target, cache) + target.rate * (timeLeftAfterValveOpened)
            else 0
        } ?: 0)
            .also { cache[CacheKey(timeLeft, currentNode, valesToOpen)] = it }
    }

    fun List<Node>.part1(minutes: Int = 30, workers: Int = 1): Int {
        val graph = createGraph()
        val distances: Map<Node, Map<Node, Pair<Int, Node?>>> = this.associateWith { graph.dijkstra(it) }
        val openValveSet = filter { it.rate > 0 }.toSet()
        val startNode = find { it.name == "AA" }!!
        return depthFirstSearch(distances, minutes, startNode, openValveSet)
    }

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 1651)
        println("Sample passed part 1")
    }

    with(puzzleInput.parseData()) {
        Assert.equals(part1(), 1595)
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun List<Node>.assignValves(mask: Long): Pair<Set<Node>, Set<Node>> {
        val a = mutableSetOf<Node>()
        val b = mutableSetOf<Node>()
        for (i in indices) {
            if ((1L shl i) and mask == 0L) {
                a.add(this[i])
            } else {
                b.add(this[i])
            }
        }
        return Pair(a,b)
    }

    fun List<Node>.part2(minutes: Int = 26): Int {
        val graph = createGraph()
        val distances: Map<Node, Map<Node, Pair<Int, Node?>>> = this.associateWith { graph.dijkstra(it) }
        val openValves = filter { it.rate > 0 }
        val myStart = openValves.sortedByDescending { it.rate }.mapIndexedNotNull { index, valve -> if (index % 2 == 0) valve else null }.toSet()
        val startNode = find { it.name == "AA" }!!
        val valveSpace = (1 shl openValves.size + 1) - 1L
        var m = 0
        for (count in 0..valveSpace) {
            val (myValveSet, elephantValveSet) = openValves.assignValves(count)
            if (count % 100L == 0L) println("Count: $count/$valveSpace  Max Value: $m")
            m = max(m, depthFirstSearch(distances, minutes, startNode, myValveSet) + depthFirstSearch(distances, minutes, startNode, elephantValveSet))
        }
        return m
    }

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 1707)
        println("Sample passed part 2")
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}