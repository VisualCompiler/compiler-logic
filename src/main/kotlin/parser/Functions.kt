package org.example.parser

sealed class FunctionDefinition : ASTNode()

data class SimpleFunction(
    val name: Identifier,
    val body: Statement
) : FunctionDefinition()
