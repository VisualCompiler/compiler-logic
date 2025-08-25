package tacky
import assembly.AsmBinary
import assembly.AsmBinaryOp
import assembly.AsmFunction
import assembly.AsmProgram
import assembly.AsmUnary
import assembly.AsmUnaryOp
import assembly.Cdq
import assembly.Cmp
import assembly.ConditionCode
import assembly.HardwareRegister
import assembly.Idiv
import assembly.Imm
import assembly.Jmp
import assembly.JmpCC
import assembly.Label
import assembly.Mov
import assembly.Pseudo
import assembly.Register
import assembly.Ret
import assembly.SetCC
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

    @Test
    fun `it should convert a logical NOT operation`() {
        // TACKY for "tmp.0 = !5; return tmp.0;"
        val tackyProgram =
            TackyProgram(
                TackyFunction(
                    "main",
                    listOf(
                        TackyUnary(TackyUnaryOP.NOT, TackyConstant(5), TackyVar("tmp.0")),
                        TackyRet(TackyVar("tmp.0"))
                    )
                )
            )
        val converter = TackyToAsm()
        val asmProgram = converter.convert(tackyProgram)

        // Expected assembly from Table 4-6
        val expected =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        Cmp(Imm(0), Imm(5)),
                        Mov(Imm(0), Pseudo("tmp.0")),
                        SetCC(ConditionCode.E, Pseudo("tmp.0")),
                        Mov(Pseudo("tmp.0"), Register(HardwareRegister.EAX)),
                        Ret
                    )
                )
            )
        assertEquals(expected, asmProgram)
    }

    @Test
    fun `it should convert a relational (less than) operation`() {
        // TACKY for "tmp.0 = 10 < 20; return tmp.0;"
        val tackyProgram =
            TackyProgram(
                TackyFunction(
                    "main",
                    listOf(
                        TackyBinary(TackyBinaryOP.LESS, TackyConstant(10), TackyConstant(20), TackyVar("tmp.0")),
                        TackyRet(TackyVar("tmp.0"))
                    )
                )
            )
        val converter = TackyToAsm()
        val asmProgram = converter.convert(tackyProgram)

        val expected =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        Mov(Imm(10), Register(HardwareRegister.EAX)),
                        Cmp(Imm(20), Register(HardwareRegister.EAX)),
                        Mov(Imm(0), Pseudo("tmp.0")),
                        SetCC(ConditionCode.L, Pseudo("tmp.0")),
                        Mov(Pseudo("tmp.0"), Register(HardwareRegister.EAX)),
                        Ret
                    )
                )
            )
        assertEquals(expected, asmProgram)
    }

    @Test
    fun `it should convert jumps and labels`() {
        // TACKY for a simple jump: "goto my_label; ... my_label: return 0;"
        val tackyProgram =
            TackyProgram(
                TackyFunction(
                    "main",
                    listOf(
                        TackyJump(TackyLabel(".L_my_label_0")),
                        TackyCopy(TackyConstant(1), TackyVar("tmp.0")), // This should be skipped
                        TackyLabel(".L_my_label_0"),
                        TackyRet(TackyConstant(0))
                    )
                )
            )
        val converter = TackyToAsm()
        val asmProgram = converter.convert(tackyProgram)

        val expected =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        Jmp(Label(".L_my_label_0")),
                        Mov(Imm(1), Pseudo("tmp.0")),
                        assembly.Label(".L_my_label_0"),
                        Mov(Imm(0), Register(HardwareRegister.EAX)),
                        Ret
                    )
                )
            )
        assertEquals(expected, asmProgram)
    }

    @Test
    fun `it should convert a conditional JumpIfZero`() {
        // TACKY for "if (var == 0) goto target;"
        val tackyProgram =
            TackyProgram(
                TackyFunction(
                    "main",
                    listOf(
                        JumpIfZero(TackyVar("tmp.0"), TackyLabel(".L_target_0"))
                    )
                )
            )
        val converter = TackyToAsm()
        val asmProgram = converter.convert(tackyProgram)

        val expected =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        Cmp(Imm(0), Pseudo("tmp.0")),
                        JmpCC(ConditionCode.E, Label(".L_target_0"))
                    )
                )
            )
        assertEquals(expected, asmProgram)
    }
}
