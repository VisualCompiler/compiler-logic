package org.example.parser

sealed class FunctionDefinition : ASTNode()

data class SimpleFunction(
    val name: Identifier,
    val body: Statement,
    override val line: Int,
    override val column: Int
) : FunctionDefinition() {
    override fun prettyPrint(indent: Int): String =
        buildString {
            appendLine("${indent(indent)}SimpleFunction(")
            append("${indent(indent + 1)}name=${name.prettyPrint(0)}")
            appendLine("${indent(indent + 1)}body=")
            append("${indent(indent)}${body.prettyPrint(indent + 1)}")
            appendLine("${indent(indent)})")
        }
}
