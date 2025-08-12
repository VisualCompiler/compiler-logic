package assembly

sealed class Program : AsmConstruct()

data class SimpleAsmProgram(
    val function: AsmFunction,
    override val line: Int,
    override val column: Int
) : Program() {
    override fun toAsm(indentationLevel: Int): String = function.toAsm(indentationLevel)
}
