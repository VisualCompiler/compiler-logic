package exceptions

import compiler.CompilerStage

sealed class CompilationExceptions(
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

class LexicalException(
    character: Char,
    line: Int? = null,
    column: Int? = null
) : CompilationExceptions(CompilerStage.LEXER, "Invalid character '$character'", line, column)

class UnexpectedTokenException(
    val expected: String,
    val actual: String,
    line: Int? = null,
    column: Int? = null
) : CompilationExceptions(CompilerStage.PARSER, "Expected $expected, got $actual", line, column)

class UnexpectedEndOfFileException(
    line: Int? = null,
    column: Int? = null
) : CompilationExceptions(CompilerStage.PARSER, "Expected end of file", line, column)

class DuplicateVariableDeclaration(
    line: Int? = null,
    column: Int? = null
) : CompilationExceptions(CompilerStage.PARSER, "Variable cannot be declared twice", line, column)

class UndeclaredVariableException(
    line: Int? = null,
    column: Int? = null
) : CompilationExceptions(CompilerStage.PARSER, "Variable is used before being declared", line, column)

class UndeclaredLabelException(
    label: String,
    line: Int? = null,
    column: Int? = null
) : CompilationExceptions(CompilerStage.PARSER, "Goto target '$label' is not defined.", line, column)

class DuplicateLabelException(
    label: String,
    line: Int? = null,
    column: Int? = null
) : CompilationExceptions(CompilerStage.PARSER, "Label '$label' is already defined.", line, column)

class InvalidLValueException(
    line: Int? = null,
    column: Int? = null
) : CompilationExceptions(CompilerStage.PARSER, "Left side of assignment is invalid", line, column)

class TackyException(
    operator: String,
    line: Int? = null,
    column: Int? = null
) : CompilationExceptions(CompilerStage.TACKY, "Invalid operator: $operator", line, column)

class CodeGenerationException(
    message: String,
    line: Int? = null,
    column: Int? = null
) : CompilationExceptions(
    stage = CompilerStage.ASSEMBLY,
    "Code Generation error: $message",
    line,
    column
)
