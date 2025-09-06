package assembly

class CodeEmitter {
    // TODO make it based on the device instead hard coded
    private val useLinuxPrefix = true

    fun emit(program: AsmProgram): String = program.functions.joinToString("\n\n") { emitFunction(it) }

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

            else -> throw NotImplementedError("Emission for ${instruction::class.simpleName} not implemented.")
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
