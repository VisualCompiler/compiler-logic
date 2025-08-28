package parser

sealed class Statement : ASTNode()

data class ReturnStatement(
    val expression: Expression
) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class ExpressionStatement(
    val expression: Expression
) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

class NullStatement : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)

    override fun equals(other: Any?): Boolean = other is NullStatement
}

class IfStatement(
    val condition: Expression,
    val then: Statement,
    val _else: Statement?
) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

class GotoStatement(
    val label: String
) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

class LabeledStatement(
    val label: String,
    val statement: Statement
) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class Declaration(
    val name: String,
    val init: Expression?
) : ASTNode() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

sealed class BlockItem : ASTNode()

data class S(
    val statement: Statement
) : BlockItem() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class D(
    val declaration: Declaration
) : BlockItem() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
