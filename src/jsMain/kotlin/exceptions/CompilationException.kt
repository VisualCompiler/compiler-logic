package exceptions

import CompilerStage

sealed class CompilationException(
    open val stage: CompilerStage,
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
                line != null && column != null -> "Compilation error at line $line, column $column: $message"
                line != null -> "Compilation error at line $line: $message"
                else -> "Compilation error: $message"
            }
    }
}
