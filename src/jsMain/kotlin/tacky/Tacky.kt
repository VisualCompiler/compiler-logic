package org.example.tacky

sealed class TackyConstruct

sealed class TackyVal : TackyConstruct()

data class TackyConstant(
    val value: Int
) : TackyVal()

data class TackyVar(
    val name: String
) : TackyVal()

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

data class TackyFunction(
    val name: String,
    val body: List<TackyInstruction>
) : TackyConstruct()

data class TackyProgram(
    val function: TackyFunction
) : TackyConstruct()
