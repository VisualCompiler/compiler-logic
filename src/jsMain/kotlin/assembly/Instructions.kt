package assembly

sealed class Instruction() : AsmConstruct()

object Ret : Instruction()

data class Mov(
    val src: Operand,
    val dest: Operand,
    val sourceId: String = ""
) : Instruction()

enum class AsmUnaryOp(
    val text: String
) {
    NEG("neg"),
    NOT("not") // complement
}

enum class AsmBinaryOp(
    val text: String
) {
    ADD("add"),
    SUB("sub"),
    MUL("imul")
}

data class AsmUnary(
    val op: AsmUnaryOp,
    val dest: Operand,
    val sourceId: String = ""
) : Instruction()

data class AsmBinary(
    val op: AsmBinaryOp,
    val src: Operand,
    val dest: Operand,
    val sourceId: String = ""
) : Instruction()

data class Idiv(
    val divisor: Operand,
    val sourceId: String = ""
) : Instruction()

// Convert Doubleword 32 to Quadword 64
object Cdq : Instruction()

data class AllocateStack(
    val size: Int,
    val sourceId: String = ""
) : Instruction()

data class DeAllocateStack(
    val size: Int,
    val sourceId: String = ""
) : Instruction()

data class Push(
    val operand: Operand,
    val sourceId: String = ""
) : Instruction()

data class Call(
    val identifier: String,
    val sourceId: String = ""
) : Instruction()

data class Label(
    val name: String,
    val sourceId: String = ""
) : Instruction()

data class Jmp(
    val label: Label,
    val sourceId: String = ""
) : Instruction()

data class JmpCC(
    val condition: ConditionCode,
    val label: Label,
    val sourceId: String = ""
) : Instruction()

data class Cmp(
    val src: Operand,
    val dest: Operand,
    val sourceId: String = ""
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
    val dest: Operand,
    val sourceId: String = ""
) : Instruction()
