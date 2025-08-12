package assembly

import org.example.parser.Identifier
import org.example.parser.IntExpression
import org.example.parser.ReturnStatement
import org.example.parser.SimpleFunction
import org.example.parser.SimpleProgram
import kotlin.test.Test
import kotlin.test.assertContentEquals

class CodeGeneratorTest {
    private val codeGenerator = CodeGenerator()

    @Test
    fun `test generate simple return statement with integer`() {
        val intExpr = IntExpression(42)
        val returnStatement = ReturnStatement(intExpr)
        val function =
            SimpleFunction(
                name = Identifier("test"),
                body = returnStatement
            )
        val program = SimpleProgram(function)

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
        val intExpr = IntExpression(-123)
        val returnStatement = ReturnStatement(intExpr)
        val function =
            SimpleFunction(
                name = Identifier("negative"),
                body = returnStatement
            )
        val program = SimpleProgram(function)

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
