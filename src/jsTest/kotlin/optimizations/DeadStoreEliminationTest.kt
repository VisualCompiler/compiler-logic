package optimizations

import tacky.TackyBinary
import tacky.TackyBinaryOP
import tacky.TackyConstant
import tacky.TackyCopy
import tacky.TackyFunCall
import tacky.TackyFunction
import tacky.TackyJump
import tacky.TackyLabel
import tacky.TackyProgram
import tacky.TackyRet
import tacky.TackyUnary
import tacky.TackyUnaryOP
import tacky.TackyVar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeadStoreEliminationTest {

    @Test
    fun `test basic dead store elimination`() {
        // Test case: x = 5; y = 10; (x and y are never used)
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyCopy(TackyConstant(10), TackyVar("y")),
            TackyRet(TackyConstant(0))
        )

        val function = TackyFunction("test", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("test", instructions)
        val optimizer = DeadStoreElimination()

        val result = optimizer.apply(cfg)

        // Both dead stores should be eliminated, only ret should remain
        assertEquals(1, result.blocks.size)
        assertEquals(1, result.blocks[0].instructions.size)
        assertEquals(TackyRet(TackyConstant(0)), result.blocks[0].instructions[0])
    }

    @Test
    fun `test live store preservation`() {
        // Test case: x = 5; return x; (x is used, should be preserved)
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyRet(TackyVar("x"))
        )

        val function = TackyFunction("test", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("test", instructions)
        val optimizer = DeadStoreElimination()

        val result = optimizer.apply(cfg)

        // Both instructions should be preserved
        assertEquals(1, result.blocks.size)
        assertEquals(2, result.blocks[0].instructions.size)
        assertEquals(TackyCopy(TackyConstant(5), TackyVar("x")), result.blocks[0].instructions[0])
        assertEquals(TackyRet(TackyVar("x")), result.blocks[0].instructions[1])
    }

    @Test
    fun `test binary operation dead store elimination`() {
        // Test case: x = a + b; (x is never used)
        val instructions = listOf(
            TackyBinary(TackyBinaryOP.ADD, TackyVar("a"), TackyVar("b"), TackyVar("x")),
            TackyRet(TackyConstant(0))
        )

        val function = TackyFunction("test", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("test", instructions)
        val optimizer = DeadStoreElimination()

        val result = optimizer.apply(cfg)

        // Binary operation should be eliminated
        assertEquals(1, result.blocks.size)
        assertEquals(1, result.blocks[0].instructions.size)
        assertEquals(TackyRet(TackyConstant(0)), result.blocks[0].instructions[0])
    }

    @Test
    fun `test unary operation dead store elimination`() {
        // Test case: x = -a; (x is never used)
        val instructions = listOf(
            TackyUnary(TackyUnaryOP.NEGATE, TackyVar("a"), TackyVar("x")),
            TackyRet(TackyConstant(0))
        )

        val function = TackyFunction("test", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("test", instructions)
        val optimizer = DeadStoreElimination()

        val result = optimizer.apply(cfg)

        // Unary operation should be eliminated
        assertEquals(1, result.blocks.size)
        assertEquals(1, result.blocks[0].instructions.size)
        assertEquals(TackyRet(TackyConstant(0)), result.blocks[0].instructions[0])
    }

    @Test
    fun `test function call preservation`() {
        // Test case: x = f(); (function calls should never be eliminated)
        val instructions = listOf(
            TackyFunCall("f", emptyList(), TackyVar("x")),
            TackyRet(TackyConstant(0))
        )

        val function = TackyFunction("test", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("test", instructions)
        val optimizer = DeadStoreElimination()

        val result = optimizer.apply(cfg)

        // Function call should be preserved even if result is not used
        assertEquals(1, result.blocks.size)
        assertEquals(2, result.blocks[0].instructions.size)
        assertEquals(TackyFunCall("f", emptyList(), TackyVar("x")), result.blocks[0].instructions[0])
        assertEquals(TackyRet(TackyConstant(0)), result.blocks[0].instructions[1])
    }

    @Test
    fun `test mixed live and dead stores`() {
        // Test case: x = 5; y = 10; z = x + y; return z;
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyCopy(TackyConstant(10), TackyVar("y")),
            TackyBinary(TackyBinaryOP.ADD, TackyVar("x"), TackyVar("y"), TackyVar("z")),
            TackyRet(TackyVar("z"))
        )

        val function = TackyFunction("test", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("test", instructions)
        val optimizer = DeadStoreElimination()

        val result = optimizer.apply(cfg)

        // All instructions should be preserved as they form a live chain
        assertEquals(1, result.blocks.size)
        assertEquals(4, result.blocks[0].instructions.size)
        assertEquals(TackyCopy(TackyConstant(5), TackyVar("x")), result.blocks[0].instructions[0])
        assertEquals(TackyCopy(TackyConstant(10), TackyVar("y")), result.blocks[0].instructions[1])
        assertEquals(TackyBinary(TackyBinaryOP.ADD, TackyVar("x"), TackyVar("y"), TackyVar("z")), result.blocks[0].instructions[2])
        assertEquals(TackyRet(TackyVar("z")), result.blocks[0].instructions[3])
    }

    @Test
    fun `test conditional jump with dead stores`() {
        // Test case: x = 5; if (x) jump L1; y = 10; L1: return 0;
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyJump(TackyLabel("L1")),
            TackyCopy(TackyConstant(10), TackyVar("y")), // Dead store
            TackyLabel("L1"),
            TackyRet(TackyConstant(0))
        )

        val function = TackyFunction("test", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("test", instructions)
        val optimizer = DeadStoreElimination()

        val result = optimizer.apply(cfg)

        // Dead store after unreachable code should be eliminated
        val blockWithDeadStore = result.blocks.find { it.instructions.any { instr -> instr is TackyCopy && instr.dest.name == "y" } }
        if (blockWithDeadStore != null) {
            // If the block exists, the dead store should be eliminated
            assertTrue(blockWithDeadStore.instructions.none { instr -> instr is TackyCopy && instr.dest.name == "y" })
        }
    }

    @Test
    fun `test empty block handling`() {
        // Test case: empty function
        val instructions = listOf(
            TackyRet(TackyConstant(0))
        )

        val function = TackyFunction("test", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("test", instructions)
        val optimizer = DeadStoreElimination()

        val result = optimizer.apply(cfg)

        // Should handle empty blocks gracefully
        assertEquals(1, result.blocks.size)
        assertEquals(1, result.blocks[0].instructions.size)
        assertEquals(TackyRet(TackyConstant(0)), result.blocks[0].instructions[0])
    }

    @Test
    fun `test variable reuse after dead store`() {
        // Test case: x = 5; x = 10; return x; (first assignment to x is dead)
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")), // Dead store
            TackyCopy(TackyConstant(10), TackyVar("x")), // Live store
            TackyRet(TackyVar("x"))
        )

        val function = TackyFunction("test", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("test", instructions)
        val optimizer = DeadStoreElimination()

        val result = optimizer.apply(cfg)

        // First assignment should be eliminated, second should be preserved
        assertEquals(1, result.blocks.size)
        assertEquals(2, result.blocks[0].instructions.size)
        assertEquals(TackyCopy(TackyConstant(10), TackyVar("x")), result.blocks[0].instructions[0])
        assertEquals(TackyRet(TackyVar("x")), result.blocks[0].instructions[1])
    }

    @Test
    fun `test complex control flow with live variables`() {
        // Test case: x = 5; if (x) { y = 10; } else { z = 15; } return x;
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyJump(TackyLabel("else")),
            TackyCopy(TackyConstant(10), TackyVar("y")), // Dead store
            TackyJump(TackyLabel("end")),
            TackyLabel("else"),
            TackyCopy(TackyConstant(15), TackyVar("z")), // Dead store
            TackyLabel("end"),
            TackyRet(TackyVar("x"))
        )

        val function = TackyFunction("test", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("test", instructions)
        val optimizer = DeadStoreElimination()

        val result = optimizer.apply(cfg)

        // x assignment should be preserved, y and z assignments should be eliminated
        val allInstructions = result.blocks.flatMap { it.instructions }
        assertTrue(allInstructions.any { it is TackyCopy && it.dest.name == "x" })
        assertTrue(allInstructions.none { it is TackyCopy && it.dest.name == "y" })
        assertTrue(allInstructions.none { it is TackyCopy && it.dest.name == "z" })
    }

    @Test
    fun `test liveness analysis with multiple blocks`() {
        // Test case: x = 5; jump L1; L1: y = x; return y;
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyJump(TackyLabel("L1")),
            TackyLabel("L1"),
            TackyCopy(TackyVar("x"), TackyVar("y")),
            TackyRet(TackyVar("y"))
        )

        val function = TackyFunction("test", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("test", instructions)
        val optimizer = DeadStoreElimination()

        val result = optimizer.apply(cfg)

        // Both x and y assignments should be preserved as they form a live chain
        val allInstructions = result.blocks.flatMap { it.instructions }
        assertTrue(allInstructions.any { it is TackyCopy && it.dest.name == "x" })
        assertTrue(allInstructions.any { it is TackyCopy && it.dest.name == "y" })
    }
}
