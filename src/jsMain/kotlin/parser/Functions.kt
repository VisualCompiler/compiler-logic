package parser

sealed class FunctionDefinition : ASTNode()

data class Function(
    val name: String,
    val body: List<BlockItem>
) : FunctionDefinition() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
