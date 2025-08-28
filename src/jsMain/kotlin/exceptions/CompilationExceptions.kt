package exceptions

import compiler.CompilerStage

sealed class CompilationException(
    open val stage: CompilerStage,
    message: String,
    val line: Int? = null,
    val column: Int? = null
) : Exception(buildMessage(stage, message, line, column)) {
    companion object {
        private fun buildMessage(
            stage: CompilerStage,
            message: String,
            line: Int?,
            column: Int?
        ): String =
            when {
                line != null && column != null -> "Error at line $line, column $column: $message"
                line != null -> "Error in stage $stage at line $line: $message"
                else -> message
            }
    }
}

class LexicalException(
    character: Char,
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.LEXER, "Invalid character '$character'", line, column)

class UnexpectedTokenException(
    val expected: String,
    val actual: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.PARSER, "Expected $expected, got $actual", line, column)

class UnexpectedEndOfFileException(
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.PARSER, "Expected end of file", line, column)

class DuplicateVariableDeclaration(
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.PARSER, "Variable cannot be declared twice", line, column)

class UndeclaredVariableException(
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.PARSER, "Variable is used before being declared", line, column)

class UndeclaredLabelException(
    label: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.PARSER, "Goto target '$label' is not defined.", line, column)

class DuplicateLabelException(
    label: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.PARSER, "Label '$label' is already defined.", line, column)

class InvalidLValueException(
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.PARSER, "Left side of assignment is invalid", line, column)

class InvalidStatementException(
    message: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.PARSER, message, line, column)

class TackyException(
    operator: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.TACKY, "Invalid operator: $operator", line, column)

class CodeGenerationException(
    message: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException(
    stage = CompilerStage.ASSEMBLY,
    "Code Generation error: $message",
    line,
    column
)
