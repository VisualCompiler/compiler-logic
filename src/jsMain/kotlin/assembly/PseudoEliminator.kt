package assembly

class PseudoEliminator {
    fun eliminate(program: AsmProgram): AsmProgram {
        program.functions.forEach { function ->
            eliminateInFunction(function)
        }
        return program
    }

    private fun eliminateInFunction(function: AsmFunction) {
        val pseudoToOffset = mutableMapOf<String, Int>()
        var nextAvailableOffset = 0

        fun getStackLocation(name: String): Stack =
            pseudoToOffset[name]?.let { offset ->
                Stack(offset)
            } ?: run {
                nextAvailableOffset -= 8
                pseudoToOffset[name] = nextAvailableOffset
                Stack(nextAvailableOffset)
            }

        fun replace(operand: Operand): Operand = if (operand is Pseudo) getStackLocation(operand.name) else operand

        val newInstructions =
            function.body.map { instruction ->
                when (instruction) {
                    is Mov -> Mov(replace(instruction.src), replace(instruction.dest))
                    is AsmUnary -> AsmUnary(instruction.op, replace(instruction.dest))
                    is AsmBinary -> AsmBinary(instruction.op, replace(instruction.src), replace(instruction.dest))
                    is Cmp -> Cmp(replace(instruction.src), replace(instruction.dest))
                    is SetCC -> SetCC(instruction.condition, replace(instruction.dest))
                    is Push -> Push(replace(instruction.operand))
                    is Call -> instruction
                    is Idiv -> Idiv(replace(instruction.divisor))
                    else -> instruction
                }
            }

        function.body = newInstructions
        function.stackSize = -nextAvailableOffset
    }
}
