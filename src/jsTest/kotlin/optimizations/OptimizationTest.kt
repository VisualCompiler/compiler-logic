package optimizations

import tacky.TackyBinary
import tacky.TackyBinaryOP
import tacky.TackyConstant
import tacky.TackyCopy
import tacky.TackyFunction
import tacky.TackyLabel
import tacky.TackyProgram
import tacky.TackyRet
import tacky.TackyVar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OptimizationTest {

    // ===== OptimizationType Tests =====

    @Test
    fun `OptimizationType should have correct values`() {
        assertEquals("CONSTANT_FOLDING", OptimizationType.CONSTANT_FOLDING.name)
        assertEquals("DEAD_STORE_ELIMINATION", OptimizationType.DEAD_STORE_ELIMINATION.name)
    }

    // ===== OptimizationManager Tests =====

    @Test
    fun `optimizeProgram should return same program for empty functions`() {
        val program = TackyProgram(
            listOf(
                TackyFunction("empty", emptyList(), emptyList())
            )
        )

        val result = OptimizationManager.optimizeProgram(program, setOf(OptimizationType.CONSTANT_FOLDING))

        assertEquals(program, result)
    }

    @Test
    fun `optimizeProgram should return same program for empty optimization set`() {
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyCopy(TackyConstant(1), TackyVar("x")),
                        TackyRet(TackyVar("x"))
                    )
                )
            )
        )

        val result = OptimizationManager.optimizeProgram(program, emptySet())

        assertEquals(program, result)
    }

    @Test
    fun `optimizeProgram should apply constant folding optimization`() {
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyBinary(TackyBinaryOP.ADD, TackyConstant(1), TackyConstant(2), TackyVar("x")),
                        TackyRet(TackyVar("x"))
                    )
                )
            )
        )

        val result = OptimizationManager.optimizeProgram(program, setOf(OptimizationType.CONSTANT_FOLDING))

        // Should fold 1 + 2 to 3
        val optimizedInstructions = result.functions[0].body
        assertTrue(
            optimizedInstructions.any {
                it is TackyCopy && it.src is TackyConstant && (it.src as TackyConstant).value == 3
            }
        )
    }

    @Test
    fun `optimizeProgram should apply dead store elimination optimization`() {
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyCopy(TackyConstant(1), TackyVar("x")), // Dead store
                        TackyCopy(TackyConstant(2), TackyVar("y")), // Dead store
                        TackyRet(TackyConstant(0))
                    )
                )
            )
        )

        val result = OptimizationManager.optimizeProgram(program, setOf(OptimizationType.DEAD_STORE_ELIMINATION))

        // Should eliminate dead stores
        val optimizedInstructions = result.functions[0].body
        assertEquals(1, optimizedInstructions.size)
        assertTrue(optimizedInstructions[0] is TackyRet)
    }

    @Test
    fun `optimizeProgram should apply multiple optimizations`() {
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyBinary(TackyBinaryOP.ADD, TackyConstant(1), TackyConstant(2), TackyVar("x")),
                        TackyCopy(TackyConstant(3), TackyVar("y")), // Dead store
                        TackyRet(TackyVar("x"))
                    )
                )
            )
        )

        val result = OptimizationManager.optimizeProgram(
            program,
            setOf(OptimizationType.CONSTANT_FOLDING, OptimizationType.DEAD_STORE_ELIMINATION)
        )

        val optimizedInstructions = result.functions[0].body
        // Should have constant folding result and no dead stores
        assertTrue(optimizedInstructions.size <= 2)
        assertTrue(optimizedInstructions.any { it is TackyRet })
    }

    @Test
    fun `optimizeProgram should handle multiple functions`() {
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "func1",
                    emptyList(),
                    listOf(
                        TackyBinary(TackyBinaryOP.ADD, TackyConstant(1), TackyConstant(2), TackyVar("x")),
                        TackyRet(TackyVar("x"))
                    )
                ),
                TackyFunction(
                    "func2",
                    emptyList(),
                    listOf(
                        TackyCopy(TackyConstant(3), TackyVar("y")),
                        TackyRet(TackyVar("y"))
                    )
                )
            )
        )

        val result = OptimizationManager.optimizeProgram(program, setOf(OptimizationType.CONSTANT_FOLDING))

        assertEquals(2, result.functions.size)
        assertEquals("func1", result.functions[0].name)
        assertEquals("func2", result.functions[1].name)
    }

    @Test
    fun `optimizeProgram should preserve function names and arguments`() {
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    listOf("arg1", "arg2"),
                    listOf(
                        TackyCopy(TackyConstant(1), TackyVar("x")),
                        TackyRet(TackyVar("x"))
                    )
                )
            )
        )

        val result = OptimizationManager.optimizeProgram(program, setOf(OptimizationType.CONSTANT_FOLDING))

        assertEquals("test", result.functions[0].name)
        assertEquals(listOf("arg1", "arg2"), result.functions[0].args)
    }

    // ===== applyOptimizations Tests =====

    @Test
    fun `applyOptimizations should return same CFG for empty optimization set`() {
        val instructions = listOf(
            TackyCopy(TackyConstant(1), TackyVar("x")),
            TackyRet(TackyVar("x"))
        )
        val cfg = ControlFlowGraph().construct("test", instructions)

        val result = OptimizationManager.applyOptimizations(cfg, emptySet())

        assertEquals(cfg, result)
    }

    @Test
    fun `applyOptimizations should apply single optimization`() {
        val instructions = listOf(
            TackyBinary(TackyBinaryOP.ADD, TackyConstant(1), TackyConstant(2), TackyVar("x")),
            TackyRet(TackyVar("x"))
        )
        val cfg = ControlFlowGraph().construct("test", instructions)

        val result = OptimizationManager.applyOptimizations(cfg, setOf(OptimizationType.CONSTANT_FOLDING))

        val optimizedInstructions = result.toInstructions()
        assertTrue(
            optimizedInstructions.any {
                it is TackyCopy && it.src is TackyConstant && (it.src as TackyConstant).value == 3
            }
        )
    }

    @Test
    fun `applyOptimizations should apply multiple optimizations in sequence`() {
        val instructions = listOf(
            TackyBinary(TackyBinaryOP.ADD, TackyConstant(1), TackyConstant(2), TackyVar("x")),
            TackyCopy(TackyConstant(3), TackyVar("y")), // Dead store
            TackyRet(TackyVar("x"))
        )
        val cfg = ControlFlowGraph().construct("test", instructions)

        val result = OptimizationManager.applyOptimizations(
            cfg,
            setOf(OptimizationType.CONSTANT_FOLDING, OptimizationType.DEAD_STORE_ELIMINATION)
        )

        val optimizedInstructions = result.toInstructions()
        // Should have constant folding result and no dead stores
        assertTrue(optimizedInstructions.size <= 2)
        assertTrue(optimizedInstructions.any { it is TackyRet })
    }

    @Test
    fun `applyOptimizations should terminate when no changes occur`() {
        val instructions = listOf(
            TackyCopy(TackyConstant(1), TackyVar("x")),
            TackyRet(TackyVar("x"))
        )
        val cfg = ControlFlowGraph().construct("test", instructions)

        val result = OptimizationManager.applyOptimizations(cfg, setOf(OptimizationType.CONSTANT_FOLDING))

        // Should not change since no constant folding is possible
        assertEquals(cfg.toInstructions(), result.toInstructions())
    }

    @Test
    fun `applyOptimizations should handle valid optimization types`() {
        val instructions = listOf(
            TackyCopy(TackyConstant(1), TackyVar("x")),
            TackyRet(TackyVar("x"))
        )
        val cfg = ControlFlowGraph().construct("test", instructions)

        // This should not cause an error
        val result = OptimizationManager.applyOptimizations(cfg, setOf(OptimizationType.CONSTANT_FOLDING))

        assertNotNull(result)
    }

    // ===== Iterative Optimization Tests =====

    @Test
    fun `applyOptimizations should iterate until convergence`() {
        // Create a program that requires multiple optimization passes
        val instructions = listOf(
            TackyBinary(TackyBinaryOP.ADD, TackyConstant(1), TackyConstant(2), TackyVar("x")), // 1+2=3
            TackyBinary(TackyBinaryOP.ADD, TackyVar("x"), TackyConstant(1), TackyVar("y")), // 3+1=4
            TackyCopy(TackyConstant(5), TackyVar("z")), // Dead store
            TackyRet(TackyVar("y"))
        )
        val cfg = ControlFlowGraph().construct("test", instructions)

        val result = OptimizationManager.applyOptimizations(
            cfg,
            setOf(OptimizationType.CONSTANT_FOLDING, OptimizationType.DEAD_STORE_ELIMINATION)
        )

        val optimizedInstructions = result.toInstructions()
        // Should have optimized both constant folding and dead store elimination
        assertTrue(optimizedInstructions.size <= 3)
        assertTrue(optimizedInstructions.any { it is TackyRet })
    }

    @Test
    fun `applyOptimizations should handle empty CFG`() {
        val cfg = ControlFlowGraph()

        val result = OptimizationManager.applyOptimizations(cfg, setOf(OptimizationType.CONSTANT_FOLDING))

        assertEquals(cfg, result)
    }

    @Test
    fun `applyOptimizations should handle CFG with no instructions`() {
        val cfg = ControlFlowGraph().construct("test", emptyList())

        val result = OptimizationManager.applyOptimizations(cfg, setOf(OptimizationType.CONSTANT_FOLDING))

        assertEquals(cfg, result)
    }

    // ===== Edge Cases =====

    @Test
    fun `optimizeProgram should handle program with no functions`() {
        val program = TackyProgram(emptyList())

        val result = OptimizationManager.optimizeProgram(program, setOf(OptimizationType.CONSTANT_FOLDING))

        assertEquals(program, result)
    }

    @Test
    fun `optimizeProgram should handle functions with only labels and jumps`() {
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyLabel("start"),
                        TackyLabel("end")
                    )
                )
            )
        )

        val result = OptimizationManager.optimizeProgram(program, setOf(OptimizationType.CONSTANT_FOLDING))

        assertEquals(program, result)
    }

    @Test
    fun `optimizeProgram should handle functions with only returns`() {
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyRet(TackyConstant(0))
                    )
                )
            )
        )

        val result = OptimizationManager.optimizeProgram(program, setOf(OptimizationType.CONSTANT_FOLDING))

        assertEquals(program, result)
    }

    // ===== Optimization Order Tests =====

    @Test
    fun `applyOptimizations should apply optimizations in the order specified`() {
        val instructions = listOf(
            TackyBinary(TackyBinaryOP.ADD, TackyConstant(1), TackyConstant(2), TackyVar("x")),
            TackyCopy(TackyConstant(3), TackyVar("y")), // Dead store
            TackyRet(TackyVar("x"))
        )
        val cfg = ControlFlowGraph().construct("test", instructions)

        // Apply constant folding first, then dead store elimination
        val result1 = OptimizationManager.applyOptimizations(
            cfg,
            setOf(OptimizationType.CONSTANT_FOLDING, OptimizationType.DEAD_STORE_ELIMINATION)
        )

        // Apply dead store elimination first, then constant folding
        val result2 = OptimizationManager.applyOptimizations(
            cfg,
            setOf(OptimizationType.DEAD_STORE_ELIMINATION, OptimizationType.CONSTANT_FOLDING)
        )

        // Both should converge to the same result
        assertEquals(result1.toInstructions(), result2.toInstructions())
    }

    // ===== Performance Tests =====

    @Test
    fun `applyOptimizations should not infinite loop`() {
        val instructions = listOf(
            TackyCopy(TackyConstant(1), TackyVar("x")),
            TackyRet(TackyVar("x"))
        )
        val cfg = ControlFlowGraph().construct("test", instructions)

        // This should terminate quickly
        val result = OptimizationManager.applyOptimizations(
            cfg,
            setOf(OptimizationType.CONSTANT_FOLDING, OptimizationType.DEAD_STORE_ELIMINATION)
        )

        assertNotNull(result)
    }

    @Test
    fun `applyOptimizations should handle large instruction sequences`() {
        val instructions = (1..100).map { i ->
            TackyBinary(TackyBinaryOP.ADD, TackyConstant(i), TackyConstant(1), TackyVar("x$i"))
        } + listOf(TackyRet(TackyVar("x100")))

        val cfg = ControlFlowGraph().construct("test", instructions)

        val result = OptimizationManager.applyOptimizations(
            cfg,
            setOf(OptimizationType.CONSTANT_FOLDING, OptimizationType.DEAD_STORE_ELIMINATION)
        )

        assertNotNull(result)
        assertTrue(result.toInstructions().isNotEmpty())
    }
}
