package org.example.parser

sealed class ASTNode {
    abstract val line: Int
    abstract val column: Int

    abstract fun prettyPrint(indent: Int = 0): String

    fun indent(level: Int): String = "    ".repeat(level) // 4 spaces per level
}
