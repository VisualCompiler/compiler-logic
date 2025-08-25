package exceptions

import compiler.CompilerStage

class CodeGenerationException(
    message: String,
    line: Int? = null,
    column: Int? = null
) : CompilationException(
    stage = CompilerStage.CODE_GENERATOR,
    "Code Generation error: $message",
    line,
    column
)

// Later, when needed, we can add specific exceptions like register allocation exception, etc.
