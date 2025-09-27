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
                val optimizedInstructions = block.instructions.mapNotNull { foldInstruction(it) }
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
