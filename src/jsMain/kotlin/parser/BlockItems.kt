package parser

sealed class Statement(
    location: SourceLocation
) : ASTNode(location)

data class ReturnStatement(
    val expression: Expression,
    override val location: SourceLocation
) : Statement(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

data class ExpressionStatement(
    val expression: Expression,
    override val location: SourceLocation
) : Statement(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

class NullStatement(
    override val location: SourceLocation
) : Statement(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)

    override fun equals(other: Any?): Boolean = other is NullStatement

    override fun hashCode(): Int = this::class.hashCode()
}

data class BreakStatement(
    var label: String = "",
    override val location: SourceLocation
) : Statement(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

data class ContinueStatement(
    var label: String = "",
    override val location: SourceLocation
) : Statement(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

data class WhileStatement(
    val condition: Expression,
    val body: Statement,
    var label: String = "",
    override val location: SourceLocation
) : Statement(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

data class DoWhileStatement(
    val condition: Expression,
    val body: Statement,
    var label: String = "",
    override val location: SourceLocation
) : Statement(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

data class ForStatement(
    val init: ForInit,
    val condition: Expression?,
    val post: Expression?,
    val body: Statement,
    var label: String = "",
    override val location: SourceLocation
) : Statement(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

sealed class ForInit(
    location: SourceLocation
) : ASTNode(location)

data class InitDeclaration(
    val varDeclaration: VariableDeclaration,
    override val location: SourceLocation
) : ForInit(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

data class InitExpression(
    val expression: Expression?,
    override val location: SourceLocation
) : ForInit(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

class IfStatement(
    val condition: Expression,
    val then: Statement,
    val _else: Statement?,
    override val location: SourceLocation
) : Statement(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

class GotoStatement(
    val label: String,
    override val location: SourceLocation
) : Statement(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

class LabeledStatement(
    val label: String,
    val statement: Statement,
    override val location: SourceLocation
) : Statement(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

sealed class Declaration(
    location: SourceLocation
) : ASTNode(location)

data class VariableDeclaration(
    val name: String,
    val init: Expression?,
    override val location: SourceLocation
) : Declaration(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

data class VarDecl(
    val varDecl: VariableDeclaration
) : Declaration(location = varDecl.location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

data class FunDecl(
    val funDecl: FunctionDeclaration
) : Declaration(location = funDecl.location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

sealed class BlockItem(
    location: SourceLocation
) : ASTNode(location)

data class S(
    val statement: Statement
) : BlockItem(location = statement.location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

data class D(
    val declaration: Declaration
) : BlockItem(declaration.location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

data class CompoundStatement(
    val block: Block,
    override val location: SourceLocation
) : Statement(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}

data class Block(
    val items: List<BlockItem>,
    override val location: SourceLocation
) : ASTNode(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}
