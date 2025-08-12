package assembly

sealed class Program : AsmConstruct()

data class SimpleAsmProgram(
    val function: AsmFunction
) : Program() {
    override fun toAsm(indentationLevel: Int): String = function.toAsm(indentationLevel)
}
