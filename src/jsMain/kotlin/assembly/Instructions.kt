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

enum class AsmBinaryOp(
    val text: String
) {
    ADD("addl"),
    SUB("subl"),
    MUL("imull")
}

data class AsmUnary(
    val op: AsmUnaryOp,
    val dest: Operand
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}${op.text} ${dest.toAsm()}"
}

data class AsmBinary(
    val op: AsmBinaryOp,
    val src: Operand,
    val dest: Operand
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}${op.text} ${src.toAsm()}, ${dest.toAsm()}"
}

data class Idiv(
    val divisor: Operand
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "idivl ${divisor.toAsm()}"
}

// Convert Doubleword 32 to Quadword 64
object Cdq : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "cdq"
}

data class AllocateStack(
    val size: Int
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}subq $$size, %rsp"
}
