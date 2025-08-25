package assembly

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class InstructionFixerTest {
    @Test
    fun `should expand an illegal Mov (Stack to Stack) into two instructions`() {
        val program = AsmProgram(AsmFunction("main", listOf(Mov(Stack(-4), Stack(-8)))))
        val result = InstructionFixer().fix(program, 8)
        val expectedBody =
            listOf(
                AllocateStack(8),
                Mov(Stack(-4), Register(HardwareRegister.R10D)),
                Mov(Register(HardwareRegister.R10D), Stack(-8))
            )
        assertEquals(expectedBody, (result as AsmProgram).function.body)
    }

    @Test
    fun `should add AllocateStack prologue when stackSpace is greater than 0`() {
        val program = AsmProgram(AsmFunction("main", listOf(Mov(Register(HardwareRegister.EAX), Stack(-4)), Ret)))
        val result = InstructionFixer().fix(program, 4)
        val expectedBody = listOf(AllocateStack(4), Mov(Register(HardwareRegister.EAX), Stack(-4)))
        assertEquals(expectedBody, (result as AsmProgram).function.body)
    }

    @Test
    fun `should NOT add AllocateStack prologue when stackSpace is 0`() {
        val program = AsmProgram(AsmFunction("main", listOf(Mov(Register(HardwareRegister.EAX), Register(HardwareRegister.EDX)), Ret)))
        val result = InstructionFixer().fix(program, 0)
        val expectedBody = listOf(Mov(Register(HardwareRegister.EAX), Register(HardwareRegister.EDX)))
        assertEquals(expectedBody, (result as AsmProgram).function.body)
    }

    @Test
    fun `should remove Ret instructions regardless of stack space`() {
        val programWithStack = AsmProgram(AsmFunction("main", listOf(Ret)))
        val programWithoutStack = AsmProgram(AsmFunction("main", listOf(Ret)))
        val resultWithStack = InstructionFixer().fix(programWithStack, 4)
        val resultWithoutStack = InstructionFixer().fix(programWithoutStack, 0)
        assertFalse((resultWithStack as AsmProgram).function.body.any { it is Ret })
        assertFalse((resultWithoutStack as AsmProgram).function.body.any { it is Ret })
    }

    @Test
    fun `should not modify legal Mov instructions`() {
        val program =
            AsmProgram(
                AsmFunction("main", listOf(Mov(Register(HardwareRegister.EAX), Stack(-4)), Mov(Stack(-4), Register(HardwareRegister.EDX))))
            )
        val result = InstructionFixer().fix(program, 4)
        val expectedBody =
            listOf(
                AllocateStack(4),
                Mov(Register(HardwareRegister.EAX), Stack(-4)),
                Mov(Stack(-4), Register(HardwareRegister.EDX))
            )
        assertEquals(expectedBody, (result as AsmProgram).function.body)
    }

    @Test
    fun `program toAsm emits expected prologue, epilogue and body`() {
        val program =
            AsmProgram(
                AsmFunction(
                    name = "main",
                    body =
                    listOf(
                        Mov(Imm(3), Register(HardwareRegister.EAX))
                    )
                )
            )

        val asm = program.toAsm()
        val expected =
            buildString {
                appendLine("  .globl main")
                appendLine("main:")
                appendLine("  pushq %rbp")
                appendLine("  movq %rsp, %rbp")
                appendLine("  movl 3, eax")
                appendLine("  popq %rbp")
                appendLine("  ret")
            }
        assertEquals(expected, asm)
    }
}
