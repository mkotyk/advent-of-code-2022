typealias Ore = Int
typealias Clay = Int
typealias Obsidian = Int
typealias Geode = Int

fun main() {
    val sampleInput =
        """
            Blueprint 1: Each ore robot costs 4 ore. Each clay robot costs 2 ore. Each obsidian robot costs 3 ore and 14 clay. Each geode robot costs 2 ore and 7 obsidian.
            Blueprint 2: Each ore robot costs 2 ore. Each clay robot costs 3 ore. Each obsidian robot costs 3 ore and 8 clay. Each geode robot costs 3 ore and 12 obsidian.
        """.trimIndent()

    val puzzleInput = "day19/input.txt".loadResource()

    data class Blueprint(
        val id: Int,
        val oreRobotCost: Ore,
        val clayRobotCost: Ore,
        val obsidianRobotCost: Pair<Ore, Clay>,
        val geodeRobotCost: Pair<Ore, Obsidian>
    )

    fun String.parseData(): List<Blueprint> = this.split('\n').map { line ->
        val expr =
            "Blueprint (\\d+): Each ore robot costs (\\d+) ore. Each clay robot costs (\\d+) ore. Each obsidian robot costs (\\d+) ore and (\\d+) clay. Each geode robot costs (\\d+) ore and (\\d+) obsidian.".toRegex()
        val result = expr.matchEntire(line) ?: error("Unable to parse")
        Blueprint(
            id = result.groupValues[1].toInt(),
            oreRobotCost = result.groupValues[2].toInt(),
            clayRobotCost = result.groupValues[3].toInt(),
            obsidianRobotCost = Pair(result.groupValues[4].toInt(), result.groupValues[5].toInt()),
            geodeRobotCost = Pair(result.groupValues[6].toInt(), result.groupValues[7].toInt())
        )
    }

    fun simulate(blueprint: Blueprint, minutes: Int): Geode {
        var ore: Ore = 0
        var clay: Clay = 0
        var obsidian: Obsidian = 0
        var geodes: Geode = 0

        var oreCollectingRobots = 1
        var clayCollectingRobots = 0
        var obsidianCollectingRobots = 0
        var geodeCollectingRobots = 0

        for (min in 1..minutes) {
            ore += oreCollectingRobots
            clay += clayCollectingRobots
            obsidian += obsidianCollectingRobots
            geodes += geodeCollectingRobots

            if (ore >= blueprint.geodeRobotCost.first && obsidian >= blueprint.geodeRobotCost.second) {
                geodeCollectingRobots++
                ore -= blueprint.geodeRobotCost.first
                obsidian -= blueprint.geodeRobotCost.second
            }

            if (ore >= blueprint.obsidianRobotCost.first && clay >= blueprint.obsidianRobotCost.second) {
                obsidianCollectingRobots++
                ore -= blueprint.obsidianRobotCost.first
                clay -= blueprint.obsidianRobotCost.second
            }

            if (ore >= blueprint.clayRobotCost) {
                clayCollectingRobots++
                ore -= blueprint.clayRobotCost
            }
        }

        return geodes
    }

    // Part 1
    fun List<Blueprint>.part1(minutes: Int = 24): Int = this.sumOf { it.id * simulate(it, minutes) }

    with(sampleInput.parseData()) {
        Assert.equals(part1(), 33)
    }

    with(puzzleInput.parseData()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun List<Blueprint>.part2(): Int = 0

    with(sampleInput.parseData()) {
        Assert.equals(part2(), 0)
    }

    with(puzzleInput.parseData()) {
        println("Part 2 result: ${part2()}")
    }
}