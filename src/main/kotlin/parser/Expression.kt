package org.example.parser

import lexer.Token

sealed class Expression : ASTNode()

data class IntExpression(
    val value: Token
) : Expression() {
    override fun prettyPrint(indent: Int): String {
        return "${indent(indent)}Int(${value.lexeme})"
    }
}

// support arithmetic operation
data class BinaryExpression(
    val left_operand: Expression,
    val operator: Token,
    val right_operand: Expression
) : Expression() {
    override fun prettyPrint(indent: Int): String {
        return buildString {
            appendLine("${indent(indent)}BinaryExpression(operator=${operator.lexeme})")
            appendLine("${indent(indent + 1)}left =")
            append(left_operand.prettyPrint(indent + 2))
            appendLine("${indent(indent + 1)}right =")
            append(right_operand.prettyPrint(indent + 2))
            append("${indent(indent)})")
        }
    }
}
