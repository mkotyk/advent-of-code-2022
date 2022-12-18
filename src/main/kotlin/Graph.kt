class Edge<V>(val from: V, val to: V)
class Graph<V, E : Edge<V>>(val vertices: Set<V>, val edges: Set<E>) {
    fun dijkstra(source: V): Map<V, Pair<Int, V?>> {
        val dist = mutableMapOf<V, Int>()
        val prev = mutableMapOf<V, V?>()
        val Q = mutableSetOf<V>()

        vertices.forEach {
            dist[it] = Int.MAX_VALUE
            prev[it] = null
            Q.add(it)
        }
        dist[source] = 0

        while (Q.isNotEmpty()) {
            val u = Q.minBy { dist[it]!! }
            Q.remove(u)

            edges.filter { it.from == u && Q.contains(it.to) }.map { it.to }.forEach { v ->
                val alt = dist[u]!! + 1
                if (alt < dist[v]!!) {
                    dist[v] = alt
                    prev[v] = u
                }
            }
        }
        return dist.entries.associate { (k, v) -> k to Pair(v, prev[k]) }
    }
}