package org.example.wasm

sealed class Instruction : WASMConstruct()

data class Return(val operand: Operand) : Instruction()
