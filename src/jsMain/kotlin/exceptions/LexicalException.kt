package exceptions

import CompilerStage

class LexicalException(
    character: Char,
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.LEXER, "Invalid character '$character'", line, column)
