package assembly

sealed class Operand : AsmConstruct()

data class Imm(
    val value: Int
) : Operand() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}$value"
}

data class Register(
    val name: String
) : Operand() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}$name"
}
