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

// Lexer
class InvalidCharacterException(
    character: Char,
    line: Int? = null,
    column: Int? = null
) : CompilationException("InvalidCharacterException('$character' is not a valid character)", line, column)

class UnexpectedCharacterException(
    expected: String,
    actual: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException("UnexpectedCharacterException(Expected '$expected', got '$actual')", line, column)

// Parser
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

class MissingDeclarationException(
    name: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException("MissingDeclarationException('$name' is not declared in this scope)", line, column)

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

class NestedFunctionException(
    line: Int? = null,
    column: Int? = null
) : CompilationException("NestedFunctionException(Can't define a function inside another function)", line, column)

class ReDeclarationFunctionException(
    name: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException("ReDeclarationFunctionException(Function '$name' cannot be defined more than once.)", line, column)

class IncompatibleFuncDeclarationException(
    name: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException(
    "IncompatibleFuncDeclarationException(Function '$name' redeclared with a different number of parameters.)",
    line,
    column
)

class NotAFunctionException(
    name: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException("NotAFunctionException(Cannot call '$name' because it is not a function.)", line, column)

class NotAVariableException(
    name: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException("NotAVariableException(Cannot use function '$name' as a variable.)", line, column)

class ArgumentCountException(
    name: String,
    expected: Int,
    actual: Int,
    line: Int? = null,
    column: Int? = null
) : CompilationException(
    "ArgumentCountException(Wrong number of arguments for function '$name'. Expected $expected, got $actual.)",
    line,
    column
)

class IllegalStateException(
    name: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException(
    "IllegalStateException(Internal error: Variable '$name' should have been caught by IdentifierResolution.)",
    line,
    column
)

// TACKY
class TackyException(
    operator: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException("TackyException(Invalid operator: $operator)", line, column)
