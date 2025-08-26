package parser

sealed class Statement : ASTNode()

data class ReturnStatement(
    val expression: Expression
) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
