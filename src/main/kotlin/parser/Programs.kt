package org.example.parser

sealed class Program : ASTNode()

data class SimpleProgram(
    val functionDefinition: FunctionDefinition
) : Program()
