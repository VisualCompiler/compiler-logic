package tacky

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

sealed class TackyConstruct {
    abstract fun toJsonString(): String
}

sealed class TackyVal : TackyConstruct()

data class TackyConstant(
    val value: Int
) : TackyVal() {
    override fun toJsonString(): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive(value.toString())
                )
            )
        return Json.encodeToString(jsonNode)
    }
}

data class TackyVar(
    val name: String
) : TackyVal() {
    override fun toJsonString(): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive(name)
                )
            )
        return Json.encodeToString(jsonNode)
    }
}

data class TackyLabel(
    val name: String
) : TackyInstruction() {
    override fun toJsonString(): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive(name)
                )
            )
        return Json.encodeToString(jsonNode)
    }
}

data class TackyProgram(
    val function: TackyFunction
) : TackyConstruct() {
    override fun toJsonString(): String {
        val children =
            JsonObject(
                mapOf(
                    "function" to Json.parseToJsonElement(function.toJsonString())
                )
            )
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("function"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }
}

data class TackyFunction(
    val name: String,
    val body: List<TackyInstruction>
) : TackyConstruct() {
    override fun toJsonString(): String {
        // Map each instruction in the body to its JSON string
        val bodyAsJsonElements = body.map { Json.parseToJsonElement(it.toJsonString()) }
        val bodyAsJsonArray = JsonArray(bodyAsJsonElements)

        val children =
            JsonObject(
                mapOf(
                    "name" to JsonPrimitive(name),
                    "body" to bodyAsJsonArray // Embed the array of JsonElements
                )
            )
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("name, body"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }
}
