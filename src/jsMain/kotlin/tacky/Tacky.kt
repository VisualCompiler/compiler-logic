package tacky

sealed class TackyConstruct {
    abstract fun toPseudoCode(indentationLevel: Int = 0): String

    protected fun indent(level: Int): String = "  ".repeat(level)
}

sealed class TackyVal : TackyConstruct()

data class TackyConstant(
    val value: Int
) : TackyVal() {
    override fun toPseudoCode(indentationLevel: Int): String = value.toString()
}

data class TackyVar(
    val name: String
) : TackyVal() {
    override fun toPseudoCode(indentationLevel: Int): String = name
}

data class TackyLabel(
    val name: String
) : TackyInstruction() {
    override fun toPseudoCode(indentationLevel: Int): String = "$name:"
}

data class TackyProgram(
    val functions: List<TackyFunction>
) : TackyConstruct() {
    override fun toPseudoCode(indentationLevel: Int): String = functions.joinToString("\n\n") { it.toPseudoCode(indentationLevel) }
}

data class TackyFunction(
    val name: String,
    val args: List<String>,
    val body: List<TackyInstruction>
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
