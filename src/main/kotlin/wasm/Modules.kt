package org.example.wasm

sealed class Module : WASMConstruct()

data class SimpleModule(
    val function: Function,
    override val line: Int,
    override val column: Int,
) : Module() {
    override fun toWat(indent: Int): String =
        buildString {
            appendLine("(module")
            appendLine(function.toWat(indent + 1))
            if (function is WASMFunction) {
                appendLine("${indent(indent + 1)}(export \"${function.name}\" (func $${function.name}))")
            }
            appendLine(")")
        }
}
