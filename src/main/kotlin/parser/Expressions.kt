package org.example.parser

import lexer.Token

sealed class Expression : ASTNode()

data class IntExpression(
    val value: Int,
    override val line: Int,
    override val column: Int
) : Expression() {
    override fun prettyPrint(indent: Int): String = "${indent(indent)}Int($value)"
}

data class UnaryExpression(
    val operator: Token,
    val expression: Expression,
    override val line: Int,
    override val column: Int
) : Expression() {
    override fun prettyPrint(indent: Int): String =
        buildString {
            appendLine("${indent(indent)}UnaryExpression(operator='${operator.lexeme}')")
            append(expression.prettyPrint(indent + 1))
        }
}

data class BinaryExpression(
    val left: Expression,
    val operator: Token,
    val right: Expression,
    override val line: Int,
    override val column: Int
) : Expression() {
    override fun prettyPrint(indent: Int): String =
        buildString {
            appendLine("${indent(indent)}BinaryExpression(operator='${operator.lexeme}')")
            append(left.prettyPrint(indent + 1))
            append(right.prettyPrint(indent + 1))
        }
}
