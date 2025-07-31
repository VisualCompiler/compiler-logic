package org.example.parser

sealed class Program : ASTNode()

data class SimpleProgram(
    val functionDefinition: FunctionDefinition,
) : Program() {
    override fun prettyPrint(indent: Int): String {
        return buildString {
            appendLine("${indent(indent)}SimpleProgram(")
            append(functionDefinition.prettyPrint(indent + 1))
            appendLine("${indent(indent)})")
        }
    }
}
