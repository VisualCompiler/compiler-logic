package optimizations

import tacky.JumpIfNotZero
import tacky.JumpIfZero
import tacky.TackyBinary
import tacky.TackyBinaryOP
import tacky.TackyConstant
import tacky.TackyCopy
import tacky.TackyInstruction
import tacky.TackyJump
import tacky.TackyLabel
import tacky.TackyUnary
import tacky.TackyUnaryOP
import tacky.TackyVal

class ConstantFolding : Optimization() {
    override val optimizationType = OptimizationType.B_CONSTANT_FOLDING

    override fun apply(cfg: ControlFlowGraph): ControlFlowGraph {
        val optimizedBlocks =
            cfg.blocks.map { block ->
                val optimizedInstructions =
                    block.instructions.mapNotNull { instruction ->
                        val folded = foldInstruction(instruction)
                        if (folded == null && (instruction is JumpIfZero || instruction is JumpIfNotZero)) {
                            // Remove the edge to the target label since the condition is always false (folded = null)
                            val target =
                                when (instruction) {
                                    is JumpIfZero -> instruction.target
                                    is JumpIfNotZero -> instruction.target
                                    else -> null
                                }
                            val labelBlock = cfg.blocks.find { it.instructions.first() == target }
                            if (labelBlock != null) {
                                block.successors.remove(labelBlock.id)
                                val edgeToRemove = cfg.edges.find { it.from.id == block.id && it.to.id == labelBlock.id }
                                if (edgeToRemove != null) {
                                    cfg.edges.remove(edgeToRemove)
                                }
                            }
                        } else if (folded is TackyJump) {
                            // Remove the edge to the next block if next block ≠ target block, since we're now jumping unconditionally to target
                            val nextBlock = cfg.blocks.find { it.id == block.id + 1 }
                            val target =
                                when (instruction) {
                                    is JumpIfZero -> instruction.target
                                    is JumpIfNotZero -> instruction.target
                                    else -> null
                                }
                            val labelBlock = cfg.blocks.find { it.instructions.first() == target }
                            if (nextBlock != null && nextBlock.id != labelBlock?.id) {
                                block.successors.remove(block.id + 1)
                                val edgeToRemove = cfg.edges.find { it.from.id == block.id && it.to.id == block.id + 1 }
                                if (edgeToRemove != null) {
                                    cfg.edges.remove(edgeToRemove)
                                }
                            }
                        }
                        folded
                    }
                block.copy(instructions = optimizedInstructions)
            }
        return cfg.copy(blocks = optimizedBlocks)
    }

    private fun foldInstruction(instruction: TackyInstruction): TackyInstruction? =
        when (instruction) {
            is TackyUnary -> foldUnary(instruction)
            is TackyBinary -> foldBinary(instruction)
            is JumpIfZero -> foldJump(instruction.condition, expectZero = true, target = instruction.target)
            is JumpIfNotZero -> foldJump(instruction.condition, expectZero = false, target = instruction.target)
            else -> instruction
        }

    private fun foldUnary(inst: TackyUnary): TackyInstruction {
        val src = inst.src as? TackyConstant ?: return inst
        val result =
            when (inst.operator) {
                TackyUnaryOP.COMPLEMENT -> src.value.inv()
                TackyUnaryOP.NEGATE -> -src.value
                TackyUnaryOP.NOT -> if (src.value == 0) 1 else 0
            }
        return TackyCopy(TackyConstant(result), inst.dest, inst.sourceId)
    }

    private fun foldBinary(inst: TackyBinary): TackyInstruction {
        val lhs = inst.src1 as? TackyConstant ?: return inst
        val rhs = inst.src2 as? TackyConstant ?: return inst

        val result =
            when (inst.operator) {
                TackyBinaryOP.ADD -> lhs.value + rhs.value
                TackyBinaryOP.SUBTRACT -> lhs.value - rhs.value
                TackyBinaryOP.MULTIPLY -> lhs.value * rhs.value
                TackyBinaryOP.DIVIDE -> if (rhs.value == 0) return inst else lhs.value / rhs.value
                TackyBinaryOP.REMAINDER -> if (rhs.value == 0) return inst else lhs.value % rhs.value
                TackyBinaryOP.LESS -> if (lhs.value < rhs.value) 1 else 0
                TackyBinaryOP.GREATER -> if (lhs.value > rhs.value) 1 else 0
                TackyBinaryOP.LESS_EQUAL -> if (lhs.value <= rhs.value) 1 else 0
                TackyBinaryOP.GREATER_EQUAL -> if (lhs.value >= rhs.value) 1 else 0
                TackyBinaryOP.EQUAL -> if (lhs.value == rhs.value) 1 else 0
                TackyBinaryOP.NOT_EQUAL -> if (lhs.value != rhs.value) 1 else 0
            }

        return TackyCopy(TackyConstant(result), inst.dest, inst.sourceId)
    }

    private fun foldJump(
        condition: TackyVal,
        expectZero: Boolean,
        target: TackyLabel
    ): TackyInstruction? {
        val constant =
            condition as? TackyConstant ?: return when (expectZero) {
                true -> JumpIfZero(condition, target)
                false -> JumpIfNotZero(condition, target)
            }
        return if ((constant.value == 0) == expectZero) {
            TackyJump(target)
        } else {
            null
        }
    }
}
