package assembly

import kotlin.test.Test
import kotlin.test.assertEquals

class PseudoEliminatorTest {
    @Test
    fun `should replace a single pseudo-register with a stack offset`() {
        val program = AsmProgram(AsmFunction("main", listOf(Mov(Pseudo("tmp.0"), Register(HardwareRegister.EAX)))))
        val result = PseudoEliminator().eliminate(program)
        val expectedProgram = AsmProgram(AsmFunction("main", listOf(Mov(Stack(-4), Register(HardwareRegister.EAX)))))
        assertEquals(4, result.stackSpaceUsed)
        assertEquals(expectedProgram, result.program)
    }

    @Test
    fun `should assign unique offsets to multiple different pseudo-registers`() {
        val program =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        Mov(Pseudo("tmp.0"), Register(HardwareRegister.EAX)),
                        AsmBinary(AsmBinaryOp.ADD, Pseudo("tmp.1"), Register(HardwareRegister.EAX))
                    )
                )
            )
        val result = PseudoEliminator().eliminate(program)
        val expectedProgram =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        Mov(Stack(-4), Register(HardwareRegister.EAX)),
                        AsmBinary(AsmBinaryOp.ADD, Stack(-8), Register(HardwareRegister.EAX))
                    )
                )
            )
        assertEquals(8, result.stackSpaceUsed)
        assertEquals(expectedProgram, result.program)
    }

    @Test
    fun `should reuse the same stack offset for multiple uses of the same pseudo-register`() {
        val program =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        Mov(Pseudo("tmp.0"), Register(HardwareRegister.EAX)),
                        Mov(Register(HardwareRegister.EAX), Pseudo("tmp.1")),
                        AsmBinary(AsmBinaryOp.SUB, Pseudo("tmp.0"), Pseudo("tmp.1"))
                    )
                )
            )
        val result = PseudoEliminator().eliminate(program)
        val expectedProgram =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        Mov(Stack(-4), Register(HardwareRegister.EAX)),
                        Mov(Register(HardwareRegister.EAX), Stack(-8)),
                        AsmBinary(AsmBinaryOp.SUB, Stack(-4), Stack(-8))
                    )
                )
            )
        assertEquals(8, result.stackSpaceUsed)
        assertEquals(expectedProgram, result.program)
    }

    @Test
    fun `should do nothing if there are no pseudo-registers`() {
        val program = AsmProgram(AsmFunction("main", listOf(Mov(Register(HardwareRegister.EDX), Register(HardwareRegister.EAX)), Ret)))
        val result = PseudoEliminator().eliminate(program)
        assertEquals(0, result.stackSpaceUsed)
        assertEquals(program, result.program)
    }
}
