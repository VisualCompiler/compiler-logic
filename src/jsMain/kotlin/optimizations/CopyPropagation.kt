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
}
