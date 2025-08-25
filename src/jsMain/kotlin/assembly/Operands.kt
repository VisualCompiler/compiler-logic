package assembly

sealed class Operand : AsmConstruct()

data class Imm(
    val value: Int
) : Operand() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}$value"
}

data class Register(
    val name: HardwareRegister
) : Operand() {
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}${name.x32Name}"
}

data class Pseudo(
    val name: String
) : Operand() {
    // override fun toAsm(): String = throw IllegalStateException("Cannot emit assembly with pseudo-register '$name'")
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}"
}

data class Stack(
    val offset: Int
) : Operand() {
    // override fun toAsm(): String = "$offset(%rbp)"
    override fun toAsm(indentationLevel: Int): String = "${indent(indentationLevel)}$offset(%rbp)"
}

// Hardware Registers
enum class HardwareRegister(
    val x32Name: String,
    val x8Name: String
) {
    EAX("eax", "al"),
    EDX("edx", "dl"),
    R10D("r10d", "r10b"),
    R11D("r11d", "r11b")
}
