package parser

import exceptions.UnexpectedEndOfFileException
import exceptions.UnexpectedTokenSyntaxException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import lexer.Lexer
import lexer.Token
import lexer.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ParserTest {
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

    // Extracts the single expression node from a parsed AST
    private fun getExpressionFromAst(ast: ASTNode): Expression {
        val program = ast as? SimpleProgram ?: error("AST root is not a SimpleProgram")
        val function = program.functionDefinition as? SimpleFunction ?: error("Function definition is not a SimpleFunction")
        val statement = function.body as? ReturnStatement ?: error("Statement is not a ReturnStatement")
        return statement.expression
    }

    // RegressionTests
    @Test
    fun testASTStructureForInteger() {
        val tokens = buildTokensForExpression(listOf(Token(TokenType.INT_LITERAL, "42", 1, 25)))
        val parser = Parser()
        val ast = parser.parseTokens(tokens)

        val expression = getExpressionFromAst(ast)
        val intExpr = expression as? IntExpression ?: error("Expression is not an IntExpression")
        assertEquals(42, intExpr.value)
    }

    // UnaryExpressionTests
    @Test
    fun testSimpleNegation() {
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

        val unaryExpr = expr as? UnaryExpression ?: error("Expression is not a UnaryExpression")
        assertEquals(TokenType.NEGATION, unaryExpr.operator.type)
        val intExpr = unaryExpr.expression as? IntExpression ?: error("Inner expression is not an IntExpression")
        assertEquals(5, intExpr.value)
    }

    @Test
    fun testDoubleNegation() {
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

        val outerUnary = expr as? UnaryExpression ?: error("Expression is not a UnaryExpression")
        assertEquals(TokenType.NEGATION, outerUnary.operator.type)
        val innerUnary = outerUnary.expression as? UnaryExpression ?: error("Inner expression is not a UnaryExpression")
        assertEquals(TokenType.NEGATION, innerUnary.operator.type)
        val intExpr = innerUnary.expression as? IntExpression ?: error("Innermost expression is not an IntExpression")
        assertEquals(5, intExpr.value)
    }

    // BinaryExpressionTests
    @Test
    fun testAddition() {
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

        val binaryExpr = expr as? BinaryExpression ?: error("Expression is not a BinaryExpression")
        assertEquals(TokenType.PLUS, binaryExpr.operator.type)
        val leftInt = binaryExpr.left as? IntExpression ?: error("Left expression is not an IntExpression")
        assertEquals(5, leftInt.value)
        val rightInt = binaryExpr.right as? IntExpression ?: error("Right expression is not an IntExpression")
        assertEquals(3, rightInt.value)
    }

    @Test
    fun testOperatorPrecedence() {
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
        val binaryExpr = expr as? BinaryExpression ?: error("Expression is not a BinaryExpression")
        assertEquals(TokenType.PLUS, binaryExpr.operator.type)
        val leftInt = binaryExpr.left as? IntExpression ?: error("Left expression is not an IntExpression")
        assertEquals(2, leftInt.value)

        val rightBinary = binaryExpr.right as? BinaryExpression ?: error("Right expression is not a BinaryExpression")
        assertEquals(TokenType.MULTIPLY, rightBinary.operator.type)
        val rightLeftInt = rightBinary.left as? IntExpression ?: error("Right-left expression is not an IntExpression")
        val rightRightInt = rightBinary.right as? IntExpression ?: error("Right-right expression is not an IntExpression")
        assertEquals(3, rightLeftInt.value)
        assertEquals(4, rightRightInt.value)
    }

    @Test
    fun testLeftAssociativity() {
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

        val topBinary = expr as? BinaryExpression ?: error("Expression is not a BinaryExpression")
        assertEquals(TokenType.NEGATION, topBinary.operator.type)
        val rightInt = topBinary.right as? IntExpression ?: error("Right expression is not an IntExpression")
        assertEquals(2, rightInt.value)

        val leftBinary = topBinary.left as? BinaryExpression ?: error("Left expression is not a BinaryExpression")
        assertEquals(TokenType.NEGATION, leftBinary.operator.type)
        val leftLeftInt = leftBinary.left as? IntExpression ?: error("Left-left expression is not an IntExpression")
        val leftRightInt = leftBinary.right as? IntExpression ?: error("Left-right expression is not an IntExpression")
        assertEquals(10, leftLeftInt.value)
        assertEquals(4, leftRightInt.value)
    }

    // ComplexExpressionTests
    @Test
    fun testParentheses() {
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
        val binaryExpr = expr as? BinaryExpression ?: error("Expression is not a BinaryExpression")
        assertEquals(TokenType.MULTIPLY, binaryExpr.operator.type)
        val rightInt = binaryExpr.right as? IntExpression ?: error("Right expression is not an IntExpression")
        assertEquals(4, rightInt.value)

        // Left side should be PLUS
        val leftExpr = binaryExpr.left as? BinaryExpression ?: error("Left expression is not a BinaryExpression")
        assertEquals(TokenType.PLUS, leftExpr.operator.type)
        assertEquals(TokenType.PLUS, leftExpr.operator.type)
        assertEquals(2, (leftExpr.left as IntExpression).value)
        assertEquals(3, (leftExpr.right as IntExpression).value)
    }

    @Test
    fun testUnaryAndBinaryOperators() {
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
        val binaryExpr = expr as? BinaryExpression ?: error("Expression is not a BinaryExpression")
        assertEquals(TokenType.PLUS, binaryExpr.operator.type)
        val rightInt = binaryExpr.right as? IntExpression ?: error("Right expression is not an IntExpression")
        assertEquals(10, rightInt.value)

        // Left side is Unary
        val leftUnary = binaryExpr.left as? UnaryExpression ?: error("Left expression is not a UnaryExpression")
        assertEquals(TokenType.NEGATION, leftUnary.operator.type)
        val leftIntExpr = leftUnary.expression as? IntExpression ?: error("Left inner expression is not an IntExpression")
        assertEquals(5, leftIntExpr.value)
    }

    // SyntaxErrorTests
    @Test
    fun testMissingIntKeywords() {
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
        assertFailsWith<UnexpectedTokenSyntaxException> { parser.parseTokens(tokens) }
    }

    @Test
    fun testExtraTokens() {
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
        val exception = assertFailsWith<UnexpectedEndOfFileException> { parser.parseTokens(tokens) }
        assertTrue(exception.message!!.contains("Expected end of file"))
    }

    @Test
    fun testIncompleteExpression() {
        val tokens =
            buildTokensForExpression(
                listOf(
                    Token(TokenType.INT_LITERAL, "5", 1, 25),
                    Token(TokenType.PLUS, "+", 1, 27)
                    // Missing right hand side
                )
            )
        val parser = Parser()
        val exception = assertFailsWith<UnexpectedTokenSyntaxException> { parser.parseTokens(tokens) }
        // The parser expects a factor after an operator
        assertTrue(exception.message!!.contains(TokenType.INT_LITERAL.toString()))
    }

    @Test
    fun `ast toJsonString produces valid JsonObject with expected keys`() {
        val code = "int main(void) { return 5 + 3; }"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize()
        val parser = Parser()
        val ast = parser.parseTokens(tokens)

        val json = ast.toJsonString()
        val parsed = Json.parseToJsonElement(json).jsonObject

        // Basic structure checks
        assertEquals("SimpleProgram", parsed["type"]!!.jsonPrimitive.content)
        assertTrue(parsed.containsKey("children"))
        assertTrue(parsed.containsKey("label"))
    }
}
