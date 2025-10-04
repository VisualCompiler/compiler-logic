package parser

// sealed class Function : ASTNode()

data class FunctionDeclaration(
    val name: String,
    val params: List<String>,
    val body: Block?,
    override val location: SourceLocation
) : ASTNode(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}
