package org.example.parser

import lexer.Token

sealed class Expression : ASTNode()

data class IntExpression(
    val value: Token
) : Expression() {
    override fun prettyPrint(indent: Int): String {
        return "${indent(indent)}Int(${value.lexeme})"
    }
}
