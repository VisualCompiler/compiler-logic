package parser

sealed class Program(
    location: SourceLocation
) : ASTNode(location)

data class SimpleProgram(
    val functionDeclaration: List<FunctionDeclaration>,
    override val location: SourceLocation
) : Program(location) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visit(this)
}
