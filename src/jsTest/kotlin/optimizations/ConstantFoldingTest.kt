package optimizations

import tacky.JumpIfZero
import tacky.TackyBinary
import tacky.TackyBinaryOP
import tacky.TackyConstant
import tacky.TackyCopy
import tacky.TackyJump
import tacky.TackyLabel
import tacky.TackyUnary
import tacky.TackyUnaryOP
import tacky.TackyVar
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantFoldingTest {

    @Test
    fun testBinaryConstantFolding() {
        // Test: Binary(Add, Constant(1), Constant(2), Var("b")) -> Copy(Constant(3), Var("b"))
        val originalInstructions = listOf(
            TackyBinary(
                operator = TackyBinaryOP.ADD,
                src1 = TackyConstant(1),
                src2 = TackyConstant(2),
                dest = TackyVar("b")
            )
        )

        val cfg = ControlFlowGraph().construct("testFunction", originalInstructions)
        val constantFolding = ConstantFolding()
        val optimizedCfg = constantFolding.apply(cfg)
        val optimized = optimizedCfg.toInstructions()

        assertEquals(1, optimized.size)
        val copyInstruction = optimized[0] as TackyCopy
        assertEquals(TackyConstant(3), copyInstruction.src)
        assertEquals(TackyVar("b"), copyInstruction.dest)
    }

    @Test
    fun testUnaryConstantFolding() {
        // Test: Unary(Negate, Constant(5), Var("x")) -> Copy(Constant(-5), Var("x"))
        val originalInstructions = listOf(
            TackyUnary(
                operator = TackyUnaryOP.NEGATE,
                src = TackyConstant(5),
                dest = TackyVar("x")
            )
        )

        val cfg = ControlFlowGraph().construct("testFunction", originalInstructions)
        val constantFolding = ConstantFolding()
        val optimizedCfg = constantFolding.apply(cfg)
        val optimized = optimizedCfg.toInstructions()

        assertEquals(1, optimized.size)
        val copyInstruction = optimized[0] as TackyCopy
        assertEquals(TackyConstant(-5), copyInstruction.src)
        assertEquals(TackyVar("x"), copyInstruction.dest)
    }

    @Test
    fun testJumpIfZeroConstantFolding() {
        // Test: JumpIfZero(Constant(0), Label("end")) -> Jump(Label("end"))
        val originalInstructions = listOf(
            JumpIfZero(
                condition = TackyConstant(0),
                target = TackyLabel("end")
            )
        )

        val cfg = ControlFlowGraph().construct("testFunction", originalInstructions)
        val constantFolding = ConstantFolding()
        val optimizedCfg = constantFolding.apply(cfg)
        val optimized = optimizedCfg.toInstructions()

        assertEquals(1, optimized.size)
        val jumpInstruction = optimized[0] as TackyJump
        assertEquals(TackyLabel("end"), jumpInstruction.target)
    }

    @Test
    fun testJumpIfZeroRemoval() {
        // Test: JumpIfZero(Constant(5), Label("end")) -> (removed)
        val originalInstructions = listOf(
            JumpIfZero(
                condition = TackyConstant(5),
                target = TackyLabel("end")
            )
        )

        val cfg = ControlFlowGraph().construct("testFunction", originalInstructions)
        val constantFolding = ConstantFolding()
        val optimizedCfg = constantFolding.apply(cfg)
        val optimized = optimizedCfg.toInstructions()

        assertEquals(0, optimized.size) // Instruction should be removed
    }

    @Test
    fun testDivisionByZeroHandling() {
        // Test: Binary(Divide, Constant(5), Constant(0), Var("x")) -> (unchanged)
        val originalInstructions = listOf(
            TackyBinary(
                operator = TackyBinaryOP.DIVIDE,
                src1 = TackyConstant(5),
                src2 = TackyConstant(0),
                dest = TackyVar("x")
            )
        )

        val cfg = ControlFlowGraph().construct("testFunction", originalInstructions)
        val constantFolding = ConstantFolding()
        val optimizedCfg = constantFolding.apply(cfg)
        val optimized = optimizedCfg.toInstructions()

        assertEquals(1, optimized.size)
        assertEquals(originalInstructions[0], optimized[0]) // Should remain unchanged
    }
}
