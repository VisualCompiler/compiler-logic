package assembly

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Instruction() : AsmConstruct() {
    abstract val sourceId: String
}

@Serializable
@SerialName("Ret")
data class Ret(
    override val sourceId: String = ""
) : Instruction()

@Serializable
@SerialName("Mov")
data class Mov(
    val src: Operand,
    val dest: Operand,
    override val sourceId: String = ""
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

@Serializable
@SerialName("AsmUnary")
data class AsmUnary(
    val op: AsmUnaryOp,
    val dest: Operand,
    override val sourceId: String = ""
) : Instruction()

@Serializable
@SerialName("AsmBinary")
data class AsmBinary(
    val op: AsmBinaryOp,
    val src: Operand,
    val dest: Operand,
    override val sourceId: String = ""
) : Instruction()

@Serializable
@SerialName("Idiv")
data class Idiv(
    val divisor: Operand,
    override val sourceId: String = ""
) : Instruction()

// Convert Doubleword 32 to Quadword 64
@Serializable
@SerialName("Cdq")
data class Cdq(
    override val sourceId: String = ""
) : Instruction()

@Serializable
@SerialName("AllocateStack")
data class AllocateStack(
    val size: Int,
    override val sourceId: String = ""
) : Instruction()

@Serializable
@SerialName("DeAllocateStack")
data class DeAllocateStack(
    val size: Int,
    override val sourceId: String = ""
) : Instruction()

@Serializable
@SerialName("Push")
data class Push(
    val operand: Operand,
    override val sourceId: String = ""
) : Instruction()

@Serializable
@SerialName("Call")
data class Call(
    val identifier: String,
    override val sourceId: String = ""
) : Instruction()

@Serializable
@SerialName("Label")
data class Label(
    val name: String,
    override val sourceId: String = ""
) : Instruction()

@Serializable
@SerialName("Jmp")
data class Jmp(
    val label: Label,
    override val sourceId: String = ""
) : Instruction()

@Serializable
@SerialName("JmpCC")
data class JmpCC(
    val condition: ConditionCode,
    val label: Label,
    override val sourceId: String = ""
) : Instruction()

@Serializable
@SerialName("Cmp")
data class Cmp(
    val src: Operand,
    val dest: Operand,
    override val sourceId: String = ""
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

@Serializable
@SerialName("SetCC")
data class SetCC(
    val condition: ConditionCode,
    val dest: Operand,
    override val sourceId: String = ""
) : Instruction()
