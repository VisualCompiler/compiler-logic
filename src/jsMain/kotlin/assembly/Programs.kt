package assembly

sealed class Program : AsmConstruct()

data class AsmProgram(
    val function: AsmFunction
) : Program() {
    override fun toAsm(indentationLevel: Int): String = function.toAsm(indentationLevel)
}
