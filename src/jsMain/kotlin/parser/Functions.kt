package parser

sealed class FunctionDefinition : ASTNode()

data class Function(
    val name: String,
    val body: Block
) : FunctionDefinition() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
