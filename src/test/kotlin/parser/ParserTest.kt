package parser

import lexer.Token
import lexer.TokenType
import org.example.Exceptions.SyntaxError
import org.example.parser.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParserTest {
    @Nested
    inner class RegressionTests {
        @Test
        fun `test basic program parsing`() {
            // Create a list of tokens for a simple program: int main(void) { return 42; }
            val tokens = listOf(
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
        fun `test detailed AST structure for single integer`() {
            // Create a list of tokens for a simple program: int main(void) { return 42; }
            val tokens = listOf(
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

            val function = program.functionDefinition
            assertIs<SimpleFunction>(function)
            assertEquals("main", function.name.token.lexeme)

            val returnStatement = function.body
            assertIs<ReturnStatement>(returnStatement)

            val expression = returnStatement.expression
            assertIs<IntExpression>(expression)
            assertEquals("42", expression.value.lexeme)
        }
    }

    //  verify the arithmetic expression functionality
    @Nested
    inner class ArithmeticExpressionTests {

        private fun buildTokensForExpression(expressionTokens: List<Token>): List<Token> {
            return listOf(
                Token(TokenType.KEYWORD_INT, "int", 1, 1),
                Token(TokenType.IDENTIFIER, "main", 1, 5),
                Token(TokenType.LEFT_PAREN, "(", 1, 9),
                Token(TokenType.KEYWORD_VOID, "void", 1, 10),
                Token(TokenType.RIGHT_PAREN, ")", 1, 14),
                Token(TokenType.LEFT_BRACK, "{", 1, 16),
                Token(TokenType.KEYWORD_RETURN, "return", 1, 18)
            ) + expressionTokens + listOf(
                Token(TokenType.SEMICOLON, ";", 1, 27),
                Token(TokenType.RIGHT_BRACK, "}", 1, 29),
                Token(TokenType.EOF, "", 1, 30)
            )
        }

        @Test
        fun `test simple addition`() {
            //  return 5 + 3;
            val tokens = buildTokensForExpression(
                listOf(
                    Token(TokenType.INT_LITERAL, "5", 1, 25),
                    Token(TokenType.PLUS, "+", 1, 26),
                    Token(TokenType.INT_LITERAL, "3", 1, 27)
                )
            )
            val parser = Parser()
            val ast = parser.parseTokens(tokens)

            val returnStmt = ((ast as SimpleProgram).functionDefinition as SimpleFunction).body as ReturnStatement
            val expr = returnStmt.expression
            assertIs<BinaryExpression>(expr)
            assertEquals(TokenType.PLUS, expr.operator.type)
            assertIs<IntExpression>(expr.left_operand)
            assertEquals("5", (expr.left_operand as IntExpression).value.lexeme)
            assertIs<IntExpression>(expr.right_operand)
            assertEquals("3", (expr.right_operand as IntExpression).value.lexeme)
        }

        @Test
        fun `test operator precedence`() {
            // return 2 + 3 * 4;
            val tokens = buildTokensForExpression(
                listOf(
                    Token(TokenType.INT_LITERAL, "2", 1, 25),
                    Token(TokenType.PLUS, "+", 1, 27),
                    Token(TokenType.INT_LITERAL, "3", 1, 29),
                    Token(TokenType.MULTIPLY, "*", 1, 31),
                    Token(TokenType.INT_LITERAL, "4", 1, 33)
                )
            )
            val parser = Parser()
            val ast = parser.parseTokens(tokens)

            val returnStmt = ((ast as SimpleProgram).functionDefinition as SimpleFunction).body as ReturnStatement
            val expr = returnStmt.expression

            // Top level should be the PLUS operation
            assertIs<BinaryExpression>(expr)
            assertEquals(TokenType.PLUS, expr.operator.type)

            // Left side of PLUS is 2
            assertIs<IntExpression>(expr.left_operand)
            assertEquals("2", (expr.left_operand as IntExpression).value.lexeme)

            // Right side of PLUS is another binary expression for the MULTIPLY
            val rightExpr = expr.right_operand
            assertIs<BinaryExpression>(rightExpr)
            assertEquals(TokenType.MULTIPLY, rightExpr.operator.type)
            assertEquals("3", (rightExpr.left_operand as IntExpression).value.lexeme)
            assertEquals("4", (rightExpr.right_operand as IntExpression).value.lexeme)
        }

        @Test
        fun `test parentheses overriding precedence`() {
            // return (2 + 3) * 4;
            val tokens = buildTokensForExpression(
                listOf(
                    Token(TokenType.LEFT_PAREN, "(", 1, 25),
                    Token(TokenType.INT_LITERAL, "2", 1, 26),
                    Token(TokenType.PLUS, "+", 1, 28),
                    Token(TokenType.INT_LITERAL, "3", 1, 30),
                    Token(TokenType.RIGHT_PAREN, ")", 1, 31),
                    Token(TokenType.MULTIPLY, "*", 1, 33),
                    Token(TokenType.INT_LITERAL, "4", 1, 35)
                )
            )
            val parser = Parser()
            val ast = parser.parseTokens(tokens)

            val returnStmt = ((ast as SimpleProgram).functionDefinition as SimpleFunction).body as ReturnStatement
            val expr = returnStmt.expression

            // Top level should be the MULTIPLY operation
            assertIs<BinaryExpression>(expr)
            assertEquals(TokenType.MULTIPLY, expr.operator.type)

            // Right side of MULTIPLY is 4
            assertIs<IntExpression>(expr.right_operand)
            assertEquals("4", (expr.right_operand as IntExpression).value.lexeme)

            // Left side of MULTIPLY is another binary expression for the PLUS
            val leftExpr = expr.left_operand
            assertIs<BinaryExpression>(leftExpr)
            assertEquals(TokenType.PLUS, leftExpr.operator.type)
            assertEquals("2", (leftExpr.left_operand as IntExpression).value.lexeme)
            assertEquals("3", (leftExpr.right_operand as IntExpression).value.lexeme)
        }

        @Test
        fun `test left associativity`() {
            //return 10 - 4 - 2;
            val tokens = buildTokensForExpression(
                listOf(
                    Token(TokenType.INT_LITERAL, "10", 1, 25),
                    Token(TokenType.MINUS, "-", 1, 28),
                    Token(TokenType.INT_LITERAL, "4", 1, 30),
                    Token(TokenType.MINUS, "-", 1, 32),
                    Token(TokenType.INT_LITERAL, "2", 1, 34)
                )
            )
            val parser = Parser()
            val ast = parser.parseTokens(tokens)

            val returnStmt = ((ast as SimpleProgram).functionDefinition as SimpleFunction).body as ReturnStatement
            val expr = returnStmt.expression

            // The top-level operation is the second minus
            assertIs<BinaryExpression>(expr)
            assertEquals(TokenType.MINUS, expr.operator.type)

            // Its right-hand side is 2
            assertIs<IntExpression>(expr.right_operand)
            assertEquals("2", (expr.right_operand as IntExpression).value.lexeme)

            // Its left-hand side is the first minus operation
            val leftExpr = expr.left_operand
            assertIs<BinaryExpression>(leftExpr)
            assertEquals(TokenType.MINUS, leftExpr.operator.type)
            assertEquals("10", (leftExpr.left_operand as IntExpression).value.lexeme)
            assertEquals("4", (leftExpr.right_operand as IntExpression).value.lexeme)
        }
    }

    // These tests verify that the parser correctly throws errors on bad syntax
    @Nested
    inner class SyntaxErrorTests {
        @Test
        fun `test syntax error - missing int keyword`() {
            // Missing the int keyword at the beginning
            val tokens = listOf(
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
            val exception = assertThrows<SyntaxError> {
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
            val tokens = listOf(
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
            val exception = assertThrows<SyntaxError> {
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
            val tokens = listOf(
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
                // Extra tokens here
                Token(TokenType.IDENTIFIER, "extra", 1, 31),
                Token(TokenType.EOF, "", 1, 36)
            )

            val parser = Parser()
            val exception = assertThrows<SyntaxError> {
                parser.parseTokens(tokens)
            }

            // Check that the error message contains the expected text
            assert(exception.message!!.contains("Expected end of file"))
            assertEquals(1, exception.line)
            assertEquals(31, exception.column)
        }


    }
}