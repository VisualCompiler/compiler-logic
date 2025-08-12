package integration

import lexer.Lexer
import org.example.parser.IntExpression
import org.example.parser.Parser
import org.example.parser.ReturnStatement
import org.example.parser.SimpleFunction
import org.example.parser.SimpleProgram
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LexerParserIntegrationTest {
    @Test
    fun `test lexer and parser integration`() {
        // Source code for a simple program
        val source = "int main(void) { return 42; }"

        // Use the lexer to tokenize the source
        val lexer = Lexer(source)
        val tokens = lexer.tokenize()

        // Parse the tokens
        val parser = Parser()
        val ast = parser.parseTokens(tokens)

        // Verify the AST structure
        assertIs<SimpleProgram>(ast)
        val program = ast

        // Check function name
        val function = program.functionDefinition
        assertIs<SimpleFunction>(function)
        val simpleFunction = function
        assertEquals("main", simpleFunction.name.value)

        // Check return value
        val returnStatement = simpleFunction.body
        assertIs<ReturnStatement>(returnStatement)
        val returnStmt = returnStatement

        val expression = returnStmt.expression
        assertIs<IntExpression>(expression)
        val intExpr = expression
        assertEquals(42, intExpr.value)
    }
}
