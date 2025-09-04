package parser

// sealed class Function : ASTNode()

data class FunctionDeclaration(
    val name: String,
    val params: List<String>,
    val body: Block?
) : ASTNode() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
