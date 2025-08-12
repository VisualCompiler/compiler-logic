package assembly

sealed class AsmConstruct {
    abstract val line: Int
    abstract val column: Int

    abstract fun toAsm(indentationLevel: Int = 0): String

    protected fun indent(level: Int): String = "  ".repeat(level)
}
