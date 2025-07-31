package org.example.wasm

sealed class Operand : WASMConstruct()

data class Imm(
    val value: Int,
    override val line: Int,
    override val column: Int,
) : Operand() {
    override fun toWat(indent: Int): String = "${indent(indent)}i32.const $value\n"
}
