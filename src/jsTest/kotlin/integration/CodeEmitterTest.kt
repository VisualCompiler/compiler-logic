package integration

import assembly.AllocateStack
import assembly.AsmBinary
import assembly.AsmBinaryOp
import assembly.AsmFunction
import assembly.AsmProgram
import assembly.AsmUnary
import assembly.AsmUnaryOp
import assembly.Cmp
import assembly.CodeEmitter
import assembly.ConditionCode
import assembly.HardwareRegister
import assembly.Imm
import assembly.Jmp
import assembly.JmpCC
import assembly.Label
import assembly.Mov
import assembly.Register
import assembly.SetCC
import assembly.Stack
import kotlin.test.Test
import kotlin.test.assertEquals

class CodeEmitterTest {
    private val emitter = CodeEmitter()

    @Test
    fun `it should emit a basic program with prologue, epilogue, and a simple body`() {
        // Arrange
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

        // Act
        val asm = emitter.emit(program)

        // Assert
        val expected =
            """
            .globl main
            main:
              pushq %rbp
              movq %rsp, %rbp
              movl $3, %eax
              popq %rbp
              ret
            """.trimIndent()

        assertEquals(expected, asm.trim())
    }

    @Test
    fun `it should correctly format an AllocateStack instruction`() {
        // Arrange
        val program =
            AsmProgram(
                AsmFunction("main", listOf(AllocateStack(16)))
            )

        // Act
        val asm = emitter.emit(program)

        // Assert
        val bodyLine = asm.lines()[4].trim() // Get the instruction line
        assertEquals("subq $16, %rsp", bodyLine)
    }

    @Test
    fun `it should correctly format a Cmp instruction`() {
        // Arrange
        val program =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        Cmp(Imm(5), Stack(-4))
                    )
                )
            )

        // Act
        val asm = emitter.emit(program)

        // Assert
        val bodyLine = asm.lines()[4].trim()
        assertEquals("cmpl $5, -4(%rbp)", bodyLine)
    }

    @Test
    fun `it should correctly format a Label and Jmp`() {
        // Arrange
        val program =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        Jmp(Label(".L_my_label_1")),
                        Label(".L_my_label_1")
                    )
                )
            )

        // Act
        val asm = emitter.emit(program)

        // Assert
        val jumpLine = asm.lines()[4].trim()
        val labelLine = asm.lines()[5].trim()
        assertEquals("jmp .L_my_label_1", jumpLine)
        assertEquals(".L_my_label_1:", labelLine)
    }

    @Test
    fun `it should correctly format a JmpCC instruction`() {
        // Arrange
        val program =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        JmpCC(ConditionCode.NE, Label(".L_target_0"))
                    )
                )
            )

        // Act
        val asm = emitter.emit(program)

        // Assert
        val bodyLine = asm.lines()[4].trim()
        assertEquals("jne .L_target_0", bodyLine)
    }

    @Test
    fun `it should use a 1-byte register for SetCC`() {
        // Arrange
        val program =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        SetCC(ConditionCode.G, Register(HardwareRegister.EAX))
                    )
                )
            )

        // Act
        val asm = emitter.emit(program)

        // Assert
        val bodyLine = asm.lines()[4].trim()
        // The key is to check for '%al', not '%eax'
        assertEquals("setg %al", bodyLine)
    }

    @Test
    fun `it should emit an empty body correctly`() {
        // Arrange
        val program =
            AsmProgram(
                AsmFunction(name = "main", body = emptyList())
            )

        // Act
        val asm = emitter.emit(program)

        // Assert
        val expected =
            """
            .globl main
            main:
              pushq %rbp
              movq %rsp, %rbp
              popq %rbp
              ret
            """.trimIndent()

        assertEquals(expected, asm.trim())
    }

    @Test
    fun `it should handle all binary and unary operations`() {
        // Arrange
        val program =
            AsmProgram(
                AsmFunction(
                    "main",
                    listOf(
                        AsmBinary(AsmBinaryOp.ADD, Imm(1), Register(HardwareRegister.EAX)),
                        AsmBinary(AsmBinaryOp.SUB, Imm(2), Register(HardwareRegister.EAX)),
                        AsmBinary(AsmBinaryOp.MUL, Imm(3), Register(HardwareRegister.EAX)),
                        AsmUnary(AsmUnaryOp.NEG, Register(HardwareRegister.EDX)),
                        AsmUnary(AsmUnaryOp.NOT, Register(HardwareRegister.EDX))
                    )
                )
            )

        // Act
        val asm = emitter.emit(program)
        val bodyLines = asm.lines().slice(4..8).map { it.trim() }

        // Assert
        assertEquals("addl $1, %eax", bodyLines[0])
        assertEquals("subl $2, %eax", bodyLines[1])
        assertEquals("imull $3, %eax", bodyLines[2])
        assertEquals("negl %edx", bodyLines[3])
        assertEquals("notl %edx", bodyLines[4])
    }
}
