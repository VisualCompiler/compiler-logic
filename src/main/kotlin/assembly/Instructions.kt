package assembly

sealed class Instruction : AsmConstruct()

data class Ret(
    override val line: Int,
    override val column: Int
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}ret"
}

data class Mov(
    val src: Operand,
    val dest: Operand,
    override val line: Int,
    override val column: Int
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}movl ${src.toAsm()}, ${dest.toAsm()}"
}
