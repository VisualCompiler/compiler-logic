package assembly

sealed class Instruction : AsmConstruct()

object Ret : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}ret"
}

data class Mov(
    val src: Operand,
    val dest: Operand
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}movl ${src.toAsm()}, ${dest.toAsm()}"
}

enum class AsmUnaryOp(
    val text: String
) {
    NEG("negl"),
    NOT("notl")
}

data class AsmUnary(
    val op: AsmUnaryOp,
    val dest: Operand
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}${op.text} ${dest.toAsm()}"
}

data class AllocateStack(
    val size: Int
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}subq $$size, %rsp"
}
