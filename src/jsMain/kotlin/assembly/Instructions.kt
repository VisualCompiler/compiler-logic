package assembly

sealed class Instruction : AsmConstruct()

object Ret : Instruction()

data class Mov(
    val src: Operand,
    val dest: Operand
) : Instruction()

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
) : Instruction()

data class AsmBinary(
    val op: AsmBinaryOp,
    val src: Operand,
    val dest: Operand
) : Instruction()

data class Idiv(
    val divisor: Operand
) : Instruction()

// Convert Doubleword 32 to Quadword 64
object Cdq : Instruction()

data class AllocateStack(
    val size: Int
) : Instruction()

data class Label(
    val name: String
) : Instruction()

data class Jmp(
    val label: Label
) : Instruction()

data class JmpCC(
    val condition: ConditionCode,
    val label: Label
) : Instruction()

data class Cmp(
    val src: Operand,
    val dest: Operand
) : Instruction()

enum class ConditionCode(
    val text: String
) {
    E("e"), // Equal
    NE("ne"), // Not Equal
    L("l"), // Less
    LE("le"), // Less or Equal
    G("g"), // Greater
    GE("ge") // Greater or Equal
}

data class SetCC(
    val condition: ConditionCode,
    val dest: Operand
) : Instruction()
