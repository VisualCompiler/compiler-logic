package parser

sealed class ASTNode {
    abstract fun <T> accept(visitor: Visitor<T>): T
}
