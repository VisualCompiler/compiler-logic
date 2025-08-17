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
    ADD(""),
    SUB(""),
    MUL(""),
    DIV("")
}

data class AsmUnary(
    val op: AsmUnaryOp,
    val dest: Operand
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}${op.text} ${dest.toAsm()}"
}

data class Add(
    val src: Operand,
    val dest: Operand
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}addl ${src.toAsm()}, ${dest.toAsm()}"
}

data class Imul(
    val src: Operand,
    val dest: Operand
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}imull ${src.toAsm()}, ${dest.toAsm()}"
}

data class Sub(
    val src: Operand,
    val dest: Operand
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}subl ${src.toAsm()}, ${dest.toAsm()}"
}

data class Idiv(
    val divisor: Operand
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "idivl ${divisor.toAsm()}"
}

object Cdq : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "cdq"
}

data class AllocateStack(
    val size: Int
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}subq $$size, %rsp"
}
