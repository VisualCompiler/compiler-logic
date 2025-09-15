package assembly

import kotlin.test.Test
import kotlin.test.assertEquals

class InstructionFixerTest {
    private fun createFixer() = InstructionFixer()

    @Test
    fun `should expand an illegal Mov (Stack to Stack)`() {
        // Arrange
        val function = AsmFunction("main", listOf(Mov(Stack(-4), Stack(-8))))
        val program = AsmProgram(listOf(function)) // Wrap function in a list

        // Act
        val fixer = createFixer()
        val result = fixer.fix(program) as AsmProgram

        // Assert
        // The fixer doesn't add the AllocateStack instruction, it just fixes the mov
        val expectedBody =
            listOf(
                Mov(Stack(-4), Register(HardwareRegister.R10D)),
                Mov(Register(HardwareRegister.R10D), Stack(-8))
            )
        // Access the first function in the list
        assertEquals(expectedBody, result.functions[0].body)
    }

    @Test
    fun `should add AllocateStack prologue and align stack space`() {
        // Arrange
        // The fixer expects stackSize to be set by the previous pass
        val function =
            AsmFunction(
                name = "main",
                body = listOf(Mov(Register(HardwareRegister.EAX), Stack(-4))),
                stackSize = 4 // Set the pre-calculated stack size
            )
        val program = AsmProgram(listOf(function))

        // Act
        val fixer = createFixer()
        val result = fixer.fix(program) as AsmProgram

        // Assert
        // The fixer should round 4 up to 16 for alignment
        val expectedBody =
            listOf(
                AllocateStack(16),
                Mov(Register(HardwareRegister.EAX), Stack(-4))
            )
        assertEquals(expectedBody, result.functions[0].body)
    }

    @Test
    fun `should NOT add AllocateStack prologue when stackSpace is 0`() {
        // Arrange
        val function =
            AsmFunction(
                name = "main",
                body = listOf(Mov(Register(HardwareRegister.EAX), Register(HardwareRegister.EDX))),
                stackSize = 0 // No stack space needed
            )
        val program = AsmProgram(listOf(function))

        // Act
        val fixer = createFixer()
        val result = fixer.fix(program) as AsmProgram

        // Assert
        val expectedBody = listOf(Mov(Register(HardwareRegister.EAX), Register(HardwareRegister.EDX)))
        assertEquals(expectedBody, result.functions[0].body)
    }

    @Test
    fun `should handle all illegal instruction forms`() {
        // Arrange: A list of various illegal instructions
        val function =
            AsmFunction(
                name = "main",
                body =
                listOf(
                    Cmp(Stack(-4), Stack(-8)), // mem to mem
                    Cmp(Stack(-4), Imm(5)), // dest is immediate
                    AsmBinary(AsmBinaryOp.ADD, Stack(-4), Stack(-8)), // mem to mem
                    Idiv(Imm(10)) // divisor is immediate
                ),
                stackSize = 8 // e.g. two pseudo registers
            )
        val program = AsmProgram(listOf(function))

        // Act
        val fixer = createFixer()
        val result = fixer.fix(program) as AsmProgram

        // Assert
        val expectedBody =
            listOf(
                AllocateStack(16), // 8 rounded up to 16
                // Cmp(Stack, Stack)
                Mov(Stack(-4), Register(HardwareRegister.R10D)),
                Cmp(Register(HardwareRegister.R10D), Stack(-8)),
                // Cmp(Op, Imm)
                Mov(Imm(5), Register(HardwareRegister.R11D)),
                Cmp(Stack(-4), Register(HardwareRegister.R11D)),
                // AsmBinary(Stack, Stack)
                Mov(Stack(-4), Register(HardwareRegister.R10D)),
                AsmBinary(AsmBinaryOp.ADD, Register(HardwareRegister.R10D), Stack(-8)),
                // Idiv(Imm)
                Mov(Imm(10), Register(HardwareRegister.R10D)),
                Idiv(Register(HardwareRegister.R10D))
            )
        assertEquals(expectedBody, result.functions[0].body)
    }
}
