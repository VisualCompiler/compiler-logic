package parser

import lexer.Token

sealed class Expression : ASTNode()

data class IntExpression(
    val value: Int
) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class UnaryExpression(
    val operator: Token,
    val expression: Expression
) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class BinaryExpression(
    val left: Expression,
    val operator: Token,
    val right: Expression
) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
