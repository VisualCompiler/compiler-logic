package optimizations

enum class OptimizationType {
    CONSTANT_FOLDING,
    DEAD_STORE_ELIMINATION,
    UNREACHABLE_CODE_ELIMINATION,
    COPY_PROPAGATION
}

sealed class Optimization {
    abstract val optimizationType: OptimizationType

    abstract fun apply(cfg: ControlFlowGraph): ControlFlowGraph
}

object OptimizationManager {
    private val optimizations: Map<OptimizationType, Optimization> =
        mapOf(
            OptimizationType.CONSTANT_FOLDING to ConstantFolding(),
            OptimizationType.DEAD_STORE_ELIMINATION to DeadStoreElimination(),
            OptimizationType.UNREACHABLE_CODE_ELIMINATION to UnreachableCodeElimination(),
            OptimizationType.COPY_PROPAGATION to CopyPropagation()
        )

    fun applyOptimizations(
        cfg: ControlFlowGraph,
        enabledOptimizations: List<OptimizationType>
    ): ControlFlowGraph {
        var currentCfg = cfg

        while (true) {
            val previousInstructions = currentCfg.toInstructions()

            for (optimizationType in enabledOptimizations) {
                val optimization = optimizations[optimizationType] ?: continue
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
