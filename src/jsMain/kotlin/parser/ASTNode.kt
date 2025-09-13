package parser

import kotlin.random.Random

data class SourceLocation(val startLine: Int, val startCol: Int, val endLine: Int, val endCol: Int)

sealed class ASTNode(open val location: SourceLocation, open val id: String = Random.nextLong().toString()) {
    abstract fun <T> accept(visitor: Visitor<T>): T
}
