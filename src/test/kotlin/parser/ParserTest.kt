package parser

import lexer.Token
import lexer.TokenType
import org.example.Exceptions.SyntaxError
import org.example.parser.ASTNode
import org.example.parser.BinaryExpression
import org.example.parser.Expression
import org.example.parser.IntExpression
import org.example.parser.Parser
import org.example.parser.ReturnStatement
import org.example.parser.SimpleFunction
import org.example.parser.SimpleProgram
import org.example.parser.UnaryExpression
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParserTest {
    /** Helper function to build a full token list for a given expression. */
    private fun buildTokensForExpression(expressionTokens: List<Token>): List<Token> =
        listOf(
            Token(TokenType.KEYWORD_INT, "int", 1, 1),
            Token(TokenType.IDENTIFIER, "main", 1, 5),
            Token(TokenType.LEFT_PAREN, "(", 1, 9),
            Token(TokenType.KEYWORD_VOID, "void", 1, 10),
            Token(TokenType.RIGHT_PAREN, ")", 1, 14),
            Token(TokenType.LEFT_BRACK, "{", 1, 16),
            Token(TokenType.KEYWORD_RETURN, "return", 1, 18)
        ) + expressionTokens +
            listOf(
                Token(TokenType.SEMICOLON, ";", 1, 98),
                Token(TokenType.RIGHT_BRACK, "}", 1, 99),
                Token(TokenType.EOF, "", 1, 100)
            )

    /** Extracts the single expression node from a parsed AST for easier testing. */
    private fun getExpressionFromAst(ast: ASTNode): Expression {
        assertIs<SimpleProgram>(ast)
        val function = ast.functionDefinition
        assertIs<SimpleFunction>(function)
        val statement = function.body
        assertIs<ReturnStatement>(statement)
        return statement.expression
    }

    @Nested
    inner class RegressionTests {
        @Test
        fun `test detailed AST structure for single integer`() {
            val tokens = buildTokensForExpression(listOf(Token(TokenType.INT_LITERAL, "42", 1, 25)))
            val parser = Parser()
            val ast = parser.parseTokens(tokens)

            // Verify the AST structure in detail
            val expression = getExpressionFromAst(ast)
            assertIs<IntExpression>(expression)
            assertEquals(42, expression.value)
        }
    }

    @Nested
    inner class UnaryExpressionTests {
        @Test
        fun `test simple negation`() {
            val tokens =
                buildTokensForExpression(
                    listOf(
                        Token(TokenType.NEGATION, "-", 1, 25),
                        Token(TokenType.INT_LITERAL, "5", 1, 26)
                    )
                )
            val parser = Parser()
            val ast = parser.parseTokens(tokens)
            val expr = getExpressionFromAst(ast)

            assertIs<UnaryExpression>(expr)
            assertEquals(TokenType.NEGATION, expr.operator.type)
            assertIs<IntExpression>(expr.expression)
            assertEquals(5, (expr.expression as IntExpression).value)
        }

        @Test
        fun `test double negation`() {
            val tokens =
                buildTokensForExpression(
                    listOf(
                        Token(TokenType.NEGATION, "-", 1, 25),
                        Token(TokenType.NEGATION, "-", 1, 26),
                        Token(TokenType.INT_LITERAL, "5", 1, 27)
                    )
                )
            val parser = Parser()
            val ast = parser.parseTokens(tokens)
            val expr = getExpressionFromAst(ast)

            assertIs<UnaryExpression>(expr) // Outer unary op
            assertEquals(TokenType.NEGATION, expr.operator.type)
            val innerExpr = expr.expression
            assertIs<UnaryExpression>(innerExpr) // Inner unary op
            assertEquals(TokenType.NEGATION, innerExpr.operator.type)
            assertIs<IntExpression>(innerExpr.expression)
            assertEquals(5, (innerExpr.expression as IntExpression).value)
        }
    }

    @Nested
    inner class BinaryExpressionTests {
        @Test
        fun `test simple addition`() {
            val tokens =
                buildTokensForExpression(
                    listOf(
                        Token(TokenType.INT_LITERAL, "5", 1, 25),
                        Token(TokenType.PLUS, "+", 1, 27),
                        Token(TokenType.INT_LITERAL, "3", 1, 29)
                    )
                )
            val parser = Parser()
            val ast = parser.parseTokens(tokens)
            val expr = getExpressionFromAst(ast)

            assertIs<BinaryExpression>(expr)
            assertEquals(TokenType.PLUS, expr.operator.type)
            assertIs<IntExpression>(expr.left)
            assertEquals(5, (expr.left as IntExpression).value)
            assertIs<IntExpression>(expr.right)
            assertEquals(3, (expr.right as IntExpression).value)
        }

        @Test
        fun `test operator precedence`() {
            // Expression: 2 + 3 * 4
            val tokens =
                buildTokensForExpression(
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
            val expr = getExpressionFromAst(ast)

            // Top level should be the PLUS operation
            assertIs<BinaryExpression>(expr)
            assertEquals(TokenType.PLUS, expr.operator.type)
            assertIs<IntExpression>(expr.left)
            assertEquals(2, (expr.left as IntExpression).value)

            // Right side should be the MULTIPLY operation
            val rightExpr = expr.right
            assertIs<BinaryExpression>(rightExpr)
            assertEquals(TokenType.MULTIPLY, rightExpr.operator.type)
            assertEquals(3, (rightExpr.left as IntExpression).value)
            assertEquals(4, (rightExpr.right as IntExpression).value)
        }

        @Test
        fun `test left associativity`() {
            // Expression: 10 - 4 - 2 which should be parsed as (10 - 4) - 2
            val tokens =
                buildTokensForExpression(
                    listOf(
                        Token(TokenType.INT_LITERAL, "10", 1, 25),
                        Token(TokenType.NEGATION, "-", 1, 28),
                        Token(TokenType.INT_LITERAL, "4", 1, 30),
                        Token(TokenType.NEGATION, "-", 1, 32),
                        Token(TokenType.INT_LITERAL, "2", 1, 34)
                    )
                )
            val parser = Parser()
            val ast = parser.parseTokens(tokens)
            val expr = getExpressionFromAst(ast)

            // The top-level operation is the *second* minus
            assertIs<BinaryExpression>(expr)
            assertEquals(TokenType.NEGATION, expr.operator.type)
            assertIs<IntExpression>(expr.right)
            assertEquals(2, (expr.right as IntExpression).value)

            // The left-hand side is the *first* minus operation (10 - 4)
            val leftExpr = expr.left
            assertIs<BinaryExpression>(leftExpr)
            assertEquals(TokenType.NEGATION, leftExpr.operator.type)
            assertEquals(10, (leftExpr.left as IntExpression).value)
            assertEquals(4, (leftExpr.right as IntExpression).value)
        }
    }

    @Nested
    inner class ComplexExpressionTests {
        @Test
        fun `test parentheses overriding precedence`() {
            // Expression: (2 + 3) * 4
            val tokens =
                buildTokensForExpression(
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
            val expr = getExpressionFromAst(ast)

            // Top level should be MULTIPLY
            assertIs<BinaryExpression>(expr)
            assertEquals(TokenType.MULTIPLY, expr.operator.type)
            assertIs<IntExpression>(expr.right)
            assertEquals(4, (expr.right as IntExpression).value)

            // Left side should be PLUS
            val leftExpr = expr.left
            assertIs<BinaryExpression>(leftExpr)
            assertEquals(TokenType.PLUS, leftExpr.operator.type)
            assertEquals(2, (leftExpr.left as IntExpression).value)
            assertEquals(3, (leftExpr.right as IntExpression).value)
        }

        @Test
        fun `test unary operator with binary operator`() {
            // Expression: -5 + 10
            val tokens =
                buildTokensForExpression(
                    listOf(
                        Token(TokenType.NEGATION, "-", 1, 25),
                        Token(TokenType.INT_LITERAL, "5", 1, 26),
                        Token(TokenType.PLUS, "+", 1, 28),
                        Token(TokenType.INT_LITERAL, "10", 1, 30)
                    )
                )
            val parser = Parser()
            val ast = parser.parseTokens(tokens)
            val expr = getExpressionFromAst(ast)

            // Top level is PLUS
            assertIs<BinaryExpression>(expr)
            assertEquals(TokenType.PLUS, expr.operator.type)
            assertIs<IntExpression>(expr.right)
            assertEquals(10, (expr.right as IntExpression).value)

            // Left side is Unary
            val leftExpr = expr.left
            assertIs<UnaryExpression>(leftExpr)
            assertEquals(TokenType.NEGATION, leftExpr.operator.type)
            assertEquals(5, (leftExpr.expression as IntExpression).value)
        }
    }

    @Nested
    inner class SyntaxErrorTests {
        @Test
        fun `test syntax error - missing int keyword`() {
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
            assertThrows<SyntaxError> { parser.parseTokens(tokens) }
        }

        @Test
        fun `test syntax error - extra tokens after program`() {
            // CORRECTED: The extra token comes BEFORE the end of file token.
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
                    Token(TokenType.IDENTIFIER, "extra", 1, 32),
                    Token(TokenType.EOF, "", 1, 37)
                )

            val parser = Parser()
            val exception = assertThrows<SyntaxError> { parser.parseTokens(tokens) }
            assert(exception.message!!.contains("Expected end of file"))
        }

        @Test
        fun `test syntax error - incomplete expression`() {
            val tokens =
                buildTokensForExpression(
                    listOf(
                        Token(TokenType.INT_LITERAL, "5", 1, 25),
                        Token(TokenType.PLUS, "+", 1, 27)
                        // Missing right hand side
                    )
                )
            val parser = Parser()
            val exception = assertThrows<SyntaxError> { parser.parseTokens(tokens) }
            // The parser expects a factor after an operator
            assert(exception.message!!.contains("Unexpected token in expression"))
        }
    }
}
