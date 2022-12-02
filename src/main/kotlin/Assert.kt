object Assert {
    fun <T> equals(actual:T, expected:T) {
        if (actual != expected) error("Expected [$expected] but got [$actual]")
    }
}