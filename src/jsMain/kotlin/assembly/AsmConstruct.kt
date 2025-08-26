package assembly

sealed class AsmConstruct {
    protected fun indent(level: Int): String = "  ".repeat(level)
}
