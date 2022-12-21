import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

enum class Material {
    Ore,
    Clay,
    Obsidian,
    Geode
}

fun main() {
    val sampleInput =
        """
            Blueprint 1: Each ore robot costs 4 ore. Each clay robot costs 2 ore. Each obsidian robot costs 3 ore and 14 clay. Each geode robot costs 2 ore and 7 obsidian.
            Blueprint 2: Each ore robot costs 2 ore. Each clay robot costs 3 ore. Each obsidian robot costs 3 ore and 8 clay. Each geode robot costs 3 ore and 12 obsidian.
        """.trimIndent()

    val puzzleInput = "day19/input.txt".loadResource()

    data class Blueprint(val id: Int, val costs: Map<Material, Map<Material, Int>>)

    fun String.parseData(): List<Blueprint> = this.split('\n').map { line ->
        val expr =
            "Blueprint (\\d+): Each ore robot costs (\\d+) ore. Each clay robot costs (\\d+) ore. Each obsidian robot costs (\\d+) ore and (\\d+) clay. Each geode robot costs (\\d+) ore and (\\d+) obsidian.".toRegex()
        val result = expr.matchEntire(line) ?: error("Unable to parse")
        Blueprint(
            id = result.groupValues[1].toInt(),
            costs = mapOf(
                Material.Ore to mapOf(Material.Ore to result.groupValues[2].toInt()),
                Material.Clay to mapOf(Material.Ore to result.groupValues[3].toInt()),
                Material.Obsidian to mapOf(Material.Ore to result.groupValues[4].toInt(), Material.Clay to result.groupValues[5].toInt()),
                Material.Geode to mapOf(Material.Ore to result.groupValues[6].toInt(), Material.Obsidian to result.groupValues[7].toInt()),
            )
        )
    }

    data class State(val robots: Map<Material, Int>, val resources: Map<Material, Int>, val time: Int) {
        val estimate: Int = (resources[Material.Geode] ?: 0) + (robots[Material.Geode] ?: 0) * time
    }

    fun potential(blueprintCosts: Map<Material, Map<Material, Int>>, state: State): Int {
        val robots = blueprintCosts.keys.associateWithTo(mutableMapOf()) { 0 }
        val resources = state.resources.toMutableMap()
        repeat(state.time) {
            robots.entries.forEach { (robot, count) ->
                resources[robot] = (resources[robot] ?: 0) + (state.robots[robot] ?: 0) + count
            }
            robots.entries.forEach {
                if (blueprintCosts[it.key]!!.all { (type, cost) -> (resources[type] ?: 0) >= cost * (it.value + 1) }) {
                    it.setValue(it.value + 1)
                }
            }
        }
        return resources[Material.Geode] ?: 0
    }

    fun geodes(blueprint: Blueprint, minutes: Int): Int {
        val maxValues = buildMap {
            for (robotCost in blueprint.costs) {
                for ((material, cost) in robotCost.value) this[material] = maxOf(this[material] ?: 0, cost)
            }
        }

        var best = 0
        val queue = mutableListOf(State(mapOf(Material.Ore to 1), emptyMap(), minutes))
        while (queue.isNotEmpty()) {
            val state = queue.removeLast()
            if (potential(blueprint.costs, state) < best) continue
            if (state.estimate > best) best = state.estimate
            for ((robot, costs) in blueprint.costs) {
                val maxValue = maxValues[robot]
                if (maxValue != null && (state.robots[robot] ?: 0) >= maxValue) continue
                val delta = costs.keys.maxOf { type ->
                    val demand = (costs[type] ?: 0) - (state.resources[type] ?: 0)
                    if (demand <= 0) {
                        0
                    } else {
                        val supply = state.robots[type] ?: 0
                        if (supply <= 0) Int.MAX_VALUE else (demand + supply - 1) / supply
                    }
                }

                if (delta < state.time) {
                    val robots = state.robots + (robot to (state.robots[robot] ?: 0) + 1)
                    val resources = buildMap {
                        putAll(state.resources)
                        for ((type, cost) in costs) this[type] = (this[type] ?: 0) - cost
                        for ((type, count) in state.robots) this[type] = (this[type] ?: 0) + count * (delta + 1)
                    }
                    queue.add(State(robots, resources, state.time - delta - 1))
                }
            }
        }
        return best
    }

    suspend fun List<Blueprint>.tryBlueprint(minutes: Int): Map<Int, Int> = withContext(Dispatchers.Default) {
        map { blueprint ->
            async {
                blueprint.id to geodes(blueprint, minutes).also { println("${blueprint.id} -> $it") }
            }
        }.associate { it.await() }
    }

    // Part 1
    suspend fun List<Blueprint>.part1(minutes: Int = 24): Int = tryBlueprint(24).entries.sumOf { it.key * it.value }

    runBlocking {
        with(sampleInput.parseData()) {
            Assert.equals(part1(), 33)
            println("Sample passed part 1")
        }

        with(puzzleInput.parseData()) {
            println("Part 1 result: ${part1()}")
        }
    }

    // Part 2
    suspend fun List<Blueprint>.part2(): Int = take(3).tryBlueprint(32).entries.fold(1) { acc, pair -> acc * pair.value }

    runBlocking {
        with(sampleInput.parseData()) {
            Assert.equals(part2(), 62 * 56)
            println("Sample passed part 2")
        }

        with(puzzleInput.parseData()) {
            println("Part 2 result: ${part2()}")
        }
    }
}