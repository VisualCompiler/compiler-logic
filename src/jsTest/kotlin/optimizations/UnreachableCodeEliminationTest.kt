package optimizations

import tacky.JumpIfZero
import tacky.TackyConstant
import tacky.TackyCopy
import tacky.TackyJump
import tacky.TackyLabel
import tacky.TackyRet
import tacky.TackyVar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnreachableCodeEliminationTest {

    private val optimization = UnreachableCodeElimination()

    // --- Tests for removeUnreachableBlocks ---

    @Test
    fun `removeUnreachableBlocks should keep all blocks when all are reachable`() {
        // Arrange
        val instructions = listOf(
            JumpIfZero(TackyConstant(0), TackyLabel("L1")),
            TackyCopy(TackyConstant(1), TackyVar("x")),
            TackyLabel("L1"),
            TackyRet(TackyConstant(0))
        )
        val cfg = ControlFlowGraph().construct("main", instructions)
        val originalBlockCount = cfg.blocks.size

        // Act
        val result = optimization.apply(cfg)

        // Assert
        assertEquals(originalBlockCount, result.blocks.size, "Should not remove any blocks when all are reachable.")
    }

    @Test
    fun `removeUnreachableBlocks should eliminate block after an unconditional jump`() {
        // Arrange
        val instructions = listOf(
            TackyJump(TackyLabel("L1")),
            TackyCopy(TackyConstant(5), TackyVar("x")), // Unreachable
            TackyLabel("L1"),
            TackyRet(TackyConstant(0))
        )
        val cfg = ControlFlowGraph().construct("main", instructions)
        val originalBlockCount = cfg.blocks.size

        // Act
        val result = optimization.apply(cfg)

        // Assert
        assertTrue(result.blocks.size < originalBlockCount, "Should have removed unreachable blocks.")
        val hasUnreachableCopy = result.toInstructions().any { instruction ->
            instruction is TackyCopy && instruction.src is TackyConstant && (instruction.src as TackyConstant).value == 5
        }
        assertFalse(hasUnreachableCopy, "The unreachable TackyCopy instruction should be gone.")
    }

    @Test
    fun `removeUnreachableBlocks should eliminate block after a return statement`() {
        // Arrange
        val instructions = listOf(
            TackyRet(TackyConstant(1)),
            TackyCopy(TackyConstant(5), TackyVar("x")) // Unreachable
        )
        val cfg = ControlFlowGraph().construct("main", instructions)
        val originalBlockCount = cfg.blocks.size

        // Act
        val result = optimization.apply(cfg)

        // Assert
        assertTrue(result.blocks.size < originalBlockCount, "Should have removed the block after the return.")
        val resultInstructions = result.toInstructions()
        assertEquals(1, resultInstructions.size)
        assertTrue(resultInstructions.first() is TackyRet)
    }

    @Test
    fun `removeUnreachableBlocks should handle complex unreachable code patterns`() {
        // Arrange
        val instructions = listOf(
            TackyJump(TackyLabel("L2")),
            TackyCopy(TackyConstant(1), TackyVar("a")), // Unreachable
            TackyCopy(TackyConstant(2), TackyVar("b")), // Unreachable
            TackyLabel("L1"),
            TackyCopy(TackyConstant(3), TackyVar("c")), // Unreachable
            TackyLabel("L2"),
            TackyRet(TackyConstant(0))
        )
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        val copyInstructions = resultInstructions.filterIsInstance<TackyCopy>()
        assertEquals(0, copyInstructions.size, "All copy instructions should be removed as they are unreachable.")
        assertTrue(resultInstructions.any { it is TackyRet }, "Return instruction should remain.")
    }

    // --- Tests for removeUselessJumps ---

    @Test
    fun `removeUselessJumps should remove a jump to the next block`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(1), TackyVar("x")),
            TackyJump(TackyLabel("Next")), // This jump is useless
            TackyLabel("Next"),
            TackyRet(TackyVar("x"))
        )
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val result = optimization.apply(cfg)
        val resultInstructions = result.toInstructions()

        // Assert
        assertFalse(resultInstructions.any { instruction -> instruction is TackyJump }, "The TackyJump should have been removed.")
        assertTrue(resultInstructions.any { instruction -> instruction is TackyCopy }, "The TackyCopy should remain.")
        assertTrue(resultInstructions.any { instruction -> instruction is TackyRet }, "The TackyRet should remain.")
    }

    @Test
    fun `removeUselessJumps should keep a necessary jump`() {
        // Arrange
        val instructions = listOf(
            JumpIfZero(TackyConstant(0), TackyLabel("Far")), // Conditional jump - necessary
            TackyCopy(TackyConstant(1), TackyVar("x")), // This will be in the next block
            TackyLabel("Far"),
            TackyRet(TackyConstant(0))
        )
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertTrue(resultInstructions.any { instruction -> instruction is JumpIfZero }, "The necessary JumpIfZero should be kept.")
    }

    @Test
    fun `removeUselessJumps should remove useless conditional jumps`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(1), TackyVar("x")),
            JumpIfZero(TackyVar("x"), TackyLabel("Next")), // Useless - jumps to next block
            TackyLabel("Next"),
            TackyRet(TackyVar("x"))
        )
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val result = optimization.apply(cfg)

        // Assert
        assertFalse(result.toInstructions().any { instruction -> instruction is JumpIfZero }, "The useless JumpIfZero should be removed.")
    }

    @Test
    fun `removeUselessJumps should keep necessary conditional jumps`() {
        // Arrange
        val instructions = listOf(
            JumpIfZero(TackyConstant(0), TackyLabel("Else")),
            TackyCopy(TackyConstant(1), TackyVar("x")),
            TackyLabel("Else"),
            TackyRet(TackyVar("x"))
        )
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val result = optimization.apply(cfg)

        // Assert
        assertTrue(result.toInstructions().any { instruction -> instruction is JumpIfZero }, "The necessary JumpIfZero should be kept.")
    }

    // --- Tests for removeUselessLabels ---

    @Test
    fun `removeUselessLabels should remove a label that is only fallen into`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(1), TackyVar("x")),
            TackyLabel("Unused"), // This label is useless
            TackyRet(TackyVar("x"))
        )
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val result = optimization.apply(cfg)
        val resultInstructions = result.toInstructions()

        // Assert
        assertFalse(resultInstructions.any { instruction -> instruction is TackyLabel }, "The TackyLabel should have been removed.")
        assertTrue(resultInstructions.any { instruction -> instruction is TackyCopy }, "The TackyCopy should remain.")
        assertTrue(resultInstructions.any { instruction -> instruction is TackyRet }, "The TackyRet should remain.")
    }

    @Test
    fun `removeUselessLabels should remove multiple useless labels`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(1), TackyVar("x")),
            TackyLabel("L1"), // Useless
            TackyCopy(TackyConstant(2), TackyVar("y")),
            TackyLabel("L2"), // Useless
            TackyRet(TackyVar("y"))
        )
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val result = optimization.apply(cfg)

        // Assert
        val labelCount = result.toInstructions().count { instruction -> instruction is TackyLabel }
        assertEquals(0, labelCount, "All useless labels should be removed.")
    }

    // --- Tests for removeEmptyBlocks ---

    @Test
    fun `removeEmptyBlocks should remove an empty block and rewire edges`() {
        // Arrange
        // Create a CFG with an empty block manually
        val block0 = Block(1, listOf(TackyCopy(TackyConstant(1), TackyVar("x"))))
        val block1 = Block(2, emptyList()) // Empty block
        val block2 = Block(3, listOf(TackyRet(TackyConstant(0))))
        val root = START(0)

        // Setup edges: 0->1, 1->2
        root.successors.add(block0.id)
        block0.predecessors.add(root.id)
        block0.successors.add(block1.id)
        block1.predecessors.add(block0.id)
        block1.successors.add(block2.id)
        block2.predecessors.add(block1.id)

        val cfg = ControlFlowGraph(
            functionName = "main",
            root = root,
            blocks = listOf(block0, block1, block2),
            edges = listOf(Edge(root, block0), Edge(block0, block1), Edge(block1, block2))
        )

        // Act
        val result = optimization.apply(cfg)

        // Assert
        assertEquals(2, result.blocks.size, "Should have removed the empty block.")
        assertFalse(result.blocks.any { block -> block.instructions.isEmpty() }, "No empty blocks should remain.")

        // Check that B0 now points directly to B2
        val newBlock0 = result.blocks.find { block -> block.id == block0.id }
        val newBlock2 = result.blocks.find { block -> block.id == block2.id }

        assertTrue(newBlock0?.successors?.contains(block2.id) ?: false, "Block 0 should now have Block 2 as a successor.")
        assertTrue(newBlock2?.predecessors?.contains(block0.id) ?: false, "Block 2 should now have Block 0 as a predecessor.")
    }

    @Test
    fun `removeEmptyBlocks should not remove blocks with multiple successors`() {
        // Arrange
        val block0 = Block(1, listOf(TackyCopy(TackyConstant(1), TackyVar("x"))))
        val block1 = Block(2, emptyList()) // Empty block with multiple successors
        val block2 = Block(3, listOf(TackyRet(TackyConstant(0))))
        val block3 = Block(4, listOf(TackyRet(TackyConstant(1))))
        val root = START(0)

        // Setup edges: 0->1, 1->2, 1->3 (multiple successors)
        root.successors.add(block0.id)
        block0.predecessors.add(root.id)
        block0.successors.add(block1.id)
        block1.predecessors.add(block0.id)
        block1.successors.add(block2.id)
        block1.successors.add(block3.id)
        block2.predecessors.add(block1.id)
        block3.predecessors.add(block1.id)

        val cfg = ControlFlowGraph(
            functionName = "main",
            root = root,
            blocks = listOf(block0, block1, block2, block3),
            edges = listOf(Edge(root, block0), Edge(block0, block1), Edge(block1, block2), Edge(block1, block3))
        )

        // Act
        val result = optimization.apply(cfg)

        // Assert
        assertEquals(4, result.blocks.size, "Should not remove empty block with multiple successors.")
        assertTrue(result.blocks.any { block -> block.id == block1.id && block.instructions.isEmpty() }, "Empty block with multiple successors should remain.")
    }

    // --- Integration tests ---

    @Test
    fun `apply should perform all optimizations in sequence`() {
        // Arrange
        val instructions = listOf(
            TackyJump(TackyLabel("L1")), // Will create unreachable code
            TackyCopy(TackyConstant(1), TackyVar("x")), // Unreachable
            TackyLabel("L1"),
            TackyCopy(TackyConstant(2), TackyVar("y")),
            TackyJump(TackyLabel("Next")), // Useless jump
            TackyLabel("Next"),
            TackyRet(TackyVar("y"))
        )
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()

        // Should not contain unreachable code
        assertFalse(
            resultInstructions.any { instruction ->
                instruction is TackyCopy && instruction.src is TackyConstant && (instruction.src as TackyConstant).value == 1
            },
            "Unreachable copy should be removed."
        )

        // Should not contain useless jumps
        assertFalse(resultInstructions.any { instruction -> instruction is TackyJump }, "Useless jump should be removed.")

        // Should contain the essential instructions
        assertTrue(
            resultInstructions.any { instruction ->
                instruction is TackyCopy && instruction.src is TackyConstant && (instruction.src as TackyConstant).value == 2
            },
            "Reachable copy should remain."
        )
        assertTrue(resultInstructions.any { instruction -> instruction is TackyRet }, "Return should remain.")
    }

    @Test
    fun `apply should handle empty CFG gracefully`() {
        // Arrange
        val cfg = ControlFlowGraph(functionName = "empty", root = null, blocks = emptyList(), edges = emptyList())

        // Act
        val result = optimization.apply(cfg)

        // Assert
        assertEquals(0, result.blocks.size, "Empty CFG should remain empty.")
        assertEquals("empty", result.functionName, "Function name should be preserved.")
    }

    @Test
    fun `apply should handle single block CFG`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(42), TackyVar("x")),
            TackyRet(TackyVar("x"))
        )
        val cfg = ControlFlowGraph().construct("single", instructions)

        // Act
        val result = optimization.apply(cfg)

        // Assert
        assertEquals(1, result.blocks.size, "Single block CFG should remain single block.")
        val resultInstructions = result.toInstructions()
        assertEquals(2, resultInstructions.size, "Should preserve all instructions in single block.")
        assertTrue(resultInstructions.any { instruction -> instruction is TackyCopy }, "Copy instruction should remain.")
        assertTrue(resultInstructions.any { instruction -> instruction is TackyRet }, "Return instruction should remain.")
    }

    @Test
    fun `apply should preserve function name and root`() {
        // Arrange
        val instructions = listOf(
            TackyRet(TackyConstant(0))
        )
        val cfg = ControlFlowGraph().construct("testFunction", instructions)

        // Act
        val result = optimization.apply(cfg)

        // Assert
        assertEquals("testFunction", result.functionName, "Function name should be preserved.")
        assertTrue(result.root != null, "Root should be preserved.")
    }
}
