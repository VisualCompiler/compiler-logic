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

    override fun hashCode(): Int = this::class.hashCode()
}

data class BreakStatement(
    var label: String = ""
) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class ContinueStatement(
    var label: String = ""
) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class WhileStatement(
    val condition: Expression,
    val body: Statement,
    var label: String = ""
) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class DoWhileStatement(
    val condition: Expression,
    val body: Statement,
    var label: String = ""
) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class ForStatement(
    val init: ForInit,
    val condition: Expression?,
    val post: Expression?,
    val body: Statement,
    var label: String = ""
) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

sealed class ForInit : ASTNode()

data class InitDeclaration(
    val declaration: Declaration
) : ForInit() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class InitExpression(
    val expression: Expression?
) : ForInit() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
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
