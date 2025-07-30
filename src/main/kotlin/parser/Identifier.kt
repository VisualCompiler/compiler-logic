package org.example.parser

import lexer.Token

data class Identifier(
    val token: Token
) : ASTNode() {
    override fun prettyPrint(indent: Int): String {
        return buildString { appendLine("${indent(indent)}\"${token.lexeme}\"") }
    }
}
