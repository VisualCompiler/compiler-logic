package integration

import assembly.AsmFunction
import assembly.AsmProgram
import assembly.HardwareRegister
import assembly.Imm
import assembly.Mov
import assembly.Register
import kotlin.test.Test
import kotlin.test.assertEquals

class ToAsmTest {
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
