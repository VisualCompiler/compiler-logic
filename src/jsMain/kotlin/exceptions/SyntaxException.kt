package exceptions

import compiler.CompilerStage

class UnexpectedTokenSyntaxException(
    val expected: String,
    val actual: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.PARSER, "Expected $expected, got $actual", line, column)

class UnexpectedEndOfFileException(
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.PARSER, "Expected end of file", line, column)
