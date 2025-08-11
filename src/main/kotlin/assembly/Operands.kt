package assembly

sealed class Operand : AsmConstruct()

data class Imm(
    val value: Int,
    override val line: Int,
    override val column: Int
) : Operand() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}$value"
}

data class Register(
    val name: String,
    override val line: Int,
    override val column: Int
) : Operand() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}$name"
}
