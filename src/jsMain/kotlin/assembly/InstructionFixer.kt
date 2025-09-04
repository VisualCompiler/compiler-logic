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
                            Mov(instruction.divisor, Register(HardwareRegister.R10D)),
                            Idiv(Register(HardwareRegister.R10D))
                        )
                    }

                    instruction is Push && instruction.operand is Stack -> {
                        listOf(
                            Mov(instruction.operand, Register(HardwareRegister.EAX)), // Use a caller-saved register
                            Push(Register(HardwareRegister.EAX))
                        )
                    }

                    instruction is Mov && instruction.src is Stack && instruction.dest is Stack -> {
                        listOf(
                            Mov(instruction.src, Register(HardwareRegister.R10D)),
                            Mov(Register(HardwareRegister.R10D), instruction.dest)
                        )
                    }

                    // Binary operations (add, sub) cannot be memory-to-memory.
                    instruction is AsmBinary &&
                        instruction.op != AsmBinaryOp.MUL &&
                        instruction.src is Stack &&
                        instruction.dest is Stack -> {
                        listOf(
                            Mov(instruction.src, Register(HardwareRegister.R10D)),
                            AsmBinary(instruction.op, Register(HardwareRegister.R10D), instruction.dest)
                        )
                    }

                    instruction is AsmBinary &&
                        instruction.op == AsmBinaryOp.MUL &&
                        instruction.dest is Stack -> {
                        listOf(
                            Mov(instruction.dest, Register(HardwareRegister.R11D)),
                            AsmBinary(instruction.op, instruction.src, Register(HardwareRegister.R11D)),
                            Mov(Register(HardwareRegister.R11D), instruction.dest)
                        )
                    }

                    // `cmp` cannot be memory-to-memory.
                    instruction is Cmp && instruction.src is Stack && instruction.dest is Stack -> {
                        listOf(
                            Mov(instruction.src, Register(HardwareRegister.R10D)),
                            Cmp(Register(HardwareRegister.R10D), instruction.dest)
                        )
                    }

                    // The destination of `cmp` cannot be an immediate.
                    instruction is Cmp && instruction.dest is Imm -> {
                        listOf(
                            Mov(instruction.dest, Register(HardwareRegister.R11D)),
                            Cmp(instruction.src, Register(HardwareRegister.R11D))
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
                listOf(AllocateStack(stackSpace)) + fixedInstructions
            } else {
                fixedInstructions
            }

        function.body = finalInstructions
    }
}
