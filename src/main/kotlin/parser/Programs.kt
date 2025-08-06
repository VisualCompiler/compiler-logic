package org.example.parser

sealed class Program : ASTNode()

data class SimpleProgram(
    val functionDefinition: FunctionDefinition,
    override val line: Int,
    override val column: Int
) : Program() {
    override fun prettyPrint(indent: Int): String =
        buildString {
            appendLine("${indent(indent)}SimpleProgram(")
            append(functionDefinition.prettyPrint(indent + 1))
            appendLine("${indent(indent)})")
        }
}
