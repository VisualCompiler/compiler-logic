package tacky

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class TackyInstruction() : TackyConstruct() {
    abstract val sourceId: String
}

@Serializable
@SerialName("TackyRet")
data class TackyRet(
    val value: TackyVal,
    override val sourceId: String = ""
) : TackyInstruction() {

    override fun toPseudoCode(indentationLevel: Int): String = "${indent(indentationLevel)}return ${value.toPseudoCode()}"
}

enum class TackyUnaryOP(
    val text: String
) {
    COMPLEMENT("~"),
    NEGATE("-"),
    NOT("!")
}

@Serializable
@SerialName("TackyUnary")
data class TackyUnary(
    val operator: TackyUnaryOP,
    val src: TackyVal,
    val dest: TackyVar,
    override val sourceId: String = ""
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String =
        "${indent(indentationLevel)}${dest.toPseudoCode()} = ${operator.text}${src.toPseudoCode()}"
}

enum class TackyBinaryOP(
    val text: String
) {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    REMAINDER("%"),
    LESS("<"),
    GREATER(">"),
    LESS_EQUAL("<="),
    GREATER_EQUAL(">="),
    EQUAL("="),
    NOT_EQUAL("!=")
}

@Serializable
@SerialName("TackyBinary")
data class TackyBinary(
    val operator: TackyBinaryOP,
    val src1: TackyVal,
    val src2: TackyVal,
    val dest: TackyVar,
    override val sourceId: String = ""
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String =
        "${indent(indentationLevel)}${dest.toPseudoCode()} = ${src1.toPseudoCode()} ${operator.text} ${src2.toPseudoCode()}"
}

@Serializable
@SerialName("TackyCopy")
data class TackyCopy(
    val src: TackyVal,
    val dest: TackyVar,
    override val sourceId: String = ""
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String = "${indent(indentationLevel)}${dest.toPseudoCode()} = ${src.toPseudoCode()}"
}

@Serializable
@SerialName("TackyJump")
data class TackyJump(
    val target: TackyLabel,
    override val sourceId: String = ""
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String = "${indent(indentationLevel)}goto ${target.name}"
}

@Serializable
@SerialName("JumpIfZero")
data class JumpIfZero(
    val condition: TackyVal,
    val target: TackyLabel,
    override val sourceId: String = ""
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String =
        "${indent(indentationLevel)}if (${condition.toPseudoCode()} == 0) goto ${target.name}"
}

@Serializable
@SerialName("JumpIfNotZero")
data class JumpIfNotZero(
    val condition: TackyVal,
    val target: TackyLabel,
    override val sourceId: String = ""
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String =
        "${indent(indentationLevel)}if (${condition.toPseudoCode()} != 0) goto ${target.name}"
}

@Serializable
@SerialName("TackyFunCall")
data class TackyFunCall(
    val funName: String,
    val args: List<TackyVal>,
    val dest: TackyVar,
    override val sourceId: String = ""
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String {
        val argString = args.joinToString(", ") { it.toPseudoCode() }
        return "${indent(indentationLevel)}${dest.toPseudoCode()} = $funName($argString)"
    }
}

@Serializable
@SerialName("TackyLabel")
data class TackyLabel(
    val name: String,
    override val sourceId: String = ""
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String = "$name:"
}
