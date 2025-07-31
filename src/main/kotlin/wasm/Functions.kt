package org.example.wasm

sealed class Function : WASMConstruct()

data class WASMFunction(
    val name: Identifier,
    val body: List<Instruction>,
) : Function()
