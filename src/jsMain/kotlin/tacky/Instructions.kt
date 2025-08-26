package tacky

sealed class TackyInstruction : TackyConstruct()

private fun TackyUnaryOP.toSymbol(): String =
    when (this) {
        TackyUnaryOP.NEGATE -> "-"
        TackyUnaryOP.COMPLEMENT -> "~"
        TackyUnaryOP.NOT -> "!"
    }

private fun TackyBinaryOP.toSymbol(): String =
    when (this) {
        TackyBinaryOP.ADD -> "+"
        TackyBinaryOP.SUBTRACT -> "-"
        TackyBinaryOP.MULTIPLY -> "*"
        TackyBinaryOP.DIVIDE -> "/"
        TackyBinaryOP.REMAINDER -> "%"
        TackyBinaryOP.EQUAL -> "=="
        TackyBinaryOP.NOT_EQUAL -> "!="
        TackyBinaryOP.LESS -> "<"
        TackyBinaryOP.LESS_EQUAL -> "<="
        TackyBinaryOP.GREATER -> ">"
        TackyBinaryOP.GREATER_EQUAL -> ">="
    }

data class TackyRet(
    val value: TackyVal
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String = "${indent(indentationLevel)}return ${value.toPseudoCode()}"
}

enum class TackyUnaryOP {
    COMPLEMENT,
    NEGATE,
    NOT
}

data class TackyUnary(
    val operator: TackyUnaryOP,
    val src: TackyVal,
    val dest: TackyVar
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String =
        "${indent(indentationLevel)}${dest.toPseudoCode()} = ${operator.toSymbol()}${src.toPseudoCode()}"
}

enum class TackyBinaryOP {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    REMAINDER,
    LESS,
    GREATER,
    LESS_EQUAL,
    GREATER_EQUAL,
    EQUAL,
    NOT_EQUAL
}

data class TackyBinary(
    val operator: TackyBinaryOP,
    val src1: TackyVal,
    val src2: TackyVal,
    val dest: TackyVar
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String =
        "${indent(indentationLevel)}${dest.toPseudoCode()} = ${src1.toPseudoCode()} ${operator.toSymbol()} ${src2.toPseudoCode()}"
}

data class TackyCopy(
    val src: TackyVal,
    val dest: TackyVar
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String = "${indent(indentationLevel)}${dest.toPseudoCode()} = ${src.toPseudoCode()}"
}

data class TackyJump(
    val target: TackyLabel
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String = "${indent(indentationLevel)}goto ${target.name}"
}

data class JumpIfZero(
    val condition: TackyVal,
    val target: TackyLabel
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String =
        "${indent(indentationLevel)}if (${condition.toPseudoCode()} == 0) goto ${target.name}"
}

data class JumpIfNotZero(
    val condition: TackyVal,
    val target: TackyLabel
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String =
        "${indent(indentationLevel)}if (${condition.toPseudoCode()} != 0) goto ${target.name}"
}
