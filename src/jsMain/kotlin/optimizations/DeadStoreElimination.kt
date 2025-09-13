package optimizations

import tacky.JumpIfNotZero
import tacky.JumpIfZero
import tacky.TackyBinary
import tacky.TackyCopy
import tacky.TackyFunCall
import tacky.TackyInstruction
import tacky.TackyJump
import tacky.TackyLabel
import tacky.TackyRet
import tacky.TackyUnary
import tacky.TackyVar

class DeadStoreElimination : Optimization() {
    override val optimizationType: OptimizationType = OptimizationType.DEAD_STORE_ELIMINATION

    override fun apply(cfg: ControlFlowGraph): ControlFlowGraph {
        val livenessAnalysis = LivenessAnalysis()
        val liveVariables = livenessAnalysis.analyze(cfg)

        val optimizedBlocks = cfg.blocks.map { block ->
            val optimizedInstructions = block.instructions.withIndex().filterNot { (idx, instr) ->
                isDeadStore(block.id, idx, instr, liveVariables)
            }.map { it.value }

            block.copy(instructions = optimizedInstructions)
        }

        return cfg.copy(blocks = optimizedBlocks)
    }

    private fun isDeadStore(
        blockId: Int,
        idx: Int,
        instruction: TackyInstruction,
        liveVariables: Map<Pair<Int, Int>, Set<String>>
    ): Boolean {
        // Never eliminate function calls (side effects)
        if (instruction is TackyFunCall) return false

        // Only instructions with destinations are considered
        val dest = when (instruction) {
            is TackyUnary -> instruction.dest.name
            is TackyBinary -> instruction.dest.name
            is TackyCopy -> instruction.dest.name
            else -> return false
        }

        val liveAfter = liveVariables[blockId to idx] ?: emptySet()
        return dest !in liveAfter
    }
}

class LivenessAnalysis {
    fun analyze(cfg: ControlFlowGraph): Map<Pair<Int, Int>, Set<String>> {
        val allStaticVariables = extractStaticVariables(cfg)
        val blockOut = mutableMapOf<Int, Set<String>>()
        val worklist = ArrayDeque<Int>()

        // init: all blocks start with empty live-out
        cfg.blocks.forEach { block ->
            blockOut[block.id] = emptySet()
            worklist.add(block.id)
        }

        // backward fixpoint
        while (worklist.isNotEmpty()) {
            val currentId = worklist.removeFirst()
            val currentBlock = cfg.blocks.find { it.id == currentId } ?: continue

            val succLive = currentBlock.successors.flatMap { succId ->
                blockOut[succId] ?: emptySet()
            }.toSet()

            if (succLive != blockOut[currentId]) {
                blockOut[currentId] = succLive
                currentBlock.predecessors.forEach { worklist.add(it) }
            }
        }

        // instruction-level liveness
        val instructionLiveVars = mutableMapOf<Pair<Int, Int>, Set<String>>()

        cfg.blocks.forEach { block ->
            var live = blockOut[block.id] ?: emptySet()

            block.instructions.withIndex().reversed().forEach { (idx, instr) ->
                instructionLiveVars[block.id to idx] = live
                live = transfer(instr, live, allStaticVariables)
            }
        }

        return instructionLiveVars
    }

    private fun transfer(
        instruction: TackyInstruction,
        liveAfter: Set<String>,
        allStaticVariables: Set<String>
    ): Set<String> {
        val liveBefore = liveAfter.toMutableSet()

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
                liveBefore.addAll(allStaticVariables) // conservatively keep statics alive
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
            is TackyJump -> { /* no effect */ }
            is TackyLabel -> { /* no effect */ }
        }

        return liveBefore
    }

    private fun extractStaticVariables(cfg: ControlFlowGraph): Set<String> {
        // stub: no statics for now
        return emptySet()
    }
}
