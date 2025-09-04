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
    val x64Name: String,
    val x32Name: String,
    val x8Name: String
) {
    EAX("RAX", "eax", "al"),
    EDX("RDX", "edx", "dl"),
    R10D("R10", "r10d", "r10b"),
    R11D("R11", "r11d", "r11b"),
    EDI("RDI", "edi", "dil"),
    ESI("RSI", "esi", "sil"),
    ECX("RCX", "ecx", "cl"),
    R8D("R8", "r8d", "r8b"),
    R9D("R9", "r9d", "r9b")
}
