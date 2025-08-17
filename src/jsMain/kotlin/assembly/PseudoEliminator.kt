package org.example.assembly

import assembly.AsmProgram
import assembly.AsmUnary
import assembly.Mov
import assembly.Operand
import assembly.Pseudo
import assembly.Stack

class PseudoEliminator {
    data class EliminationResult(
        val program: AsmProgram,
        val stackSpaceUsed: Int
    )

    fun eliminate(program: AsmProgram): EliminationResult {
        val pseudoToOffset = mutableMapOf<String, Int>()
        var nextAvailableOffset = 0

        fun getStackLocation(name: String): Stack =
            if (pseudoToOffset.containsKey(name)) {
                Stack(pseudoToOffset.getValue(name))
            } else {
                nextAvailableOffset -= 4
                pseudoToOffset[name] = nextAvailableOffset
                Stack(nextAvailableOffset)
            }

        fun replace(operand: Operand): Operand = if (operand is Pseudo) getStackLocation(operand.name) else operand

        val newInstructions =
            program.function.body.map { instruction ->
                when (instruction) {
                    is Mov -> Mov(replace(instruction.src), replace(instruction.dest))
                    is AsmUnary -> AsmUnary(instruction.op, replace(instruction.dest))
                    else -> instruction
                }
            }

        val newFunction = program.function.copy(body = newInstructions)
        return EliminationResult(AsmProgram(newFunction), -nextAvailableOffset)
    }
}
