package exceptions

sealed class CompilationException(
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
                line != null && column != null -> "Line $line, column $column: $message"
                line != null -> "Line $line: $message"
                else -> message
            }
    }
}

class LexicalException(
    character: Char,
    line: Int? = null,
    column: Int? = null
) : CompilationException("LexicalException(Invalid character '$character')", line, column)

class UnexpectedTokenException(
    val expected: String,
    val actual: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException("UnexpectedTokenException(Expected $expected, got $actual)", line, column)

class UnexpectedEndOfFileException(
    line: Int? = null,
    column: Int? = null
) : CompilationException("UnexpectedEndOfFileException(Expected end of file)", line, column)

class DuplicateVariableDeclaration(
    line: Int? = null,
    column: Int? = null
) : CompilationException("DuplicateVariableDeclaration(Variable cannot be declared twice)", line, column)

class UndeclaredVariableException(
    line: Int? = null,
    column: Int? = null
) : CompilationException("UndeclaredVariableException(Variable is used before being declared)", line, column)

class UndeclaredLabelException(
    label: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException("UndeclaredLabelException(Goto target '$label' is not defined)", line, column)

class DuplicateLabelException(
    label: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException("DuplicateLabelException(Label '$label' is already defined)", line, column)

class InvalidLValueException(
    line: Int? = null,
    column: Int? = null
) : CompilationException("InvalidLValueException(Left side of assignment is invalid)", line, column)

class InvalidStatementException(
    message: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException("InvalidStatementException($message)", line, column)

class TackyException(
    operator: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException("TackyException(Invalid operator: $operator)", line, column)
