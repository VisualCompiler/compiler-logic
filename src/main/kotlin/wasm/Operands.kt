package org.example.wasm

sealed class Operand : WASMConstruct()

data class Imm(
    val value: Int,
) : Operand()
