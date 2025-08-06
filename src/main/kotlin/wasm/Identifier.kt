package org.example.wasm

sealed class Identifier : WASMConstruct()

data class WASMIdentifier(
    val name: String,
    override val line: Int,
    override val column: Int
) : Identifier() {
    override fun toWat(indent: Int): String = "${indent(indent)}(local $${this.name} i32)\n"
}
