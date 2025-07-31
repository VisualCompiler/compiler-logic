package org.example.parser

data class Identifier(
    val value: String,
    override val line: Int,
    override val column: Int,
) : ASTNode() {
    override fun prettyPrint(indent: Int): String {
        return buildString { appendLine("${indent(indent)}\"${this@Identifier.value}\"") }
    }
}
