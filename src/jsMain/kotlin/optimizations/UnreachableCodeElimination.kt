package optimizations

import tacky.JumpIfNotZero
import tacky.JumpIfZero
import tacky.TackyInstruction
import tacky.TackyJump
import tacky.TackyLabel

class UnreachableCodeElimination : Optimization() {
    override val optimizationType: OptimizationType = OptimizationType.UNREACHABLE_CODE_ELIMINATION

    override fun apply(cfg: ControlFlowGraph): ControlFlowGraph {
        if (optimizationType != OptimizationType.UNREACHABLE_CODE_ELIMINATION) {
            return cfg
        }

        val unreachableBlocksRemoved = removeUnreachableBlocks(cfg)
        val uselessJumpsRemoved = removeUselessJumps(unreachableBlocksRemoved)
        val uselessLabelsRemoved = removeUselessLabels(uselessJumpsRemoved)
        val rebuiltCfg = removeEmptyBlocks(uselessLabelsRemoved)

        return rebuiltCfg
    }

    private fun removeUnreachableBlocks(cfg: ControlFlowGraph): ControlFlowGraph {
        val reachableNodeIds = mutableSetOf<Int>()
        val worklist = mutableListOf<CFGNode>()

        cfg.root?.let { worklist.add(it) }

        while (worklist.isNotEmpty()) {
            val currentNode = worklist.removeAt(0)
            if (!reachableNodeIds.add(currentNode.id)) {
                continue
            }
            currentNode.successors.forEach { successorId ->
                val successorNode = findNodeById(cfg, successorId)
                if (successorNode != null) {
                    worklist.add(successorNode)
                }
            }
        }

        val reachableBlocks = cfg.blocks.filter { it.id in reachableNodeIds }
        val reachableEdges = cfg.edges.filter { edge ->
            edge.from.id in reachableNodeIds && edge.to.id in reachableNodeIds
        }

        reachableBlocks.forEach { block ->
            block.predecessors.retainAll(reachableNodeIds)
            block.successors.retainAll(reachableNodeIds)
        }

        return ControlFlowGraph(
            functionName = cfg.functionName,
            root = cfg.root,
            blocks = reachableBlocks,
            edges = reachableEdges
        )
    }

    private fun removeUselessJumps(cfg: ControlFlowGraph): ControlFlowGraph {
        val sortedBlocks = cfg.blocks.sortedBy { it.id }
        val jumpsToRemove = mutableSetOf<TackyInstruction>()

        for (i in 0 until sortedBlocks.size - 1) {
            val currentBlock = sortedBlocks[i]
            val lastInstruction = currentBlock.instructions.lastOrNull()
            if (lastInstruction is TackyJump || lastInstruction is JumpIfZero || lastInstruction is JumpIfNotZero) {
                val defaultSuccessor = sortedBlocks[i + 1]
                var keepJump = false
                for (successorId in currentBlock.successors) {
                    if (successorId != defaultSuccessor.id) {
                        keepJump = true
                        break
                    }
                }
                if (!keepJump) {
                    jumpsToRemove.add(lastInstruction)
                }
            }
        }

        if (jumpsToRemove.isEmpty()) {
            return cfg
        }

        val newBlocks = cfg.blocks.map { oldBlock ->
            val newInstructions = oldBlock.instructions.filterNot { it in jumpsToRemove }
            Block(oldBlock.id, newInstructions, oldBlock.predecessors, oldBlock.successors)
        }

        return cfg.copy(blocks = newBlocks)
    }

    private fun removeUselessLabels(cfg: ControlFlowGraph): ControlFlowGraph {
        val sortedBlocks = cfg.blocks.sortedBy { it.id }
        val labelsToRemove = mutableSetOf<TackyInstruction>()

        if (sortedBlocks.isNotEmpty()) {
            val firstBlock = sortedBlocks[0]
            val firstInstruction = firstBlock.instructions.firstOrNull()
            if (firstInstruction is TackyLabel) {
                val startNodeId = cfg.root?.id ?: -1
                if (firstBlock.predecessors.size == 1 && firstBlock.predecessors.contains(startNodeId)) {
                    labelsToRemove.add(firstInstruction)
                }
            }

            for (i in 1 until sortedBlocks.size) {
                val currentBlock = sortedBlocks[i]
                val currentFirstInstruction = currentBlock.instructions.firstOrNull()
                if (currentFirstInstruction is TackyLabel) {
                    val previousBlock = sortedBlocks[i - 1]
                    if (currentBlock.predecessors.size == 1 && currentBlock.predecessors.contains(previousBlock.id)) {
                        labelsToRemove.add(currentFirstInstruction)
                    }
                }
            }
        }

        if (labelsToRemove.isEmpty()) {
            return cfg
        }

        val newBlocks = cfg.blocks.map { oldBlock ->
            val newInstructions = oldBlock.instructions.filterNot { it in labelsToRemove }
            Block(oldBlock.id, newInstructions, oldBlock.predecessors, oldBlock.successors)
        }

        return cfg.copy(blocks = newBlocks)
    }

    private fun removeEmptyBlocks(cfg: ControlFlowGraph): ControlFlowGraph {
        val emptyBlocks = cfg.blocks.filter { it.instructions.isEmpty() }
        if (emptyBlocks.isEmpty()) {
            return cfg
        }

        val blocksToRemove = emptyBlocks.toMutableSet()
        val newEdges = cfg.edges.toMutableList()
        val blocksToKeep = cfg.blocks.filter { it !in blocksToRemove }.toMutableList()
        val nodeMap = (blocksToKeep + listOfNotNull(cfg.root)).associateBy { it.id }

        for (emptyBlock in emptyBlocks) {
            if (emptyBlock.successors.size > 1) continue

            val successorId = emptyBlock.successors.firstOrNull() ?: continue
            val successorNode = nodeMap[successorId] ?: continue

            for (predecessorId in emptyBlock.predecessors) {
                val predecessorNode = nodeMap[predecessorId] ?: continue
                newEdges.removeAll { it.from.id == predecessorId && it.to.id == emptyBlock.id }
                newEdges.add(Edge(predecessorNode, successorNode))
                predecessorNode.successors.remove(emptyBlock.id)
                predecessorNode.successors.add(successorId)
                successorNode.predecessors.remove(emptyBlock.id)
                successorNode.predecessors.add(predecessorId)
            }
        }
        newEdges.removeAll { it.from in blocksToRemove || it.to in blocksToRemove }

        return ControlFlowGraph(
            functionName = cfg.functionName,
            root = cfg.root,
            blocks = blocksToKeep,
            edges = newEdges.distinct() // remove duplicates
        )
    }

    private fun findNodeById(cfg: ControlFlowGraph, nodeId: Int): CFGNode? {
        cfg.blocks.find { it.id == nodeId }?.let { return it }
        cfg.root?.let { if (it.id == nodeId) return it }
        return null
    }
}
