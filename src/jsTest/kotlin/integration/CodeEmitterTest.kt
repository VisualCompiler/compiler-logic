package integration

import assembly.AllocateStack
import assembly.AsmFunction
import assembly.AsmProgram
import assembly.CodeEmitter
import assembly.ConditionCode
import assembly.HardwareRegister
import assembly.Imm
import assembly.Jmp
import assembly.Label
import assembly.Mov
import assembly.Register
import assembly.SetCC
import kotlin.test.Test
import kotlin.test.assertEquals

class CodeEmitterTest {
    private val emitter = CodeEmitter()

    @Test
    fun `it should emit a basic program with prologue, epilogue, and a simple body`() {
        // Arrange
        val program =
            AsmProgram(
                functions =
                listOf(
                    AsmFunction(
                        name = "main",
                        body =
                        listOf(
                            Mov(Imm(3), Register(HardwareRegister.EAX))
                        )
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
              push rbp
              mov rbp, rsp
              mov rax, 3
              mov rsp, rbp
              pop rbp
              ret
            """.trimIndent()

        assertEquals(expected, asm.trim())
    }

    @Test
    fun `it should correctly format an AllocateStack instruction`() {
        // Arrange
        val program =
            AsmProgram(
                functions =
                listOf(
                    AsmFunction("main", listOf(AllocateStack(16)))
                )
            )

        // Act
        val asm = emitter.emit(program)

        // Assert
        val bodyLine = asm.lines()[4].trim() // Prologue is 3 lines
        assertEquals("subq rsp, 16", bodyLine)
    }

    @Test
    fun `it should correctly format a Label and Jmp`() {
        // Arrange
        val program =
            AsmProgram(
                functions =
                listOf(
                    AsmFunction(
                        "main",
                        listOf(
                            Jmp(Label(".L_my_label_1")),
                            Label(".L_my_label_1")
                        )
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
    fun `it should use a 1-byte register for SetCC`() {
        // Arrange
        val program =
            AsmProgram(
                functions =
                listOf(
                    AsmFunction(
                        "main",
                        listOf(
                            SetCC(ConditionCode.G, Register(HardwareRegister.EAX))
                        )
                    )
                )
            )

        // Act
        val asm = emitter.emit(program)

        // Assert
        val bodyLine = asm.lines()[4].trim()
        assertEquals("setg al", bodyLine)
    }

    @Test
    fun `it should emit an empty body correctly`() {
        // Arrange
        val program =
            AsmProgram(
                functions =
                listOf(
                    AsmFunction(name = "main", body = emptyList())
                )
            )

        // Act
        val asm = emitter.emit(program)

        // Assert
        val expected =
            """
            .globl main
            main:
              push rbp
              mov rbp, rsp
              mov rsp, rbp
              pop rbp
              ret
            """.trimIndent()

        assertEquals(expected, asm.trim())
    }

    // Function Call Instructions ---
    @Test
    fun `it should correctly format function call instructions`() {
        // Arrange
        val program =
            AsmProgram(
                functions =
                listOf(
                    AsmFunction(
                        "main",
                        listOf(
                            assembly.Push(Imm(10)),
                            assembly.Call("my_function"),
                            assembly.DeAllocateStack(8)
                        )
                    )
                )
            )

        // Act
        val asm = emitter.emit(program)
        val bodyLines = asm.lines().slice(4..6).map { it.trim() } // Get the 3 body instructions

        // Assert
        assertEquals("push 10", bodyLines[0])
        assertEquals("call my_function", bodyLines[1])
        assertEquals("addq rsp, 8", bodyLines[2])
    }

    @Test
    fun `it should handle multiple functions`() {
        // Arrange
        val program =
            AsmProgram(
                functions =
                listOf(
                    AsmFunction("foo", listOf(Mov(Imm(1), Register(HardwareRegister.EAX)))),
                    AsmFunction("bar", listOf(Mov(Imm(2), Register(HardwareRegister.EAX))))
                )
            )

        // Act
        val asm = emitter.emit(program)

        // Assert
        val expected =
            """
            .globl foo
            foo:
              push rbp
              mov rbp, rsp
              mov rax, 1
              mov rsp, rbp
              pop rbp
              ret

              .globl bar
            bar:
              push rbp
              mov rbp, rsp
              mov rax, 2
              mov rsp, rbp
              pop rbp
              ret
            """.trimIndent()

        assertEquals(expected, asm.trim())
    }
}
