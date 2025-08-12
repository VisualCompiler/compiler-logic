package assembly

sealed class Function : AsmConstruct()

data class AsmFunction(
    val name: String,
    val body: List<Instruction>,
    override val line: Int,
    override val column: Int
) : Function() {
    override fun toAsm(indentationLevel: Int): String {
        val bodyAsm = body.joinToString("\n") { it.toAsm(indentationLevel + 1) }
        return buildString {
            appendLine("${indent(indentationLevel + 1)}.globl $name")
            appendLine("${indent(indentationLevel)}$name:")
            append(bodyAsm)
        }
    }
}
