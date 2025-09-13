package optimizations

import tacky.TackyBinary
import tacky.TackyBinaryOP
import tacky.TackyConstant
import tacky.TackyCopy
import tacky.TackyFunCall
import tacky.TackyFunction
import tacky.TackyInstruction
import tacky.TackyJump
import tacky.TackyLabel
import tacky.TackyProgram
import tacky.TackyRet
import tacky.TackyUnary
import tacky.TackyUnaryOP
import tacky.TackyVar
import kotlin.test.Test
import kotlin.test.assertEquals

class DeadStoreEliminationTest {
    private val optimization = DeadStoreElimination()

    @Test
    fun `it should eliminate simple dead store`() {
        // Arrange
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyCopy(TackyConstant(5), TackyVar("x")),
                        TackyCopy(TackyConstant(10), TackyVar("x")),
                        TackyRet(TackyVar("x"))
                    )
                )
            )
        )

        val cfg = ControlFlowGraph().construct("test", program.functions[0].body)

        // Act
        val optimizedCfg = optimization.apply(cfg)
        val optimizedInstructions = optimizedCfg.toInstructions()

        // Assert
        val expectedInstructions = listOf(
            TackyCopy(TackyConstant(10), TackyVar("x")),
            TackyRet(TackyVar("x"))
        )
        assertEquals(expectedInstructions, optimizedInstructions)
    }

    @Test
    fun `it should not eliminate store that is used later`() {
        // Arrange
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyCopy(TackyConstant(5), TackyVar("x")),
                        TackyBinary(TackyBinaryOP.ADD, TackyVar("x"), TackyConstant(1), TackyVar("y")),
                        TackyRet(TackyVar("y"))
                    )
                )
            )
        )

        val cfg = ControlFlowGraph().construct("test", program.functions[0].body)

        // Act
        val optimizedCfg = optimization.apply(cfg)
        val optimizedInstructions = optimizedCfg.toInstructions()

        // Assert
        val expectedInstructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyBinary(TackyBinaryOP.ADD, TackyVar("x"), TackyConstant(1), TackyVar("y")),
            TackyRet(TackyVar("y"))
        )
        assertEquals(expectedInstructions, optimizedInstructions)
    }

    @Test
    fun `it should eliminate dead store in conditional branch`() {
        // Arrange
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyCopy(TackyConstant(5), TackyVar("x")),
                        TackyLabel("label1"),
                        TackyCopy(TackyConstant(10), TackyVar("x")),
                        TackyRet(TackyVar("x"))
                    )
                )
            )
        )

        val cfg = ControlFlowGraph().construct("test", program.functions[0].body)

        // Act
        val optimizedCfg = optimization.apply(cfg)
        val optimizedInstructions = optimizedCfg.toInstructions()

        // Assert
        val expectedInstructions = listOf(
            TackyLabel("label1"),
            TackyCopy(TackyConstant(10), TackyVar("x")),
            TackyRet(TackyVar("x"))
        )
        assertEquals(expectedInstructions, optimizedInstructions)
    }

    @Test
    fun `it should not eliminate function calls even if destination is dead`() {
        // Arrange
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyFunCall("foo", emptyList(), TackyVar("x")),
                        TackyRet(TackyConstant(0))
                    )
                )
            )
        )

        val cfg = ControlFlowGraph().construct("test", program.functions[0].body)

        // Act
        val optimizedCfg = optimization.apply(cfg)
        val optimizedInstructions = optimizedCfg.toInstructions()

        // Assert
        val expectedInstructions = listOf(
            TackyFunCall("foo", emptyList(), TackyVar("x")),
            TackyRet(TackyConstant(0))
        )
        assertEquals(expectedInstructions, optimizedInstructions)
    }

    @Test
    fun `it should eliminate multiple dead stores`() {
        // Arrange
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyCopy(TackyConstant(1), TackyVar("x")),
                        TackyCopy(TackyConstant(2), TackyVar("y")),
                        TackyCopy(TackyConstant(3), TackyVar("z")),
                        TackyRet(TackyConstant(0))
                    )
                )
            )
        )

        val cfg = ControlFlowGraph().construct("test", program.functions[0].body)

        // Act
        val optimizedCfg = optimization.apply(cfg)
        val optimizedInstructions = optimizedCfg.toInstructions()

        // Assert
        val expectedInstructions = listOf(
            TackyRet(TackyConstant(0))
        )
        assertEquals(expectedInstructions, optimizedInstructions)
    }

    @Test
    fun `it should handle unary operations`() {
        // Arrange
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyUnary(TackyUnaryOP.NEGATE, TackyConstant(5), TackyVar("x")),
                        TackyUnary(TackyUnaryOP.NEGATE, TackyConstant(10), TackyVar("x")),
                        TackyRet(TackyVar("x"))
                    )
                )
            )
        )

        val cfg = ControlFlowGraph().construct("test", program.functions[0].body)

        // Act
        val optimizedCfg = optimization.apply(cfg)
        val optimizedInstructions = optimizedCfg.toInstructions()

        // Assert
        val expectedInstructions = listOf(
            TackyUnary(TackyUnaryOP.NEGATE, TackyConstant(10), TackyVar("x")),
            TackyRet(TackyVar("x"))
        )
        assertEquals(expectedInstructions, optimizedInstructions)
    }

    @Test
    fun `it should handle binary operations`() {
        // Arrange
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyBinary(TackyBinaryOP.ADD, TackyConstant(1), TackyConstant(2), TackyVar("x")),
                        TackyBinary(TackyBinaryOP.MULTIPLY, TackyConstant(3), TackyConstant(4), TackyVar("x")),
                        TackyRet(TackyVar("x"))
                    )
                )
            )
        )

        val cfg = ControlFlowGraph().construct("test", program.functions[0].body)

        // Act
        val optimizedCfg = optimization.apply(cfg)
        val optimizedInstructions = optimizedCfg.toInstructions()

        // Assert
        val expectedInstructions = listOf(
            TackyBinary(TackyBinaryOP.MULTIPLY, TackyConstant(3), TackyConstant(4), TackyVar("x")),
            TackyRet(TackyVar("x"))
        )
        assertEquals(expectedInstructions, optimizedInstructions)
    }

    @Test
    fun `it should not eliminate instructions that read and write the same variable`() {
        // Arrange
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyCopy(TackyConstant(5), TackyVar("x")),
                        TackyBinary(TackyBinaryOP.ADD, TackyVar("x"), TackyConstant(1), TackyVar("x")),
                        TackyRet(TackyVar("x"))
                    )
                )
            )
        )

        val cfg = ControlFlowGraph().construct("test", program.functions[0].body)

        // Act
        val optimizedCfg = optimization.apply(cfg)
        val optimizedInstructions = optimizedCfg.toInstructions()

        // Assert
        val expectedInstructions = listOf(
            TackyCopy(TackyConstant(5), TackyVar("x")),
            TackyBinary(TackyBinaryOP.ADD, TackyVar("x"), TackyConstant(1), TackyVar("x")),
            TackyRet(TackyVar("x"))
        )
        assertEquals(expectedInstructions, optimizedInstructions)
    }

    @Test
    fun `it should handle empty function`() {
        // Arrange
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    emptyList()
                )
            )
        )

        val cfg = ControlFlowGraph().construct("test", program.functions[0].body)

        // Act
        val optimizedCfg = optimization.apply(cfg)
        val optimizedInstructions = optimizedCfg.toInstructions()

        // Assert
        assertEquals(emptyList<TackyInstruction>(), optimizedInstructions)
    }

    @Test
    fun `it should handle complex control flow with jumps`() {
        // Arrange
        val program = TackyProgram(
            listOf(
                TackyFunction(
                    "test",
                    emptyList(),
                    listOf(
                        TackyCopy(TackyConstant(1), TackyVar("x")),
                        TackyLabel("start"),
                        TackyCopy(TackyConstant(2), TackyVar("y")),
                        TackyJump(TackyLabel("end")),
                        TackyLabel("middle"),
                        TackyCopy(TackyConstant(3), TackyVar("z")),
                        TackyLabel("end"),
                        TackyRet(TackyVar("x"))
                    )
                )
            )
        )

        val cfg = ControlFlowGraph().construct("test", program.functions[0].body)

        // Act
        val optimizedCfg = optimization.apply(cfg)
        val optimizedInstructions = optimizedCfg.toInstructions()

        val expectedInstructions = listOf(
            TackyLabel("start"),
            TackyJump(TackyLabel("end")),
            TackyLabel("middle"),
            TackyLabel("end"),
            TackyRet(TackyVar("x"))
        )
        assertEquals(expectedInstructions, optimizedInstructions)
    }
}
