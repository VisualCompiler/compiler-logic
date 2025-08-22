package exceptions

import CompilerStage

class TackyException(
    operator: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException(CompilerStage.TACKY, "Invalid operator: $operator", line, column)
