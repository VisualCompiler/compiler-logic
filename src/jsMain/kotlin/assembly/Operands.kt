package assembly

sealed class Operand : AsmConstruct()

data class Imm(
    val value: Int
) : Operand()

data class Register(
    val name: HardwareRegister
) : Operand()

data class Pseudo(
    val name: String
) : Operand()

data class Stack(
    val offset: Int
) : Operand()

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
