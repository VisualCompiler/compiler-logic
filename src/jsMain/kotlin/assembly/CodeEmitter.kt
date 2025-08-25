package assembly

class CodeEmitter {
    // TODO make it based on the device instead hard coded
    private val useLinuxPrefix = true

    fun emit(program: AsmProgram): String {
        val function = program.function

        val functionName = if (useLinuxPrefix) function.name else "_${function.name}"

        val bodyAsm = function.body.joinToString("\n") { emitInstruction(it) }

        // add prologue and epilogue
        return buildString {
            appendLine("  .globl $functionName")
            appendLine("$functionName:")
            appendLine("  pushq %rbp")
            appendLine("  movq %rsp, %rbp")
            if (bodyAsm.isNotEmpty()) {
                appendLine(bodyAsm)
            }
            appendLine("  popq %rbp")
            appendLine("  ret")
        }
    }

    private fun emitInstruction(instruction: Instruction): String {
        val indent = "  "
        return when (instruction) {
            // Standard instructions with 4-byte operands
            is Mov -> "${indent}movl ${emitOperand(instruction.src)}, ${emitOperand(instruction.dest)}"
            is AsmUnary -> "${indent}${instruction.op.text} ${emitOperand(instruction.dest)}"
            is AsmBinary -> "${indent}${instruction.op.text} ${emitOperand(instruction.src)}, ${emitOperand(instruction.dest)}"
            is Cmp -> "${indent}cmpl ${emitOperand(instruction.src)}, ${emitOperand(instruction.dest)}"
            is Idiv -> "${indent}idivl ${emitOperand(instruction.divisor)}"
            is AllocateStack -> "${indent}subq $${instruction.size}, %rsp"
            is Cdq -> "${indent}cdq"

            // Labels and Jumps
            is Label -> formatLabel(instruction.name) + ":"
            is Jmp -> "${indent}jmp ${formatLabel(instruction.label.name)}"
            is JmpCC -> "${indent}j${getConditionSuffix(instruction.condition)} ${formatLabel(instruction.label.name)}"

            is SetCC -> {
                val destOperand = emitOperand(instruction.dest, size = OperandSize.BYTE)
                "${indent}set${getConditionSuffix(instruction.condition)} $destOperand"
            }

            // Ret is handled by the main epilogue
            is Ret -> ""

            else -> throw NotImplementedError("Emission for ${instruction::class.simpleName} not implemented.")
        }
    }

    private enum class OperandSize { BYTE, LONG }

    private fun emitOperand(
        operand: Operand,
        size: OperandSize = OperandSize.LONG
    ): String =
        when (operand) {
            is Imm -> "$${operand.value}"
            is Stack -> "${operand.offset}(%rbp)"
            is Register ->
                when (size) {
                    OperandSize.LONG -> "%${operand.name.x32Name}"
                    OperandSize.BYTE -> "%${operand.name.x8Name}"
                }
            is Pseudo -> throw IllegalStateException("Cannot emit assembly with pseudo-register '${operand.name}'")
        }

    private fun formatLabel(name: String): String =
        if (name.startsWith(".L")) {
            if (useLinuxPrefix) name else name.substring(1)
        } else {
            name
        }

    private fun getConditionSuffix(condition: ConditionCode): String =
        when (condition) {
            ConditionCode.E -> "e"
            ConditionCode.NE -> "ne"
            ConditionCode.G -> "g"
            ConditionCode.GE -> "ge"
            ConditionCode.L -> "l"
            ConditionCode.LE -> "le"
        }
}
