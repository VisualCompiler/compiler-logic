package org.example.tacky

sealed class TackyConstruct

sealed class TackyVal : TackyConstruct()

data class TackyConstant(
    val value: Int
) : TackyVal()

data class TackyVar(
    val name: String
) : TackyVal()

data class TackyProgram(
    val function: TackyFunction
) : TackyConstruct()

data class TackyFunction(
    val name: String,
    val body: List<TackyInstruction>
) : TackyConstruct()
