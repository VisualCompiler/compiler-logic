package org.example.parser

import lexer.Token

data class Identifier(
    val token: Token
) : ASTNode()
