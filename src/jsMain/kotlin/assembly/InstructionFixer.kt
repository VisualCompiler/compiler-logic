package assembly

class InstructionFixer {
    fun fix(program: AsmProgram): Program {
        program.functions.forEach { fixInFunction(it) }
        return program
    }

    private fun fixInFunction(function: AsmFunction) {
        val instructions = function.body

        val fixedInstructions =
            instructions.flatMap { instruction ->
                when {
                    // Idiv cannot take an immediate value directly.
                    instruction is Idiv && instruction.divisor is Imm -> {
                        listOf(
                            Mov(instruction.divisor, Register(HardwareRegister.R10D), instruction.sourceId),
                            Idiv(Register(HardwareRegister.R10D), instruction.sourceId)
                        )
                    }

                    instruction is Push && instruction.operand is Stack -> {
                        listOf(
                            Mov(instruction.operand, Register(HardwareRegister.EAX), instruction.sourceId), // Use a caller-saved register
                            Push(Register(HardwareRegister.EAX), instruction.sourceId)
                        )
                    }

                    instruction is Mov && instruction.src is Stack && instruction.dest is Stack -> {
                        listOf(
                            Mov(instruction.src, Register(HardwareRegister.R10D), instruction.sourceId),
                            Mov(Register(HardwareRegister.R10D), instruction.dest, instruction.sourceId)
                        )
                    }

                    // Binary operations (add, sub) cannot be memory-to-memory.
                    instruction is AsmBinary &&
                        instruction.op != AsmBinaryOp.MUL &&
                        instruction.src is Stack &&
                        instruction.dest is Stack -> {
                        listOf(
                            Mov(instruction.src, Register(HardwareRegister.R10D), instruction.sourceId),
                            AsmBinary(instruction.op, Register(HardwareRegister.R10D), instruction.dest, instruction.sourceId)
                        )
                    }

                    instruction is AsmBinary &&
                        instruction.op == AsmBinaryOp.MUL &&
                        instruction.dest is Stack -> {
                        listOf(
                            Mov(instruction.dest, Register(HardwareRegister.R11D), instruction.sourceId),
                            AsmBinary(instruction.op, instruction.src, Register(HardwareRegister.R11D), instruction.sourceId),
                            Mov(Register(HardwareRegister.R11D), instruction.dest, instruction.sourceId)
                        )
                    }

                    // `cmp` cannot be memory-to-memory.
                    instruction is Cmp && instruction.src is Stack && instruction.dest is Stack -> {
                        listOf(
                            Mov(instruction.src, Register(HardwareRegister.R10D), instruction.sourceId),
                            Cmp(Register(HardwareRegister.R10D), instruction.dest, instruction.sourceId)
                        )
                    }

                    // The destination of `cmp` cannot be an immediate.
                    instruction is Cmp && instruction.dest is Imm -> {
                        listOf(
                            Mov(instruction.dest, Register(HardwareRegister.R11D), instruction.sourceId),
                            Cmp(instruction.src, Register(HardwareRegister.R11D), instruction.sourceId)
                        )
                    }

                    else -> listOf(instruction)
                }
            }

        var stackSpace = function.stackSize

        if (stackSpace % 16 != 0) {
            stackSpace += 16 - (stackSpace % 16)
        }

        val finalInstructions =
            if (stackSpace > 0) {
                // Stack allocation is a function-level operation, not tied to a specific source instruction
                listOf(AllocateStack(stackSpace, "")) + fixedInstructions
            } else {
                fixedInstructions
            }

        function.body = finalInstructions
    }
}
