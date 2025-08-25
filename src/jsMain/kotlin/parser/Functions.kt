package parser

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

sealed class FunctionDefinition : ASTNode()

data class SimpleFunction(
    val name: String,
    val body: Statement
) : FunctionDefinition() {
    override fun prettyPrint(indent: Int): String =
        buildString {
            appendLine("${indent(indent)}SimpleFunction(")
            append("${indent(indent + 1)}name=$name")
            appendLine("${indent(indent + 1)}body=")
            append("${indent(indent)}${body.prettyPrint(indent + 1)}")
            appendLine("${indent(indent)})")
        }

    override fun toJsonString(): String {
        val children =
            JsonObject(
                mapOf(
                    "name" to JsonPrimitive(name),
                    "body" to JsonPrimitive(body.toJsonString())
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("'main', body'"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
