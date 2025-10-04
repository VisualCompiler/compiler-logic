package optimizations

import tacky.JumpIfNotZero
import tacky.JumpIfZero
import tacky.TackyInstruction
import tacky.TackyJump
import tacky.TackyLabel
import tacky.TackyRet

sealed class CFGNode {
    abstract val id: Int
    abstract val predecessors: MutableList<Int>
    abstract val successors: MutableList<Int>
}

data class START(
    override val id: Int = -1,
    override val successors: MutableList<Int> = mutableListOf()
) : CFGNode() {
    override val predecessors: MutableList<Int> = mutableListOf()
}

data class EXIT(
    override val id: Int = -2,
    override val predecessors: MutableList<Int> = mutableListOf()
) : CFGNode() {
    override val successors: MutableList<Int> = mutableListOf()
}

data class Block(
    override val id: Int,
    val instructions: List<TackyInstruction>,
    override val predecessors: MutableList<Int> = mutableListOf(),
    override val successors: MutableList<Int> = mutableListOf()
) : CFGNode()

data class Edge(
    val from: CFGNode,
    val to: CFGNode
)

data class ControlFlowGraph(
    val functionName: String? = null,
    val root: CFGNode? = null,
    val blocks: List<Block> = emptyList(),
    val edges: MutableList<Edge> = mutableListOf()
) {
    fun construct(
        functionName: String,
        functionBody: List<TackyInstruction>
    ): ControlFlowGraph {
        val nodes = toBasicBlocks(functionBody)
        val blocks = nodes.filterIsInstance<Block>()
        val edges = buildEdges(nodes, blocks)
        return ControlFlowGraph(
            functionName = functionName,
            root = nodes.firstOrNull(),
            blocks = blocks,
            edges = edges
        )
    }

    fun toInstructions(): List<TackyInstruction> = blocks.flatMap { it.instructions }

    private fun toBasicBlocks(instructions: List<TackyInstruction>): List<CFGNode> {
        val nodes = mutableListOf<CFGNode>()
        val current = mutableListOf<TackyInstruction>()
        var blockId = 0

        nodes += START()

        for (inst in instructions) {
            when (inst) {
                is TackyLabel -> {
                    if (current.isNotEmpty()) {
                        nodes += Block(blockId++, current.toList())
                        current.clear()
                    }
                    current += inst
                }
                is TackyJump, is JumpIfZero, is JumpIfNotZero, is TackyRet -> {
                    current += inst
                    nodes += Block(blockId++, current.toList())
                    current.clear()
                }
                else -> current += inst
            }
        }

        if (current.isNotEmpty()) {
            nodes += Block(blockId++, current.toList())
        }

        nodes += EXIT()
        return nodes
    }

    private fun buildEdges(
        nodes: List<CFGNode>,
        blocks: List<Block>
    ): MutableList<Edge> {
        val edges = mutableListOf<Edge>()
        val entry = nodes.filterIsInstance<START>().firstOrNull()
        val exit = nodes.filterIsInstance<EXIT>().firstOrNull()

        fun connect(
            from: CFGNode,
            to: CFGNode
        ) {
            edges += Edge(from, to)
            from.successors += to.id
            to.predecessors += from.id
        }

        // entry -> first block
        blocks.firstOrNull()?.let { connect(entry ?: return@let, it) }

        for ((i, block) in blocks.withIndex()) {
            val last = block.instructions.lastOrNull()
            val next = blocks.getOrNull(i + 1)

            when (last) {
                is TackyRet -> exit?.let { connect(block, it) }

                is TackyJump -> {
                    findBlockByLabel(blocks, last.target)?.let { connect(block, it) }
                }

                is JumpIfZero, is JumpIfNotZero -> {
                    val target =
                        when (last) {
                            is JumpIfZero -> last.target
                            is JumpIfNotZero -> last.target
                            else -> null
                        }
                    target?.let { t -> findBlockByLabel(blocks, t)?.let { connect(block, it) } }
                    next?.let { connect(block, next) }
                }

                else -> {
                    if (next != null) {
                        connect(block, next)
                    } else {
                        exit?.let { connect(block, it) }
                    }
                }
            }
        }

        return edges
    }

    private fun findBlockByLabel(
        blocks: List<Block>,
        label: TackyLabel
    ): Block? = blocks.find { blk -> blk.instructions.any { it is TackyLabel && it.name == label.name } }
}
