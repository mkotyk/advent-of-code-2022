import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

fun String.loadResource(): String = object {}.javaClass.getResource(this)?.readText() ?: error("Failed to load resource [$this]")

fun <T> List<T>.permute(i: List<T> = emptyList()): List<List<T>> = if (isEmpty()) listOf(i) else
    fold(emptyList()) { acc, e -> acc + (this - e).permute(i + e) }

fun <T> List<T>.permute(i: List<T> = emptyList(), block: (List<T>) -> Unit): Unit =
    if (isEmpty()) block(i) else forEach { e -> (this - e).permute(i + e, block) }

suspend fun <T, R> permute(s: Set<T>, i: Set<T> = emptySet(), block: suspend (Set<T>) -> R): Set<R> = coroutineScope {
    withContext(Dispatchers.Default) { s.map { e -> permute((s - e), i + e, block) } }.flatten().toSet() + block(i)
}
