package exceptions

class SyntaxError(
    message: String,
    val line: Int? = null,
    val column: Int? = null
) : Exception(buildMessage(message, line, column)) {
    companion object {
        private fun buildMessage(
            message: String,
            line: Int?,
            column: Int?
        ): String =
            when {
                line != null && column != null -> "Syntax error at line $line, column $column: $message"
                line != null -> "Syntax error at line $line: $message"
                else -> "Syntax error: $message"
            }
    }
}
