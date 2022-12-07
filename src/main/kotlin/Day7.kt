sealed class FsNode {
    data class File(val name: String, val size: Long) : FsNode()
    data class Directory(
        val name: String,
        val parent: Directory? = null,
        val children: MutableList<FsNode> = mutableListOf()
    ) : FsNode() {
        fun add(node: FsNode) = children.add(node)
    }
}

fun main() {
    val sampleInput = """
        ${'$'} cd /
        ${'$'} ls
        dir a
        14848514 b.txt
        8504156 c.dat
        dir d
        ${'$'} cd a
        ${'$'} ls
        dir e
        29116 f
        2557 g
        62596 h.lst
        ${'$'} cd e
        ${'$'} ls
        584 i
        ${'$'} cd ..
        ${'$'} cd ..
        ${'$'} cd d
        ${'$'} ls
        4060174 j
        8033020 d.log
        5626152 d.ext
        7214296 k
    """.trimIndent()

    val puzzleInput = "day7/input.txt".loadResource()

    fun String.parseData(): List<String> = this.split('\n')

    fun List<String>.buildFsTree(): FsNode.Directory {
        val root = FsNode.Directory("/")
        var cd = root

        this.forEach { line ->
            when {
                line.startsWith("$ cd") -> {
                    val path = line.substring(5).trim()
                    cd = when (path) {
                        "/" -> root
                        ".." -> cd.parent ?: root
                        else -> cd.children.firstOrNull { it is FsNode.Directory && it.name == path } as FsNode.Directory?
                            ?: error("Unknown directory [$path]")
                    }
                }
                line.startsWith("$ ls") -> {} // noop
                else -> {
                    val (a, name) = line.split(' ')
                    cd.add(
                        if (a == "dir") {
                            FsNode.Directory(name = name, parent = cd)
                        } else {
                            FsNode.File(name = name, size = a.toLong())
                        }
                    )
                }
            }
        }
        return root
    }

    fun FsNode.Directory.diskUsed(): Long = children.sumOf { node ->
        when (node) {
            is FsNode.Directory -> node.diskUsed()
            is FsNode.File -> node.size
        }
    }

    fun FsNode.Directory.filterRecursive(predicate: (FsNode) -> Boolean): List<FsNode> = sequence {
        children.forEach {
            if (predicate(it)) yield(it)
            if (it is FsNode.Directory) yieldAll(it.filterRecursive(predicate))
        }
    }.toList()

    // Part 1
    fun FsNode.Directory.part1(): Long = filterRecursive { it is FsNode.Directory }.sumOf {
        if (it is FsNode.Directory) {
            val size = it.diskUsed()
            if (size <= 100000L) size else 0L
        } else 0L
    }

    with(sampleInput.parseData().buildFsTree()) {
        Assert.equals(part1(), 95437L)
    }

    with(puzzleInput.parseData().buildFsTree()) {
        println("Part 1 result: ${part1()}")
    }

    // Part 2
    fun FsNode.Directory.part2(targetSize: Long): Long = filterRecursive { it is FsNode.Directory }.mapNotNull {
        if (it is FsNode.Directory) {
            val size = it.diskUsed()
            if (size >= targetSize) {
                it to size
            } else {
                null
            }
        } else {
            null
        }
    }.minBy { it.second }.second

    with(sampleInput.parseData().buildFsTree()) {
        val free = 70000000 - diskUsed()
        val targetSize = 30000000 - free
        Assert.equals(part2(targetSize), 24933642)
    }

    with(puzzleInput.parseData().buildFsTree()) {
        val free = 70000000 - diskUsed()
        val targetSize = 30000000 - free
        println("Part 2 result: ${part2(targetSize)}")
    }
}