package org.example.parser

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import lexer.Token

sealed class Expression : ASTNode()

data class IntExpression(
    val value: Int
) : Expression() {
    override fun prettyPrint(indent: Int): String = "${indent(indent)}Int($value)"

    override fun toJsonString(): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive(value)
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class UnaryExpression(
    val operator: Token,
    val expression: Expression
) : Expression() {
    override fun prettyPrint(indent: Int): String =
        buildString {
            appendLine("${indent(indent)}UnaryExpression(operator='${operator.lexeme}')")
            append(expression.prettyPrint(indent + 1))
        }

    override fun toJsonString(): String {
        val children =
            JsonObject(
                mapOf(
                    "operator" to JsonPrimitive(operator.toString()),
                    "expression" to JsonPrimitive(expression.toJsonString())
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("operator, expression"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}

data class BinaryExpression(
    val left: Expression,
    val operator: Token,
    val right: Expression
) : Expression() {
    override fun prettyPrint(indent: Int): String =
        buildString {
            appendLine("${indent(indent)}BinaryExpression(operator='${operator.lexeme}')")
            append(left.prettyPrint(indent + 1))
            append(right.prettyPrint(indent + 1))
        }

    override fun toJsonString(): String {
        val children =
            JsonObject(
                mapOf(
                    "left" to JsonPrimitive(left.toJsonString()),
                    "operator" to JsonPrimitive(operator.toString()),
                    "right" to JsonPrimitive(right.toJsonString())
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("operator, left, right"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
