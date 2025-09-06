package assembly

import kotlin.test.Test
import kotlin.test.assertEquals

class PseudoEliminatorTest {
    private val eliminator = PseudoEliminator()

    @Test
    fun `should replace a single pseudo-register and calculate stack size`() {
        // Arrange
        val function = AsmFunction("main", listOf(Mov(Pseudo("tmp.0"), Register(HardwareRegister.EAX))))
        val program = AsmProgram(listOf(function))

        // Act
        val resultProgram = eliminator.eliminate(program)
        val resultFunction = resultProgram.functions[0]

        // Assert
        val expectedBody = listOf(Mov(Stack(-8), Register(HardwareRegister.EAX)))
        assertEquals(8, resultFunction.stackSize)
        assertEquals(expectedBody, resultFunction.body)
    }

    @Test
    fun `should assign unique offsets to multiple different pseudo-registers`() {
        // Arrange
        val function =
            AsmFunction(
                "main",
                listOf(
                    Mov(Pseudo("tmp.0"), Register(HardwareRegister.EAX)),
                    AsmBinary(AsmBinaryOp.ADD, Pseudo("tmp.1"), Register(HardwareRegister.EAX))
                )
            )
        val program = AsmProgram(listOf(function))

        // Act
        val resultProgram = eliminator.eliminate(program)
        val resultFunction = resultProgram.functions[0]

        // Assert
        val expectedBody =
            listOf(
                Mov(Stack(-8), Register(HardwareRegister.EAX)),
                AsmBinary(AsmBinaryOp.ADD, Stack(-16), Register(HardwareRegister.EAX))
            )
        assertEquals(16, resultFunction.stackSize)
        assertEquals(expectedBody, resultFunction.body)
    }

    @Test
    fun `should reuse the same stack offset for multiple uses of the same pseudo-register`() {
        // Arrange
        val function =
            AsmFunction(
                "main",
                listOf(
                    Mov(Pseudo("tmp.0"), Register(HardwareRegister.EAX)),
                    Mov(Register(HardwareRegister.EAX), Pseudo("tmp.1")),
                    AsmBinary(AsmBinaryOp.SUB, Pseudo("tmp.0"), Pseudo("tmp.1"))
                )
            )
        val program = AsmProgram(listOf(function))

        // Act
        val resultProgram = eliminator.eliminate(program)
        val resultFunction = resultProgram.functions[0]

        // Assert
        val expectedBody =
            listOf(
                Mov(Stack(-8), Register(HardwareRegister.EAX)),
                Mov(Register(HardwareRegister.EAX), Stack(-16)),
                AsmBinary(AsmBinaryOp.SUB, Stack(-8), Stack(-16))
            )
        assertEquals(16, resultFunction.stackSize)
        assertEquals(expectedBody, resultFunction.body)
    }

    @Test
    fun `should do nothing and calculate zero stack space if no pseudo-registers exist`() {
        // Arrange
        val function =
            AsmFunction(
                "main",
                listOf(
                    Mov(Register(HardwareRegister.EDX), Register(HardwareRegister.EAX))
                )
            )
        val program = AsmProgram(listOf(function))
        val originalBody = program.functions[0].body.toList() // Make a copy

        // Act
        val resultProgram = eliminator.eliminate(program)
        val resultFunction = resultProgram.functions[0]

        // Assert
        assertEquals(0, resultFunction.stackSize)
        assertEquals(originalBody, resultFunction.body)
    }

    @Test
    fun `should correctly replace pseudos in a Push instruction`() {
        // Arrange
        val function = AsmFunction("main", listOf(Push(Pseudo("param1"))))
        val program = AsmProgram(listOf(function))

        // Act
        val resultProgram = eliminator.eliminate(program)
        val resultFunction = resultProgram.functions[0]

        // Assert
        val expectedBody = listOf(Push(Stack(-8)))
        assertEquals(8, resultFunction.stackSize)
        assertEquals(expectedBody, resultFunction.body)
    }
}
