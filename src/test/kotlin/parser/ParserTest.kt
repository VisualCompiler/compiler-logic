package parser

import lexer.Token
import lexer.TokenType
import org.example.Exceptions.SyntaxError
import org.example.parser.Parser
import org.example.parser.SimpleProgram
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParserTest {
    @Test
    fun `test basic program parsing`() {
        // Create a list of tokens for a simple program: int main(void) { return 42; }
        val tokens =
            listOf(
                Token(TokenType.KEYWORD_INT, "int", 1, 1),
                Token(TokenType.IDENTIFIER, "main", 1, 5),
                Token(TokenType.LEFT_PAREN, "(", 1, 9),
                Token(TokenType.KEYWORD_VOID, "void", 1, 10),
                Token(TokenType.RIGHT_PAREN, ")", 1, 14),
                Token(TokenType.LEFT_BRACK, "{", 1, 16),
                Token(TokenType.KEYWORD_RETURN, "return", 1, 18),
                Token(TokenType.INT_LITERAL, "42", 1, 25),
                Token(TokenType.SEMICOLON, ";", 1, 27),
                Token(TokenType.RIGHT_BRACK, "}", 1, 29),
                Token(TokenType.EOF, "", 1, 30)
            )

        val parser = Parser()
        val ast = parser.parseTokens(tokens)

        // Verify the AST structure
        assertIs<SimpleProgram>(ast)
    }

    @Test
    fun `test detailed AST structure`() {
        // Create a list of tokens for a simple program: int main(void) { return 42; }
        val tokens =
            listOf(
                Token(TokenType.KEYWORD_INT, "int", 1, 1),
                Token(TokenType.IDENTIFIER, "main", 1, 5),
                Token(TokenType.LEFT_PAREN, "(", 1, 9),
                Token(TokenType.KEYWORD_VOID, "void", 1, 10),
                Token(TokenType.RIGHT_PAREN, ")", 1, 14),
                Token(TokenType.LEFT_BRACK, "{", 1, 16),
                Token(TokenType.KEYWORD_RETURN, "return", 1, 18),
                Token(TokenType.INT_LITERAL, "42", 1, 25),
                Token(TokenType.SEMICOLON, ";", 1, 27),
                Token(TokenType.RIGHT_BRACK, "}", 1, 29),
                Token(TokenType.EOF, "", 1, 30)
            )

        val parser = Parser()
        val ast = parser.parseTokens(tokens)

        // Verify the AST structure in detail
        assertIs<SimpleProgram>(ast)
        val program = ast

        // Check function definition
        val function = program.functionDefinition
        assertIs<org.example.parser.SimpleFunction>(function)
        val simpleFunction = function

        // Check function name
        assertEquals("main", simpleFunction.name.value)

        // Check return statement
        val returnStatement = simpleFunction.body
        assertIs<org.example.parser.ReturnStatement>(returnStatement)
        val returnStmt = returnStatement

        // Check return expression
        val expression = returnStmt.expression
        assertIs<org.example.parser.IntExpression>(expression)
        val intExpr = expression
        assertEquals(42, intExpr.value)
    }

    @Test
    fun `test syntax error - missing int keyword`() {
        // Missing the int keyword at the beginning
        val tokens =
            listOf(
                Token(TokenType.IDENTIFIER, "main", 1, 1),
                Token(TokenType.LEFT_PAREN, "(", 1, 5),
                Token(TokenType.KEYWORD_VOID, "void", 1, 6),
                Token(TokenType.RIGHT_PAREN, ")", 1, 10),
                Token(TokenType.LEFT_BRACK, "{", 1, 12),
                Token(TokenType.KEYWORD_RETURN, "return", 1, 14),
                Token(TokenType.INT_LITERAL, "42", 1, 21),
                Token(TokenType.SEMICOLON, ";", 1, 23),
                Token(TokenType.RIGHT_BRACK, "}", 1, 25),
                Token(TokenType.EOF, "", 1, 26)
            )

        val parser = Parser()
        val exception =
            assertThrows<SyntaxError> {
                parser.parseTokens(tokens)
            }

        // Check that the error message contains the expected text
        assert(exception.message!!.contains("Expected token: KEYWORD_INT, got IDENTIFIER"))
        assertEquals(1, exception.line)
        assertEquals(1, exception.column)
    }

    @Test
    fun `test syntax error - missing return keyword`() {
        // Missing the return keyword
        val tokens =
            listOf(
                Token(TokenType.KEYWORD_INT, "int", 1, 1),
                Token(TokenType.IDENTIFIER, "main", 1, 5),
                Token(TokenType.LEFT_PAREN, "(", 1, 9),
                Token(TokenType.KEYWORD_VOID, "void", 1, 10),
                Token(TokenType.RIGHT_PAREN, ")", 1, 14),
                Token(TokenType.LEFT_BRACK, "{", 1, 16),
                // Missing KEYWORD_RETURN here
                Token(TokenType.INT_LITERAL, "42", 1, 18),
                Token(TokenType.SEMICOLON, ";", 1, 20),
                Token(TokenType.RIGHT_BRACK, "}", 1, 22),
                Token(TokenType.EOF, "", 1, 23)
            )

        val parser = Parser()
        val exception =
            assertThrows<SyntaxError> {
                parser.parseTokens(tokens)
            }

        // Check that the error message contains the expected text
        assert(exception.message!!.contains("Expected token: KEYWORD_RETURN, got INT_LITERAL"))
        assertEquals(1, exception.line)
        assertEquals(18, exception.column)
    }

    @Test
    fun `test syntax error - extra tokens after program`() {
        // Extra tokens after the program
        val tokens =
            listOf(
                Token(TokenType.KEYWORD_INT, "int", 1, 1),
                Token(TokenType.IDENTIFIER, "main", 1, 5),
                Token(TokenType.LEFT_PAREN, "(", 1, 9),
                Token(TokenType.KEYWORD_VOID, "void", 1, 10),
                Token(TokenType.RIGHT_PAREN, ")", 1, 14),
                Token(TokenType.LEFT_BRACK, "{", 1, 16),
                Token(TokenType.KEYWORD_RETURN, "return", 1, 18),
                Token(TokenType.INT_LITERAL, "42", 1, 25),
                Token(TokenType.SEMICOLON, ";", 1, 27),
                Token(TokenType.RIGHT_BRACK, "}", 1, 29),
                Token(TokenType.EOF, "", 1, 31),
                // Extra tokens here
                Token(TokenType.IDENTIFIER, "extra", 1, 32)
            )

        val parser = Parser()
        val exception =
            assertThrows<SyntaxError> {
                parser.parseTokens(tokens)
            }

        // Check that the error message contains the expected text
        assert(exception.message!!.contains("Expected end of file"))
        assertEquals(1, exception.line)
        assertEquals(31, exception.column)
    }
}
