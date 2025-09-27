package optimizations

import tacky.JumpIfNotZero
import tacky.JumpIfZero
import tacky.TackyBinary
import tacky.TackyCopy
import tacky.TackyFunCall
import tacky.TackyJump
import tacky.TackyLabel
import tacky.TackyRet
import tacky.TackyUnary
import tacky.TackyVar

class DeadStoreElimination : Optimization() {
    override val optimizationType: OptimizationType = OptimizationType.D_DEAD_STORE_ELIMINATION

    override fun apply(cfg: ControlFlowGraph): ControlFlowGraph {
        val liveness = LivenessAnalysis()
        val liveAfter = liveness.analyze(cfg)

        val optimizedBlocks =
            cfg.blocks.map { block ->
                val optimizedInstructions =
                    block.instructions
                        .withIndex()
                        .filterNot { (idx, instr) ->
                            when (instr) {
                                is TackyFunCall -> false
                                is TackyUnary, is TackyBinary, is TackyCopy -> {
                                    val live = liveAfter[block.id to idx] ?: emptySet()
                                    val dest =
                                        when (instr) {
                                            is TackyUnary -> instr.dest.name
                                            is TackyBinary -> instr.dest.name
                                            is TackyCopy -> instr.dest.name
                                            else -> ""
                                        }
                                    dest !in live
                                }
                                else -> false
                            }
                        }.map { it.value }
                block.copy(instructions = optimizedInstructions)
            }

        return cfg.copy(blocks = optimizedBlocks)
    }
}

class LivenessAnalysis {
    private val instructionAnnotations = mutableMapOf<Pair<Int, Int>, Set<String>>()
    private val blockAnnotations = mutableMapOf<Int, Set<String>>()

    fun analyze(cfg: ControlFlowGraph): Map<Pair<Int, Int>, Set<String>> {
        for (block in cfg.blocks) {
            blockAnnotations[block.id] = emptySet()
        }

        val workList = cfg.blocks.map { it.id }.toMutableList()
        while (workList.isNotEmpty()) {
            val blockId = workList.removeFirst()
            val block = cfg.blocks.find { it.id == blockId } ?: continue
            val out = meet(block, emptySet())
            val newIn = transfer(block, out)
            if (newIn != blockAnnotations[block.id]) {
                blockAnnotations[block.id] = newIn
                workList.addAll(block.predecessors)
            }
        }

        return instructionAnnotations
    }

    private fun transfer(
        block: Block,
        liveAfter: Set<String>,
        staticVariables: Set<String> = emptySet()
    ): Set<String> {
        val liveBefore = liveAfter.toMutableSet()
        block.instructions.withIndex().reversed().forEach { (idx, instruction) ->
            instructionAnnotations[block.id to idx] = liveBefore.toSet()
            when (instruction) {
                is TackyUnary -> {
                    liveBefore.remove(instruction.dest.name)
                    if (instruction.src is TackyVar) liveBefore.add(instruction.src.name)
                }
                is TackyBinary -> {
                    liveBefore.remove(instruction.dest.name)
                    if (instruction.src1 is TackyVar) liveBefore.add(instruction.src1.name)
                    if (instruction.src2 is TackyVar) liveBefore.add(instruction.src2.name)
                }
                is TackyCopy -> {
                    liveBefore.remove(instruction.dest.name)
                    if (instruction.src is TackyVar) liveBefore.add(instruction.src.name)
                }
                is TackyFunCall -> {
                    liveBefore.remove(instruction.dest.name)
                    instruction.args.forEach { arg ->
                        if (arg is TackyVar) liveBefore.add(arg.name)
                    }
                }
                is TackyRet -> {
                    if (instruction.value is TackyVar) liveBefore.add(instruction.value.name)
                }
                is JumpIfZero -> {
                    if (instruction.condition is TackyVar) liveBefore.add(instruction.condition.name)
                }
                is JumpIfNotZero -> {
                    if (instruction.condition is TackyVar) liveBefore.add(instruction.condition.name)
                }
                is TackyJump, is TackyLabel -> {}
            }
        }
        return liveBefore
    }

    private fun meet(
        block: Block,
        allStaticVariables: Set<String>
    ): MutableSet<String> {
        val liveVariables = mutableSetOf<String>()
        for (suc in block.successors) {
            liveVariables.addAll(blockAnnotations.getOrElse(suc) { emptySet() })
        }
        return liveVariables
    }
}
