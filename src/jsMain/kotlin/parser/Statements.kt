package org.example.parser

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

sealed class Statement : ASTNode()

data class ReturnStatement(
    val expression: Expression
) : Statement() {
    override fun prettyPrint(indent: Int): String =
        buildString {
            appendLine("${indent(indent)}ReturnStatement(")
            appendLine(expression.prettyPrint(indent + 1))
            appendLine("${indent(indent)})")
        }

    override fun toJsonString(): String {
        val children = JsonObject(
            mapOf(
                "expression" to JsonPrimitive(expression.toJsonString())
            )
        )

        val jsonNode = JsonObject(
            mapOf(
                "type" to JsonPrimitive(this::class.simpleName),
                "label" to JsonPrimitive("return expression"),
                "children" to children
            )
        )

        return Json.encodeToString(jsonNode)
    }
}
