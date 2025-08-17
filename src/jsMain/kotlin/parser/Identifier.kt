package org.example.parser

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

data class Identifier(
    val value: String
) : ASTNode() {
    override fun prettyPrint(indent: Int): String = buildString { appendLine("${indent(indent)}\"${this@Identifier.value}\"") }

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
