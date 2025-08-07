package org.example.wasm

import org.example.parser.ASTNode
import org.example.parser.BinaryExpression
import org.example.parser.Identifier
import org.example.parser.IntExpression
import org.example.parser.ReturnStatement
import org.example.parser.SimpleFunction
import org.example.parser.SimpleProgram
import org.example.parser.UnaryExpression

class CodeGenerator {
    fun generateWat(ast: ASTNode): List<String> {
        val wasmTree = visit(ast)
        return wasmTree.toWat().lines()
    }

    private fun visit(ast: ASTNode): WASMConstruct =
        when (ast) {
            is SimpleProgram ->
                SimpleModule(
                    visit(ast.functionDefinition) as WASMFunction,
                    line = ast.line,
                    column = ast.column
                )
            is SimpleFunction ->
                WASMFunction(
                    name = ast.name.value,
                    body = listOf(visit(ast.body) as Instruction),
                    line = ast.line,
                    column = ast.column
                )
            is ReturnStatement -> {
                Return(
                    visit(ast.expression) as Operand,
                    line = ast.line,
                    column = ast.column
                )
            }
            is IntExpression ->
                Imm(
                    ast.value,
                    line = ast.line,
                    column = ast.column
                )

            is Identifier ->
                WASMIdentifier(
                    ast.value,
                    line = ast.line,
                    column = ast.column
                )
            is UnaryExpression -> TODO()
            is BinaryExpression -> TODO()
        }
}
