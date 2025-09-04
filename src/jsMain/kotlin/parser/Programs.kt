package parser

sealed class Program : ASTNode()

data class SimpleProgram(
    val functionDeclaration: List<FunctionDeclaration>
) : Program() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
