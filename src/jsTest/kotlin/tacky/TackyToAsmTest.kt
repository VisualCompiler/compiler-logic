package tacky
import assembly.AsmBinary
import assembly.AsmBinaryOp
import assembly.AsmFunction
import assembly.AsmProgram
import assembly.AsmUnary
import assembly.AsmUnaryOp
import assembly.Cdq
import assembly.HardwareRegister
import assembly.Idiv
import assembly.Imm
import assembly.Mov
import assembly.Pseudo
import assembly.Register
import assembly.Ret
import kotlin.test.Test
import kotlin.test.assertEquals

class TackyToAsmTest {
    @Test
    fun `it should convert a simple tacky return`() {
        val tackyProgram =
            TackyProgram(
                TackyFunction(
                    name = "main",
                    body = listOf(TackyRet(TackyConstant(3)))
                )
            )
        val converter = TackyToAsm()

        val asmProgram = converter.convert(tackyProgram)

        val expected =
            AsmProgram(
                AsmFunction(
                    name = "main",
                    body =
                    listOf(
                        Mov(Imm(3), Register(HardwareRegister.EAX)),
                        Ret
                    )
                )
            )

        assertEquals(expected, asmProgram)
    }

    @Test
    fun `it should convert a tacky unary operation`() {
        val tackyProgram =
            TackyProgram(
                TackyFunction(
                    name = "main",
                    body =
                    listOf(
                        TackyUnary(TackyUnaryOP.NEGATE, TackyConstant(10), TackyVar("tmp.0")),
                        TackyRet(TackyVar("tmp.0"))
                    )
                )
            )
        val converter = TackyToAsm()

        val asmProgram = converter.convert(tackyProgram)

        val expected =
            AsmProgram(
                AsmFunction(
                    name = "main",
                    body =
                    listOf(
                        Mov(Imm(10), Pseudo("tmp.0")),
                        AsmUnary(AsmUnaryOp.NEG, Pseudo("tmp.0")),
                        Mov(Pseudo("tmp.0"), Register(HardwareRegister.EAX)),
                        Ret
                    )
                )
            )

        assertEquals(expected, asmProgram)
    }

    @Test
    fun `it should convert a tacky subtraction operation`() {
        // Arrange: TACKY for "tmp.0 = 100 - 5; return tmp.0;"
        val tackyProgram =
            TackyProgram(
                TackyFunction(
                    name = "main",
                    body =
                    listOf(
                        TackyBinary(TackyBinaryOP.SUBTRACT, TackyConstant(100), TackyConstant(5), TackyVar("tmp.0")),
                        TackyRet(TackyVar("tmp.0"))
                    )
                )
            )
        val converter = TackyToAsm()

        // Act
        val asmProgram = converter.convert(tackyProgram)

        // Assert
        val expected =
            AsmProgram(
                AsmFunction(
                    name = "main",
                    body =
                    listOf(
                        // From TackyBinary
                        Mov(Imm(100), Pseudo("tmp.0")),
                        AsmBinary(AsmBinaryOp.SUB, Imm(5), Pseudo("tmp.0")),
                        // From TackyReturn
                        Mov(Pseudo("tmp.0"), Register(HardwareRegister.EAX)),
                        Ret
                    )
                )
            )
        assertEquals(expected, asmProgram)
    }

    @Test
    fun `it should convert a tacky division operation`() {
        // Arrange: TACKY for "tmp.0 = 21 / 4; return tmp.0;"
        val tackyProgram =
            TackyProgram(
                TackyFunction(
                    name = "main",
                    body =
                    listOf(
                        TackyBinary(TackyBinaryOP.DIVIDE, TackyConstant(21), TackyConstant(4), TackyVar("tmp.0")),
                        TackyRet(TackyVar("tmp.0"))
                    )
                )
            )
        val converter = TackyToAsm()

        // Act
        val asmProgram = converter.convert(tackyProgram)

        // Assert: Check for the special sequence for idivl
        val expected =
            AsmProgram(
                AsmFunction(
                    name = "main",
                    body =
                    listOf(
                        // From TackyBinary
                        Mov(Imm(21), Register(HardwareRegister.EAX)), // Dividend to EAX
                        Cdq, // Sign-extend
                        Idiv(Imm(4)), // Divide by divisor
                        Mov(Register(HardwareRegister.EAX), Pseudo("tmp.0")), // Move result from EAX
                        // From TackyReturn
                        Mov(Pseudo("tmp.0"), Register(HardwareRegister.EAX)),
                        Ret
                    )
                )
            )
        assertEquals(expected, asmProgram)
    }
}
