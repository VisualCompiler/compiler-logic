package org.example.parser

sealed class Statement : ASTNode()

data class ReturnStatement(
    val expression: Expression,
    override val line: Int,
    override val column: Int
) : Statement() {
    override fun prettyPrint(indent: Int): String =
        buildString {
            appendLine("${indent(indent)}ReturnStatement(")
            appendLine(expression.prettyPrint(indent + 1))
            appendLine("${indent(indent)})")
        }
}
