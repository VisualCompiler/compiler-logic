package parser

import lexer.Token

sealed class Expression(location: SourceLocation) : ASTNode(location)

data class IntExpression(
    val value: Int,
    override val location: SourceLocation
) : Expression(location) {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class VariableExpression(
    val name: String,
    override val location: SourceLocation
) : Expression(location) {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class UnaryExpression(
    val operator: Token,
    val expression: Expression,
    override val location: SourceLocation
) : Expression(location) {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class BinaryExpression(
    val left: Expression,
    val operator: Token,
    val right: Expression,
    override val location: SourceLocation
) : Expression(location) {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class AssignmentExpression(
    val lvalue: VariableExpression,
    val rvalue: Expression,
    override val location: SourceLocation
) : Expression(location) {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class ConditionalExpression(
    val codition: Expression,
    val thenExpression: Expression,
    val elseExpression: Expression,
    override val location: SourceLocation
) : Expression(location) {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class FunctionCall(
    val name: String,
    val arguments: List<Expression>,
    override val location: SourceLocation
) : Expression(location) {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
