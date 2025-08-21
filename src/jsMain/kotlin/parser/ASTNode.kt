package parser

sealed class ASTNode {
    abstract fun prettyPrint(indent: Int = 0): String

    fun indent(level: Int): String = "    ".repeat(level) // 4 spaces per level

    abstract fun toJsonString(): String

    abstract fun <T> accept(visitor: Visitor<T>): T
}
