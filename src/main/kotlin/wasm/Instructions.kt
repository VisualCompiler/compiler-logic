package org.example.wasm

sealed class Instruction : WASMConstruct()

data class Return(
    val operand: Operand,
    override val line: Int,
    override val column: Int,
) : Instruction() {
    override fun toWat(indent: Int): String =
        buildString {
            appendLine("${indent(indent)}${operand.toWat()}")
            appendLine("${indent(indent)}return")
        }
}
