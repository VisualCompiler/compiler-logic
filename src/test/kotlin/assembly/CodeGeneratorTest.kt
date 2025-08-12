package assembly

import org.example.parser.Identifier
import org.example.parser.IntExpression
import org.example.parser.ReturnStatement
import org.example.parser.SimpleFunction
import org.example.parser.SimpleProgram
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class CodeGeneratorTest {
    private val codeGenerator = CodeGenerator()

    @Test
    fun `test generate simple return statement with integer`() {
        val intExpr = IntExpression(42, line = 1, column = 8)
        val returnStatement = ReturnStatement(intExpr, line = 1, column = 1)
        val function =
            SimpleFunction(
                name = Identifier("test", line = 1, column = 1),
                body = returnStatement,
                line = 1,
                column = 1
            )
        val program = SimpleProgram(function, line = 1, column = 1)

        val asmProgram = codeGenerator.generateAsm(program)
        val resultLines = asmProgram.toAsm().lines()

        val expected =
            listOf(
                "  .globl test",
                "test:",
                "  movl 42, EAX",
                "  ret"
            )
        assertContentEquals(expected, resultLines)
    }

    @Test
    fun `test generate negative integer return value`() {
        val intExpr = IntExpression(-123, line = 1, column = 8)
        val returnStatement = ReturnStatement(intExpr, line = 1, column = 1)
        val function =
            SimpleFunction(
                name = Identifier("negative", line = 1, column = 1),
                body = returnStatement,
                line = 1,
                column = 1
            )
        val program = SimpleProgram(function, line = 1, column = 1)

        val asmProgram = codeGenerator.generateAsm(program)
        val resultLines = asmProgram.toAsm().lines()

        val expected =
            listOf(
                "  .globl negative",
                "negative:",
                "  movl -123, EAX",
                "  ret"
            )
        assertContentEquals(expected, resultLines)
    }
}
