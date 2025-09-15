package tacky

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class TackyConstruct() {
    abstract fun toPseudoCode(indentationLevel: Int = 0): String

    protected fun indent(level: Int): String = "  ".repeat(level)
}

@Serializable
sealed class TackyVal() : TackyConstruct()

@Serializable
@SerialName("TackyConstant")
data class TackyConstant(
    val value: Int
) : TackyVal() {
    override fun toPseudoCode(indentationLevel: Int): String = value.toString()
}

@Serializable
@SerialName("TackyVar")
data class TackyVar(
    val name: String
) : TackyVal() {
    override fun toPseudoCode(indentationLevel: Int): String = name
}

@Serializable
@SerialName("TackyProgram")
data class TackyProgram(
    val functions: List<TackyFunction>
) : TackyConstruct() {
    override fun toPseudoCode(indentationLevel: Int): String = functions.joinToString("\n\n") { it.toPseudoCode(indentationLevel) }
}

@Serializable
@SerialName("TackyFunction")
data class TackyFunction(
    val name: String,
    val args: List<String>,
    var body: List<TackyInstruction>,
    val sourceId: String = ""

) : TackyConstruct() {
    override fun toPseudoCode(indentationLevel: Int): String {
        val paramString = args.joinToString(", ")
        val bodyAsCode = body.joinToString("\n") { it.toPseudoCode(indentationLevel + 1) }
        return buildString {
            appendLine("${indent(indentationLevel)}def $name($paramString):")
            append(bodyAsCode)
        }
    }
}
