package optimizations

import tacky.TackyProgram

enum class OptimizationType {
    CONSTANT_FOLDING,
    DEAD_STORE_ELIMINATION,

    // UNREACHABLE_CODE_ELIMINATION,
    COPY_PROPAGATION
}

sealed class Optimization {
    abstract val optimizationType: OptimizationType
    abstract fun apply(cfg: ControlFlowGraph): ControlFlowGraph
}

object OptimizationManager {
    private fun createOptimization(type: OptimizationType): Optimization = when (type) {
        OptimizationType.CONSTANT_FOLDING -> ConstantFolding()
        OptimizationType.DEAD_STORE_ELIMINATION -> DeadStoreElimination()
        // OptimizationType.UNREACHABLE_CODE_ELIMINATION -> UnreachableCodeElimination()
        OptimizationType.COPY_PROPAGATION -> CopyPropagation()
    }

    fun optimizeProgram(program: TackyProgram, enabledOptimizations: Set<OptimizationType>): TackyProgram {
        val optimizedFunctions = program.functions.map { function ->
            if (function.body.isEmpty()) {
                function
            } else {
                val cfg = ControlFlowGraph().construct(function.name, function.body)
                val optimizedCfg = applyOptimizations(cfg, enabledOptimizations)
                val optimizedInstructions = optimizedCfg.toInstructions()
                function.copy(body = optimizedInstructions)
            }
        }
        return program.copy(functions = optimizedFunctions)
    }

    fun applyOptimizations(cfg: ControlFlowGraph, enabledOptimizations: Set<OptimizationType>): ControlFlowGraph {
        var currentCfg = cfg

        while (true) {
            val previousInstructions = currentCfg.toInstructions()

            for (optimizationType in enabledOptimizations) {
                val optimization = createOptimization(optimizationType)
                currentCfg = optimization.apply(currentCfg)
            }

            val optimizedInstructions = currentCfg.toInstructions()

            if (optimizedInstructions == previousInstructions || optimizedInstructions.isEmpty()) {
                break
            }
        }

        return currentCfg
    }
}
