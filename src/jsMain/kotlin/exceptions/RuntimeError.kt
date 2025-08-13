package exceptions

/**
 * Represents a runtime error during program execution.
 * Does not throw an exception.
 */
data class RuntimeError(
    val message: String,
    val line: Int,
    val column: Int
) {
    override fun toString(): String = "RuntimeError(line=$line, column=$column, message=$message)"
}
