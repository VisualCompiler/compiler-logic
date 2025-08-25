package assembly

sealed class AsmConstruct {
    // abstract fun toAsm(indentationLevel: Int = 0): String

    protected fun indent(level: Int): String = "  ".repeat(level)
}
