package parser

import lexer.Token

sealed class Expression : ASTNode()

data class IntExpression(
    val value: Int
) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class VariableExpression(
    val name: String
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

data class AssignmentExpression(
    val lvalue: VariableExpression,
    val rvalue: Expression
) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class ConditionalExpression(
    val codition: Expression,
    val thenExpression: Expression,
    val elseExpression: Expression
) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class FunctionCall(
    val name: String,
    val arguments: List<Expression>
) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
