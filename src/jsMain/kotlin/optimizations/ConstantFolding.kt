package optimizations

import tacky.JumpIfNotZero
import tacky.JumpIfZero
import tacky.TackyBinary
import tacky.TackyBinaryOP
import tacky.TackyConstant
import tacky.TackyCopy
import tacky.TackyInstruction
import tacky.TackyJump
import tacky.TackyUnary
import tacky.TackyUnaryOP

class ConstantFolding : Optimization() {
    override val optimizationType: OptimizationType = OptimizationType.CONSTANT_FOLDING

    override fun apply(
        cfg: ControlFlowGraph
    ): ControlFlowGraph {
        if (optimizationType != OptimizationType.CONSTANT_FOLDING) {
            return cfg
        }

        val optimizedBlocks = cfg.blocks.map { block ->
            val optimizedInstructions = block.instructions.mapNotNull { instruction ->
                when (instruction) {
                    is TackyUnary -> foldUnary(instruction)
                    is TackyBinary -> foldBinary(instruction)
                    is JumpIfZero -> foldJumpIfZero(instruction)
                    is JumpIfNotZero -> foldJumpIfNotZero(instruction)
                    else -> instruction
                }
            }
            block.copy(instructions = optimizedInstructions)
        }

        return cfg.copy(blocks = optimizedBlocks)
    }

    private fun foldUnary(instruction: TackyUnary): TackyInstruction? {
        if (instruction.src !is TackyConstant) return instruction

        val result = try {
            when (instruction.operator) {
                TackyUnaryOP.COMPLEMENT -> instruction.src.value.inv()
                TackyUnaryOP.NEGATE -> -instruction.src.value
                TackyUnaryOP.NOT -> if (instruction.src.value == 0) 1 else 0
            }
        } catch (e: Exception) {
            // Handle overflow gracefully - undefined behavior
            return instruction
        }

        return TackyCopy(TackyConstant(result), instruction.dest)
    }

    private fun foldBinary(instruction: TackyBinary): TackyInstruction? {
        if (instruction.src1 !is TackyConstant || instruction.src2 !is TackyConstant) {
            return instruction
        }

        val result = try {
            when (instruction.operator) {
                TackyBinaryOP.ADD -> instruction.src1.value + instruction.src2.value
                TackyBinaryOP.SUBTRACT -> instruction.src1.value - instruction.src2.value
                TackyBinaryOP.MULTIPLY -> instruction.src1.value * instruction.src2.value
                TackyBinaryOP.DIVIDE -> {
                    if (instruction.src2.value == 0) {
                        // Division by zero - undefined behavior, keep original instruction
                        return instruction
                    }
                    instruction.src1.value / instruction.src2.value
                }
                TackyBinaryOP.REMAINDER -> {
                    if (instruction.src2.value == 0) {
                        // Division by zero - undefined behavior, keep original instruction
                        return instruction
                    }
                    instruction.src1.value % instruction.src2.value
                }
                TackyBinaryOP.LESS -> if (instruction.src1.value < instruction.src2.value) 1 else 0
                TackyBinaryOP.GREATER -> if (instruction.src1.value > instruction.src2.value) 1 else 0
                TackyBinaryOP.LESS_EQUAL -> if (instruction.src1.value <= instruction.src2.value) 1 else 0
                TackyBinaryOP.GREATER_EQUAL -> if (instruction.src1.value >= instruction.src2.value) 1 else 0
                TackyBinaryOP.EQUAL -> if (instruction.src1.value == instruction.src2.value) 1 else 0
                TackyBinaryOP.NOT_EQUAL -> if (instruction.src1.value != instruction.src2.value) 1 else 0
            }
        } catch (e: Exception) {
            // Handle overflow gracefully - undefined behavior
            return instruction
        }

        return TackyCopy(TackyConstant(result), instruction.dest)
    }

    private fun foldJumpIfZero(instruction: JumpIfZero): TackyInstruction? {
        if (instruction.condition !is TackyConstant) return instruction

        return if (instruction.condition.value == 0) {
            TackyJump(instruction.target)
        } else {
            null // Remove the instruction
        }
    }

    private fun foldJumpIfNotZero(instruction: JumpIfNotZero): TackyInstruction? {
        if (instruction.condition !is TackyConstant) return instruction

        return if (instruction.condition.value != 0) {
            TackyJump(instruction.target)
        } else {
            null // Remove the instruction
        }
    }
}
