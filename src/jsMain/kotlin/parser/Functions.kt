package parser

sealed class FunctionDefinition : ASTNode()

data class SimpleFunction(
    val name: String,
    val body: Statement
) : FunctionDefinition() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
