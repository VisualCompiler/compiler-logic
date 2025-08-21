package assembly

sealed class Function : AsmConstruct()

data class AsmFunction(
    val name: String,
    val body: List<Instruction>
) : Function() {
    override fun toAsm(indentationLevel: Int): String {
        val bodyAsm = body.joinToString("\n") { it.toAsm(indentationLevel + 1) }
        return buildString {
            // code emission
            appendLine("${indent(indentationLevel + 1)}.globl $name")
            appendLine("${indent(indentationLevel)}$name:")
            appendLine("  pushq %rbp")
            appendLine("  movq %rsp, %rbp")
            appendLine(bodyAsm)
            appendLine("  popq %rbp")
            appendLine("  ret")
        }
    }
}
