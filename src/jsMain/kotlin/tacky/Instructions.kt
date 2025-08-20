package org.example.tacky
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

sealed class TackyInstruction : TackyConstruct()

data class TackyRet(
    val value: TackyVal
) : TackyInstruction() {
    override fun toJsonString(): String {
        val children =
            JsonObject(
                mapOf(
                    "value" to JsonPrimitive(value.toJsonString())
                )
            )
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("value"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }
}

enum class TackyUnaryOP {
    COMPLEMENT,
    NEGATE
}

data class TackyUnary(
    val operator: TackyUnaryOP,
    val src: TackyVal,
    val dest: TackyVar
) : TackyInstruction() {
    override fun toJsonString(): String {
        val children =
            JsonObject(
                mapOf(
                    "operator" to JsonPrimitive(operator.name),
                    "src" to JsonPrimitive(src.toJsonString()),
                    "dest" to JsonPrimitive(dest.toJsonString())
                )
            )
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("op, src, dest"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }
}

enum class TackyBinaryOP {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    REMAINDER
}

data class TackyBinary(
    val operator: TackyBinaryOP,
    val src1: TackyVal,
    val src2: TackyVal,
    val dest: TackyVar
) : TackyInstruction() {
    override fun toJsonString(): String {
        val children =
            JsonObject(
                mapOf(
                    "operator" to JsonPrimitive(operator.name),
                    "src1" to JsonPrimitive(src1.toJsonString()),
                    "src2" to JsonPrimitive(src2.toJsonString()),
                    "dest" to JsonPrimitive(dest.toJsonString())
                )
            )
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("op, src1, src2, dest"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }
}
