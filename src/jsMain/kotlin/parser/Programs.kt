package parser

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

sealed class Program : ASTNode()

data class SimpleProgram(
    val functionDefinition: FunctionDefinition
) : Program() {
    override fun prettyPrint(indent: Int): String =
        buildString {
            appendLine("${indent(indent)}SimpleProgram(")
            append(functionDefinition.prettyPrint(indent + 1))
            appendLine("${indent(indent)})")
        }

    override fun toJsonString(): String {
        val children =
            JsonObject(
                mapOf(
                    "functionDefinition" to JsonPrimitive(functionDefinition.toJsonString())
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("function definition"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
}
