package org.example.parser

sealed class Expression : ASTNode()

data class IntExpression(
    val value: Int,
    override val line: Int,
    override val column: Int,
) : Expression() {
    override fun prettyPrint(indent: Int): String {
        return "${indent(indent)}Int($value)"
    }
}
