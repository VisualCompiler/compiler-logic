package org.example.parser

sealed class Statement : ASTNode()

data class ReturnStatement(
    val expression: Expression
) : Statement()
