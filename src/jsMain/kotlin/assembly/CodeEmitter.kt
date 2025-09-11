package assembly
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CodeEmitter {
    // TODO make it based on the device instead hard coded
    private val useLinuxPrefix = true

    @kotlinx.serialization.Serializable
    data class RawInstruction(val code: String, val sourceId: String?)

    @kotlinx.serialization.Serializable
    data class RawFunction(
        val name: String,
        val body: List<RawInstruction>,
        val stackSize: Int
    )
    fun emit(program: AsmProgram): String = program.functions.joinToString("\n\n") { emitFunction(it) }

    fun emitRaw(program: AsmProgram): String = program.functions.joinToString("\n\n") { emitFunctionRaw(it) }
    private fun emitFunction(function: AsmFunction): String {
        val functionName = formatLabel(function.name)
        val bodyAsm = function.body.joinToString("\n") { emitInstruction(it) }

        return buildString {
            appendLine("  .globl $functionName")
            appendLine("$functionName:")
            appendLine("  push rbp")
            appendLine("  mov rbp, rsp")
            if (bodyAsm.isNotEmpty()) {
                appendLine(bodyAsm)
            }
            appendLine("  mov rsp, rbp")
            appendLine("  pop rbp")
            append("  ret")
        }
    }

    private fun emitFunctionRaw(function: AsmFunction): String {
        val bodyRaw = function.body.map { emitInstructionRaw(it) }
        val rawFunc = RawFunction(function.name, bodyRaw, function.stackSize)
        return Json.encodeToString(rawFunc) // returns valid JSON
    }

    private fun emitInstructionRaw(instruction: Instruction): RawInstruction {
        val indent = "  "
        return when (instruction) {
            is Call -> RawInstruction("call ${formatLabel(instruction.identifier)}", instruction.sourceId.toString())
            is Push -> {
                val operand = emitOperand(instruction.operand, size = OperandSize.QUAD)
                RawInstruction("push $operand", instruction.sourceId.toString())
            }
            is DeAllocateStack -> RawInstruction("addq rsp, ${instruction.size}", instruction.sourceId.toString())
            is Mov -> RawInstruction("mov ${emitOperand(instruction.dest)}, ${emitOperand(instruction.src)}", instruction.sourceId.toString())
            is AsmUnary -> RawInstruction("${instruction.op.text} ${emitOperand(instruction.dest)}", instruction.sourceId.toString())
            is AsmBinary -> RawInstruction("${instruction.op.text} ${emitOperand(instruction.dest)}, ${emitOperand(instruction.src)}", instruction.sourceId.toString())
            is Cmp -> RawInstruction("cmp ${emitOperand(instruction.dest)}, ${emitOperand(instruction.src)}", instruction.sourceId.toString())
            is Idiv -> RawInstruction("idiv ${emitOperand(instruction.divisor)}", instruction.sourceId.toString())
            is AllocateStack -> RawInstruction("subq rsp, ${instruction.size}", instruction.sourceId.toString())
            is Cdq -> RawInstruction("cdq", instruction.sourceId.toString())
            is Label -> RawInstruction("${formatLabel(instruction.name)}:", instruction.sourceId.toString())
            is Jmp -> RawInstruction("jmp ${formatLabel(instruction.label.name)}", instruction.sourceId.toString())
            is JmpCC -> RawInstruction("j${instruction.condition.text} ${formatLabel(instruction.label.name)}", instruction.sourceId.toString())
            is SetCC -> {
                val destOperand = emitOperand(instruction.dest, size = OperandSize.BYTE)
                RawInstruction("set${instruction.condition.text} $destOperand", instruction.sourceId.toString())
            }
            is Ret -> RawInstruction("ret", instruction.sourceId.toString())
        }
    }

    private fun emitInstruction(instruction: Instruction): String {
        val indent = "  "
        return when (instruction) {
            is Call -> "${indent}call ${formatLabel(instruction.identifier)}"
            is Push -> {
                val operand = emitOperand(instruction.operand, size = OperandSize.QUAD)
                "${indent}push $operand"
            }
            is DeAllocateStack -> "${indent}addq rsp, ${instruction.size}"
            is Mov -> "${indent}mov ${emitOperand(instruction.dest)}, ${emitOperand(instruction.src)}"
            is AsmUnary -> "${indent}${instruction.op.text} ${emitOperand(instruction.dest)}"
            is AsmBinary -> "${indent}${instruction.op.text} ${emitOperand(instruction.dest)}, ${emitOperand(instruction.src)}"
            is Cmp -> "${indent}cmp ${emitOperand(instruction.dest)}, ${emitOperand(instruction.src)}"
            is Idiv -> "${indent}idiv ${emitOperand(instruction.divisor)}"
            is AllocateStack -> "${indent}subq rsp, ${instruction.size}"
            is Cdq -> "${indent}cdq"
            is Label -> formatLabel(instruction.name) + ":"
            is Jmp -> "${indent}jmp ${formatLabel(instruction.label.name)}"
            is JmpCC -> "${indent}j${instruction.condition.text} ${formatLabel(instruction.label.name)}"
            is SetCC -> {
                val destOperand = emitOperand(instruction.dest, size = OperandSize.BYTE)
                "${indent}set${instruction.condition.text} $destOperand"
            }
            is Ret -> ""
        }
    }

    private fun emitOperandRaw(operand: Operand): String {
        return when (operand) {
            is Imm -> "Imm(value=${operand.value})"
            is Stack -> "Stack(offset=${operand.offset})"
            is Register -> "Register(name=${operand.name})"
            is Pseudo -> "Pseudo(name=${operand.name})"
        }
    }

    private enum class OperandSize { BYTE, LONG, QUAD }

    private fun emitOperand(
        operand: Operand,
        size: OperandSize = OperandSize.QUAD
    ): String =
        when (operand) {
            is Imm -> "${operand.value}"
            is Stack -> "qword ptr[rbp ${operand.offset}]"
            is Register ->
                when (size) {
                    OperandSize.QUAD -> operand.name.x64Name.lowercase()
                    OperandSize.LONG -> operand.name.x32Name.lowercase()
                    OperandSize.BYTE -> operand.name.x8Name.lowercase()
                }
            is Pseudo -> throw IllegalStateException("Cannot emit assembly with pseudo-register '${operand.name}'")
        }

    private fun formatLabel(name: String): String =
        if (name.startsWith(".L")) { // It's an auto-generated local label
            if (useLinuxPrefix) name else name.substring(1)
        } else { // It's a user-defined function name
            if (useLinuxPrefix) name else "_$name"
        }
}
