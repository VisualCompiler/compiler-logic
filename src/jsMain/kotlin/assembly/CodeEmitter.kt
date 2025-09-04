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
            appendLine("  pushq %rbp")
            appendLine("  movq %rsp, %rbp")
            if (bodyAsm.isNotEmpty()) {
                appendLine(bodyAsm)
            }
            appendLine("  movq %rbp, %rsp")
            appendLine("  popq %rbp")
            append("  ret")
        }
    }

    private fun emitInstruction(instruction: Instruction): String {
        val indent = "  "
        return when (instruction) {
            is Call -> "${indent}call ${formatLabel(instruction.identifier)}"
            is Push -> {
                val operand = emitOperand(instruction.operand, size = OperandSize.QUAD)
                "${indent}pushq $operand"
            }
            is DeAllocateStack -> "${indent}addq $${instruction.size}, %rsp"

            is Mov -> "${indent}movl ${emitOperand(instruction.src)}, ${emitOperand(instruction.dest)}"
            is AsmUnary -> "${indent}${instruction.op.text} ${emitOperand(instruction.dest)}"
            is AsmBinary -> "${indent}${instruction.op.text} ${emitOperand(instruction.src)}, ${emitOperand(instruction.dest)}"
            is Cmp -> "${indent}cmpl ${emitOperand(instruction.src)}, ${emitOperand(instruction.dest)}"
            is Idiv -> "${indent}idivl ${emitOperand(instruction.divisor)}"
            is AllocateStack -> "${indent}subq $${instruction.size}, %rsp"
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
        size: OperandSize = OperandSize.LONG
    ): String =
        when (operand) {
            is Imm -> "$${operand.value}"
            is Stack -> "${operand.offset}(%rbp)"
            is Register ->
                when (size) {
                    OperandSize.QUAD -> "%${operand.name.x64Name}"
                    OperandSize.LONG -> "%${operand.name.x32Name}"
                    OperandSize.BYTE -> "%${operand.name.x8Name}"
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
