package org.example.parser

import lexer.Token

sealed class Expression : ASTNode()

data class IntExpression(
    val value: Token
) : Expression()
