package assembly

class InstructionFixer {
    fun fix(
        program: AsmProgram,
        stackSpace: Int
    ): Program {
        val instructions = program.function.body

        // Rewrite invalid Mov instructions (Stack -> Stack)
        val fixedInstructions =
            instructions.flatMap { instruction ->
                if (instruction is Mov && instruction.src is Stack && instruction.dest is Stack) {
                    listOf(
                        Mov(instruction.src, Register(HardwareRegister.R10D)),
                        Mov(Register(HardwareRegister.R10D), instruction.dest)
                    )
                } else if (instruction is AsmBinary && instruction.src is Stack && instruction.dest is Stack) {
                    listOf(
                        Mov(instruction.src, Register(HardwareRegister.R10D)),
                        AsmBinary(instruction.op, Register(HardwareRegister.R10D), instruction.dest)
                    )
                } else if (instruction is Cmp && instruction.src is Stack && instruction.dest is Stack) {
                    listOf(
                        Mov(instruction.src, Register(HardwareRegister.R10D)),
                        Cmp(Register(HardwareRegister.R10D), instruction.dest)
                    )
                } else if (instruction is Cmp && instruction.dest is Imm) {
                    listOf(
                        Mov(instruction.dest, Register(HardwareRegister.R11D)),
                        Cmp(instruction.src, Register(HardwareRegister.R11D))
                    )
                } else {
                    listOf(instruction)
                }
            }

        val finalInstructions =
            if (stackSpace > 0) {
                // Remove the placeholder Ret instructions, as the epilogue handles it
                val body = fixedInstructions.filterNot { it is Ret }
                listOf(AllocateStack(stackSpace)) + body
            } else {
                fixedInstructions.filterNot { it is Ret }
            }

        val newFunction = program.function.copy(body = finalInstructions)
        return AsmProgram(newFunction)
    }
}
