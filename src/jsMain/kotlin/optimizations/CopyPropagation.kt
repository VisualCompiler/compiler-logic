package optimizations

import tacky.JumpIfNotZero
import tacky.JumpIfZero
import tacky.TackyBinary
import tacky.TackyCopy
import tacky.TackyFunCall
import tacky.TackyInstruction
import tacky.TackyRet
import tacky.TackyUnary
import tacky.TackyVal
import tacky.TackyVar

class CopyPropagation : Optimization() {
    // A map to store the set of copies reaching the *entry* of each block.
    private lateinit var inSets: MutableMap<Int, Set<TackyCopy>>

    // A map to store the set of copies reaching the *exit* of each block.
    private lateinit var outSets: MutableMap<Int, Set<TackyCopy>>

    // A map to store which copies reach each specific instruction. This is needed for the final rewrite.
    private val instructionReachingCopies = mutableMapOf<TackyInstruction, Set<TackyCopy>>()

    override val optimizationType: OptimizationType = OptimizationType.A_COPY_PROPAGATION

    override fun apply(cfg: ControlFlowGraph): ControlFlowGraph {
        val newBlocks =
            cfg.blocks.map { block ->
                val newInstructions = mutableListOf<TackyInstruction>()
                val copyMap = mutableMapOf<String, TackyVal>()

                for (instr in block.instructions) {
                    when (instr) {
                        is TackyCopy -> {
                            if (instr.src is TackyVar && instr.src.name == instr.dest.name) {
                            } else {
                                val newSrc =
                                    if (instr.src is TackyVar && copyMap.containsKey(instr.src.name)) {
                                        copyMap[instr.src.name]!!
                                    } else {
                                        instr.src
                                    }

                                copyMap[instr.dest.name] = newSrc
                                newInstructions.add(TackyCopy(newSrc, instr.dest, instr.sourceId))
                            }
                        }
                        is TackyRet -> {
                            val newValue =
                                if (instr.value is TackyVar && copyMap.containsKey(instr.value.name)) {
                                    copyMap[instr.value.name]!!
                                } else {
                                    instr.value
                                }
                            newInstructions.add(TackyRet(newValue, instr.sourceId))
                        }
                        is TackyUnary -> {
                            val newSrc =
                                if (instr.src is TackyVar && copyMap.containsKey(instr.src.name)) {
                                    copyMap[instr.src.name]!!
                                } else {
                                    instr.src
                                }
                            copyMap.remove(instr.dest.name)
                            newInstructions.add(TackyUnary(instr.operator, newSrc, instr.dest, instr.sourceId))
                        }
                        is TackyBinary -> {
                            val newSrc1 =
                                if (instr.src1 is TackyVar && copyMap.containsKey(instr.src1.name)) {
                                    copyMap[instr.src1.name]!!
                                } else {
                                    instr.src1
                                }
                            val newSrc2 =
                                if (instr.src2 is TackyVar && copyMap.containsKey(instr.src2.name)) {
                                    copyMap[instr.src2.name]!!
                                } else {
                                    instr.src2
                                }
                            copyMap.remove(instr.dest.name)
                            newInstructions.add(TackyBinary(instr.operator, newSrc1, newSrc2, instr.dest, instr.sourceId))
                        }
                        is TackyFunCall -> {
                            val newArgs =
                                instr.args.map { arg ->
                                    if (arg is TackyVar && copyMap.containsKey(arg.name)) {
                                        copyMap[arg.name]!!
                                    } else {
                                        arg
                                    }
                                }
                            copyMap.remove(instr.dest.name)
                            newInstructions.add(TackyFunCall(instr.funName, newArgs, instr.dest, instr.sourceId))
                        }
                        is JumpIfZero -> {
                            val newCondition =
                                if (instr.condition is TackyVar && copyMap.containsKey(instr.condition.name)) {
                                    copyMap[instr.condition.name]!!
                                } else {
                                    instr.condition
                                }
                            newInstructions.add(JumpIfZero(newCondition, instr.target, instr.sourceId))
                        }
                        is JumpIfNotZero -> {
                            val newCondition =
                                if (instr.condition is TackyVar && copyMap.containsKey(instr.condition.name)) {
                                    copyMap[instr.condition.name]!!
                                } else {
                                    instr.condition
                                }
                            newInstructions.add(JumpIfNotZero(newCondition, instr.target, instr.sourceId))
                        }
                        else -> {
                            newInstructions.add(instr)
                        }
                    }
                }
                block.copy(instructions = newInstructions)
            }

        return cfg.copy(blocks = newBlocks)
    }

    private fun runAnalysis(cfg: ControlFlowGraph) {
        outSets = mutableMapOf()
        instructionReachingCopies.clear()

        val allCopies =
            cfg.blocks
                .flatMap { it.instructions }
                .filterIsInstance<TackyCopy>()
                .toSet()

        val worklist = cfg.blocks.toMutableList()
        cfg.blocks.forEach {
            outSets[it.id] = allCopies
        }

        while (worklist.isNotEmpty()) {
            val block = worklist.removeAt(0)
            val inSet = meet(block, allCopies)
            val newOut = transfer(block, inSet)

            if (newOut != outSets[block.id]) {
                outSets[block.id] = newOut
                block.successors.forEach { succId ->
                    cfg.blocks.find { it.id == succId }?.let { successorBlock ->
                        if (!worklist.contains(successorBlock)) {
                            worklist.add(successorBlock)
                        }
                    }
                }
            }
        }
    }

    private fun meet(
        block: Block,
        allCopies: Set<TackyCopy>
    ): Set<TackyCopy> {
        if (block.predecessors.all { it == 0 }) {
            return emptySet<TackyCopy>()
        }

        var incomingCopies: Set<TackyCopy> = allCopies

        for (predId in block.predecessors) {
            val predOutSet = outSets[predId]
            if (predOutSet != null) {
                incomingCopies = incomingCopies.intersect(predOutSet)
            }
        }
        return incomingCopies
    }

    private fun transfer(
        block: Block,
        inSet: Set<TackyCopy>
    ): Set<TackyCopy> {
        var currentCopies = inSet.toMutableSet()
        for (instruction in block.instructions) {
            instructionReachingCopies[instruction] = currentCopies.toSet()
            val toRemove = mutableSetOf<TackyCopy>()
            when (instruction) {
                is TackyCopy -> {
                    val destVar = instruction.dest.name
                    // Kill all previous copies to or from the destination variable.
                    currentCopies.forEach { if (it.src is TackyVar && it.src.name == destVar || it.dest.name == destVar) toRemove.add(it) }
                    currentCopies.removeAll(toRemove)
                    currentCopies.add(instruction)
                }
                is TackyUnary -> {
                    val destVar = instruction.dest.name
                    // Kill any copies to or from the destination variable.
                    currentCopies.forEach { if (it.src is TackyVar && it.src.name == destVar || it.dest.name == destVar) toRemove.add(it) }
                    currentCopies.removeAll(toRemove)
                }
                is TackyBinary -> {
                    val destVar = instruction.dest.name
                    // Kill any copies to or from the destination variable.
                    currentCopies.forEach { if (it.src is TackyVar && it.src.name == destVar || it.dest.name == destVar) toRemove.add(it) }
                    currentCopies.removeAll(toRemove)
                }
                is TackyFunCall -> {
                    val destVar = instruction.dest.name
                    // Kill any copies to or from the destination variable.
                    currentCopies.forEach { if (it.src is TackyVar && it.src.name == destVar || it.dest.name == destVar) toRemove.add(it) }
                    currentCopies.removeAll(toRemove)
                }
                else -> {}
            }
        }
        return currentCopies
    }

    private fun rewrite(
        instruction: TackyInstruction,
        reaching: Set<TackyCopy>
    ): TackyInstruction {
        val substitutionMap = reaching.associate { it.dest.name to it.src }

        return when (instruction) {
            is TackyRet -> TackyRet(value = substitute(instruction.value, substitutionMap), instruction.sourceId)
            is TackyUnary ->
                TackyUnary(
                    operator = instruction.operator,
                    src = substitute(instruction.src, substitutionMap),
                    dest = instruction.dest,
                    instruction.sourceId
                )
            is TackyBinary ->
                TackyBinary(
                    operator = instruction.operator,
                    src1 = substitute(instruction.src1, substitutionMap),
                    src2 = substitute(instruction.src2, substitutionMap),
                    dest = instruction.dest,
                    instruction.sourceId
                )
            is TackyCopy ->
                TackyCopy(
                    src = substitute(instruction.src, substitutionMap),
                    dest = instruction.dest,
                    instruction.sourceId
                )
            is TackyFunCall ->
                TackyFunCall(
                    funName = instruction.funName,
                    args = instruction.args.map { substitute(it, substitutionMap) },
                    dest = instruction.dest,
                    instruction.sourceId
                )
            is JumpIfZero ->
                JumpIfZero(
                    condition = substitute(instruction.condition, substitutionMap),
                    target = instruction.target,
                    instruction.sourceId
                )
            is JumpIfNotZero ->
                JumpIfNotZero(
                    condition = substitute(instruction.condition, substitutionMap),
                    target = instruction.target,
                    instruction.sourceId
                )
            else -> instruction
        }
    }

    private fun substitute(
        value: TackyVal,
        substitutionMap: Map<String, TackyVal>
    ): TackyVal =
        if (value is TackyVar && substitutionMap.containsKey(value.name)) {
            substitutionMap.getValue(value.name)
        } else {
            value
        }
}
