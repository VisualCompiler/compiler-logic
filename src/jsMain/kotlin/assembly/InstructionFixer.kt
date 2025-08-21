package assembly

class InstructionFixer {
    fun fix(
        program: AsmProgram,
        stackSpace: Int
    ): Program {
        var instructions = program.function.body

        // Rewrite invalid Mov instructions (Stack -> Stack)
        val fixedMovInstructions =
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
                } else {
                    listOf(instruction)
                }
            }

        val finalInstructions =
            if (stackSpace > 0) {
                // Remove the placeholder Ret instructions, as the epilogue handles it
                val body = fixedMovInstructions.filterNot { it is Ret }
                listOf(AllocateStack(stackSpace)) + body
            } else {
                fixedMovInstructions.filterNot { it is Ret }
            }

        val newFunction = program.function.copy(body = finalInstructions)
        return AsmProgram(newFunction)
    }
}
