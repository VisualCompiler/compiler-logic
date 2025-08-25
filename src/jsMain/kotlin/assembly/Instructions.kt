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
    NOT("notl") // complement
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

data class Label(
    val name: String
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "$name:"
}

data class Jmp(
    val label: Label
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}jmp ${label.name}"
}

data class Cmp(
    val src: Operand,
    val dest: Operand
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}cmpl ${src.toAsm()}, ${dest.toAsm()}"
}

enum class ConditionCode { E, NE, G, GE, L, LE }

data class JmpCC(
    val condition: ConditionCode,
    val label: Label
) : Instruction() {
    private val opText =
        when (condition) {
            ConditionCode.E -> "je"
            ConditionCode.NE -> "jne"
            ConditionCode.G -> "jg"
            ConditionCode.GE -> "jge"
            ConditionCode.L -> "jl"
            ConditionCode.LE -> "jle"
        }

    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}$opText ${label.name}"
}

data class SetCC(
    val condition: ConditionCode,
    val dest: Operand
) : Instruction() {
    private val opText =
        when (condition) {
            ConditionCode.E -> "sete"
            ConditionCode.NE -> "setne"
            ConditionCode.G -> "setg"
            ConditionCode.GE -> "setge"
            ConditionCode.L -> "setl"
            ConditionCode.LE -> "setle"
        }

    // Note: This will be fixed later to handle 1-byte registers. For now, this is fine.
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}$opText ${dest.toAsm()}"
}
