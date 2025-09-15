package optimizations

import tacky.JumpIfZero
import tacky.TackyBinary
import tacky.TackyBinaryOP
import tacky.TackyConstant
import tacky.TackyCopy
import tacky.TackyFunCall
import tacky.TackyFunction
import tacky.TackyLabel
import tacky.TackyProgram
import tacky.TackyRet
import tacky.TackyUnary
import tacky.TackyUnaryOP
import tacky.TackyVar
import kotlin.test.Test
import kotlin.test.assertEquals

class CopyPropagationTest {
    private fun createOptimization() = CopyPropagation()

    @Test
    fun `should propagate simple copy through basic block`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyCopy(TackyVar("x"), TackyVar("y")),
            TackyRet(TackyVar("y"))
        )
        val function = TackyFunction("main", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val optimization = createOptimization()
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertEquals(3, resultInstructions.size)
        assertEquals(TackyCopy(TackyConstant(5), TackyVar("x")), resultInstructions[0])
        assertEquals(TackyCopy(TackyConstant(5), TackyVar("y")), resultInstructions[1]) // Propagated
        assertEquals(TackyRet(TackyConstant(5)), resultInstructions[2]) // Propagated
    }

    @Test
    fun `should eliminate redundant copy x = x`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyCopy(TackyVar("x"), TackyVar("x")), // Redundant copy
            TackyRet(TackyVar("x"))
        )
        val function = TackyFunction("main", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val optimization = createOptimization()
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertEquals(2, resultInstructions.size)
        assertEquals(TackyCopy(TackyConstant(5), TackyVar("x")), resultInstructions[0])
        assertEquals(TackyRet(TackyConstant(5)), resultInstructions[1]) // Propagated
    }

    @Test
    fun `should propagate copy through binary operation`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(3), TackyVar("x")),
            TackyBinary(TackyBinaryOP.ADD, TackyVar("x"), TackyConstant(2), TackyVar("y")),
            TackyRet(TackyVar("y"))
        )
        val function = TackyFunction("main", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val optimization = createOptimization()
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertEquals(3, resultInstructions.size)
        assertEquals(TackyCopy(TackyConstant(3), TackyVar("x")), resultInstructions[0])
        assertEquals(TackyBinary(TackyBinaryOP.ADD, TackyConstant(3), TackyConstant(2), TackyVar("y")), resultInstructions[1]) // Propagated
        assertEquals(TackyRet(TackyVar("y")), resultInstructions[2])
    }

    @Test
    fun `should propagate copy through unary operation`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyUnary(TackyUnaryOP.NEGATE, TackyVar("x"), TackyVar("y")),
            TackyRet(TackyVar("y"))
        )
        val function = TackyFunction("main", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val optimization = createOptimization()
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertEquals(3, resultInstructions.size)
        assertEquals(TackyCopy(TackyConstant(5), TackyVar("x")), resultInstructions[0])
        assertEquals(TackyUnary(TackyUnaryOP.NEGATE, TackyConstant(5), TackyVar("y")), resultInstructions[1]) // Propagated
        assertEquals(TackyRet(TackyVar("y")), resultInstructions[2])
    }

    @Test
    fun `should propagate copy through function call arguments`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(42), TackyVar("x")),
            TackyFunCall("print", listOf(TackyVar("x")), TackyVar("result")),
            TackyRet(TackyVar("result"))
        )
        val function = TackyFunction("main", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val optimization = createOptimization()
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertEquals(3, resultInstructions.size)
        assertEquals(TackyCopy(TackyConstant(42), TackyVar("x")), resultInstructions[0])
        assertEquals(TackyFunCall("print", listOf(TackyConstant(42)), TackyVar("result")), resultInstructions[1]) // Propagated
        assertEquals(TackyRet(TackyVar("result")), resultInstructions[2])
    }

    @Test
    fun `should propagate copy through conditional jump`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(0), TackyVar("x")),
            JumpIfZero(TackyVar("x"), TackyLabel("L1")),
            TackyRet(TackyConstant(1)),
            TackyLabel("L1"),
            TackyRet(TackyConstant(0))
        )
        val function = TackyFunction("main", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val optimization = createOptimization()
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertEquals(5, resultInstructions.size)
        assertEquals(TackyCopy(TackyConstant(0), TackyVar("x")), resultInstructions[0])
        assertEquals(JumpIfZero(TackyConstant(0), TackyLabel("L1")), resultInstructions[1]) // Propagated
        assertEquals(TackyRet(TackyConstant(1)), resultInstructions[2])
        assertEquals(TackyLabel("L1"), resultInstructions[3])
        assertEquals(TackyRet(TackyConstant(0)), resultInstructions[4])
    }

    @Test
    fun `should kill copy when variable is redefined`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyCopy(TackyConstant(10), TackyVar("x")), // Kills previous copy
            TackyRet(TackyVar("x"))
        )
        val function = TackyFunction("main", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val optimization = createOptimization()
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertEquals(3, resultInstructions.size)
        assertEquals(TackyCopy(TackyConstant(5), TackyVar("x")), resultInstructions[0])
        assertEquals(TackyCopy(TackyConstant(10), TackyVar("x")), resultInstructions[1])
        assertEquals(TackyRet(TackyConstant(10)), resultInstructions[2]) // Propagated
    }

    @Test
    fun `should handle multiple copies of same variable`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyCopy(TackyVar("x"), TackyVar("y")),
            TackyCopy(TackyVar("y"), TackyVar("z")),
            TackyRet(TackyVar("z"))
        )
        val function = TackyFunction("main", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val optimization = createOptimization()
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertEquals(4, resultInstructions.size)
        assertEquals(TackyCopy(TackyConstant(5), TackyVar("x")), resultInstructions[0])
        assertEquals(TackyCopy(TackyConstant(5), TackyVar("y")), resultInstructions[1]) // Propagated
        assertEquals(TackyCopy(TackyConstant(5), TackyVar("z")), resultInstructions[2]) // Propagated
        assertEquals(TackyRet(TackyConstant(5)), resultInstructions[3]) // Propagated
    }

    @Test
    fun `should not propagate copy across variable redefinition`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyBinary(TackyBinaryOP.ADD, TackyVar("x"), TackyConstant(1), TackyVar("x")), // Redefines x
            TackyRet(TackyVar("x"))
        )
        val function = TackyFunction("main", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val optimization = createOptimization()
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertEquals(3, resultInstructions.size)
        assertEquals(TackyCopy(TackyConstant(5), TackyVar("x")), resultInstructions[0])
        assertEquals(TackyBinary(TackyBinaryOP.ADD, TackyConstant(5), TackyConstant(1), TackyVar("x")), resultInstructions[1]) // First use propagated
        assertEquals(TackyRet(TackyVar("x")), resultInstructions[2])
    }

    @Test
    fun `should handle function with no copies`() {
        // Arrange
        val instructions = listOf(
            TackyRet(TackyConstant(42))
        )
        val function = TackyFunction("main", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val optimization = createOptimization()
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertEquals(1, resultInstructions.size)
        assertEquals(TackyRet(TackyConstant(42)), resultInstructions[0])
    }

    @Test
    fun `should handle complex control flow with multiple blocks`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            JumpIfZero(TackyVar("x"), TackyLabel("L1")),
            TackyRet(TackyVar("x")), // Should be propagated
            TackyLabel("L1"),
            TackyRet(TackyConstant(0))
        )
        val function = TackyFunction("main", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val optimization = createOptimization()
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertEquals(5, resultInstructions.size)
        assertEquals(TackyCopy(TackyConstant(5), TackyVar("x")), resultInstructions[0])
        assertEquals(JumpIfZero(TackyConstant(5), TackyLabel("L1")), resultInstructions[1]) // Propagated
        assertEquals(TackyRet(TackyVar("x")), resultInstructions[2]) // Not propagated across control flow
        assertEquals(TackyLabel("L1"), resultInstructions[3])
        assertEquals(TackyRet(TackyConstant(0)), resultInstructions[4])
    }

    @Test
    fun `should handle copy propagation with constants and variables`() {
        // Arrange
        val instructions = listOf(
            TackyCopy(TackyConstant(10), TackyVar("a")),
            TackyCopy(TackyVar("a"), TackyVar("b")),
            TackyBinary(TackyBinaryOP.ADD, TackyVar("a"), TackyVar("b"), TackyVar("c")),
            TackyRet(TackyVar("c"))
        )
        val function = TackyFunction("main", emptyList(), instructions)
        val program = TackyProgram(listOf(function))
        val cfg = ControlFlowGraph().construct("main", instructions)

        // Act
        val optimization = createOptimization()
        val result = optimization.apply(cfg)

        // Assert
        val resultInstructions = result.toInstructions()
        assertEquals(4, resultInstructions.size)
        assertEquals(TackyCopy(TackyConstant(10), TackyVar("a")), resultInstructions[0])
        assertEquals(TackyCopy(TackyConstant(10), TackyVar("b")), resultInstructions[1]) // Propagated
        assertEquals(TackyBinary(TackyBinaryOP.ADD, TackyConstant(10), TackyConstant(10), TackyVar("c")), resultInstructions[2]) // Both propagated
        assertEquals(TackyRet(TackyVar("c")), resultInstructions[3])
    }
}
