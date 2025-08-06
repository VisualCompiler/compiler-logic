package wat

import org.example.parser.Identifier
import org.example.parser.IntExpression
import org.example.parser.ReturnStatement
import org.example.parser.SimpleFunction
import org.example.parser.SimpleProgram
import org.example.wasm.CodeGenerator
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class CodeGeneratorTest {
    private val codeGenerator = CodeGenerator()

    @Test
    fun `test generate simple return statement with integer`() {
        val intExpr = IntExpression(42, line = 1, column = 8)
        val returnStatement = ReturnStatement(intExpr, line = 1, column = 1)
        val function = SimpleFunction(
            name = Identifier("test", line = 1, column = 1),
            body = returnStatement,
            line = 1,
            column = 1
        )
        val program = SimpleProgram(function, line = 1, column = 1)

        val result = codeGenerator.generateWat(program)

        val expected = listOf(
            "(module",
            "  (func \$test",
            "    i32.const 42",
            "    return",
            "  )",
            "  (export \"test\" (func \$test))",
            ")",
            ""
        )
        assertContentEquals(expected, result)
    }

    @Test
    fun `test generate negative integer return value`() {
        val intExpr = IntExpression(-123, line = 1, column = 8)
        val returnStatement = ReturnStatement(intExpr, line = 1, column = 1)
        val function = SimpleFunction(
            name = Identifier("negative", line = 1, column = 1),
            body = returnStatement,
            line = 1,
            column = 1
        )
        val program = SimpleProgram(function, line = 1, column = 1)

        val result = codeGenerator.generateWat(program)

        val expected = listOf(
            "(module",
            "  (func \$negative",
            "    i32.const -123",
            "    return",
            "  )",
            "  (export \"negative\" (func \$negative))",
            ")",
            ""
        )
        assertEquals(expected, result)
    }
}
