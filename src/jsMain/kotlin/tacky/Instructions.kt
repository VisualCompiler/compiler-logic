package org.example.tacky

sealed class TackyInstruction : TackyConstruct()

data class TackyRet(
    val value: TackyVal
) : TackyInstruction()

data class TackyUnary(
    val operator: TackyUnaryOP,
    val src: TackyVal,
    val dest: TackyVar
) : TackyInstruction()

enum class TackyUnaryOP {
    COMPLEMENT,
    NEGATE
}

enum class TackyBinaryOP {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE
}

data class TackyBinary(
    val operator: TackyBinaryOP,
    val src1: TackyVal,
    val src2: TackyVal,
    val dest: TackyVar
) : TackyInstruction()
