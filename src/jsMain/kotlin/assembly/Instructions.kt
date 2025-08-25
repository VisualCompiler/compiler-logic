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

data class Cmp(
    val src: Operand,
    val dest: Operand
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}cmpl ${src.toAsm()}, ${dest.toAsm()}"
}

enum class JumpCondition(
    val text: String
) {
    E("je"), // Equal
    NE("jne"), // Not Equal
    L("jl"), // Less
    LE("jle"), // Less or Equal
    G("jg"), // Greater
    GE("jge"), // Greater or Equal
    Z("jz"), // Zero
    NZ("jnz") // Not Zero
}

enum class ConditionCode { E, NE, G, GE, L, LE }

data class Jcc(
    val condition: JumpCondition,
    val label: Label
) : Instruction() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}${condition.text} ${label.name}"
}

data class SetCC(
    val condition: ConditionCode,
    val dest: Operand
) : Instruction() {
    // The toAsm method is not needed here if the CodeEmitter handles everything.
    // Or, if it's required by the sealed class, it can be simple:
    override fun toAsm(indentationLevel: Int): String {
        // This is just a placeholder; the real logic is in the emitter.
        return "SetCC(condition=$condition, dest=$dest)"
    }
}
