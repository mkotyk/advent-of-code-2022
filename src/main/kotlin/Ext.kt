
fun String.loadResource(): String = object{}.javaClass.getResource(this)?.readText() ?: error("Failed to load resource [$this]")