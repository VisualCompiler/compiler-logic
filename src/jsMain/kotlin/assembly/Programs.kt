package assembly

sealed class Program : AsmConstruct()

data class SimpleAsmProgram(
    val function: AsmFunction
) : Program() {
    override fun toAsm(indentationLevel: Int): String = function.toAsm(indentationLevel)
}

data class AsmProgram(
    val function: AsmFunction
) : AsmConstruct() {
    override fun toAsm(indentationLevel: Int): String = function.toAsm(indentationLevel)
}
