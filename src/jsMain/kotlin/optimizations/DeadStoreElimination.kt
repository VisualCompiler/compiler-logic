package optimizations

class DeadStoreElimination : Optimization() {
    override val optimizationType: OptimizationType = OptimizationType.DEAD_STORE_ELIMINATION

    override fun apply(
        cfg: ControlFlowGraph
    ): ControlFlowGraph {
        return cfg
    }
}
