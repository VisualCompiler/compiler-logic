package org.example.wasm

sealed class Function : WASMConstruct()

data class WASMFunction(
    val name: String,
    val body: List<Instruction>,
    override val line: Int,
    override val column: Int,
) : Function() {
    override fun toWat(indent: Int): String {
        val bodyWat = body.joinToString("\n") { it.toWat(indent + 1) }
        return buildString {
            appendLine("${indent(indent)}(func $$name")
            appendLine(bodyWat)
            appendLine("${indent(indent)})")
        }
    }
}
