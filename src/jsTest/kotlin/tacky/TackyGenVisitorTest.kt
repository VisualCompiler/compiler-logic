package tacky

import lexer.Token
import lexer.TokenType
import org.example.parser.BinaryExpression
import org.example.parser.Identifier
import org.example.parser.IntExpression
import org.example.parser.ReturnStatement
import org.example.parser.SimpleFunction
import org.example.parser.SimpleProgram
import org.example.parser.UnaryExpression
import org.example.tacky.TackyBinary
import org.example.tacky.TackyBinaryOP
import org.example.tacky.TackyConstant
import org.example.tacky.TackyFunction
import org.example.tacky.TackyGenVisitor
import org.example.tacky.TackyProgram
import org.example.tacky.TackyRet
import org.example.tacky.TackyUnary
import org.example.tacky.TackyUnaryOP
import org.example.tacky.TackyVar
import kotlin.test.Test
import kotlin.test.assertEquals

class TackyGenVisitorTest {
    @Test
    fun `it should convert a simple return constant`() {
        // Arrange
        val ast =
            SimpleProgram(
                SimpleFunction(
                    name = Identifier("main"),
                    body = ReturnStatement(expression = IntExpression(5))
                )
            )
        val visitor = TackyGenVisitor()

        val visitorResult = ast.accept(visitor)

        val tackyFunction =
            TackyFunction(
                name = (ast.functionDefinition as SimpleFunction).name.value,
                body = visitorResult.instructions
            )
        val tackyProgram = TackyProgram(function = tackyFunction)

        // Assert
        val expected =
            TackyProgram(
                TackyFunction(
                    name = "main",
                    body = listOf(TackyRet(TackyConstant(5)))
                )
            )
        assertEquals(expected, tackyProgram)
    }

    @Test
    fun `it should handle a single nested unary expression`() {
        // Arrange
        val ast =
            SimpleProgram(
                SimpleFunction(
                    name = Identifier("main"),
                    body =
                    ReturnStatement(
                        UnaryExpression(
                            operator = Token(TokenType.TILDE, "~", 1, 1),
                            expression = IntExpression(2)
                        )
                    )
                )
            )
        val visitor = TackyGenVisitor()

        // Act
        val visitorResult = ast.accept(visitor)
        val tackyFunction =
            TackyFunction(
                name = (ast.functionDefinition as SimpleFunction).name.value,
                body = visitorResult.instructions
            )
        val tackyProgram = TackyProgram(function = tackyFunction)

        // Assert
        val expected =
            TackyProgram(
                TackyFunction(
                    name = "main",
                    body =
                    listOf(
                        TackyUnary(TackyUnaryOP.COMPLEMENT, TackyConstant(2), TackyVar("tmp.0")),
                        TackyRet(TackyVar("tmp.0"))
                    )
                )
            )
        assertEquals(expected, tackyProgram)
    }

    @Test
    fun `it should handle deeply nested unary expressions`() {
        // Arrange
        val ast =
            SimpleProgram(
                SimpleFunction(
                    name = Identifier("main"),
                    body =
                    ReturnStatement(
                        UnaryExpression(
                            operator = Token(TokenType.NEGATION, "-", 1, 1),
                            expression =
                            UnaryExpression(
                                operator = Token(TokenType.TILDE, "~", 1, 2),
                                expression =
                                UnaryExpression(
                                    operator = Token(TokenType.NEGATION, "-", 1, 3),
                                    expression = IntExpression(8)
                                )
                            )
                        )
                    )
                )
            )
        val visitor = TackyGenVisitor()

        // Act
        val visitorResult = ast.accept(visitor)
        val tackyFunction =
            TackyFunction(
                name = (ast.functionDefinition as SimpleFunction).name.value,
                body = visitorResult.instructions
            )
        val tackyProgram = TackyProgram(function = tackyFunction)

        // Assert
        val expected =
            TackyProgram(
                TackyFunction(
                    name = "main",
                    body =
                    listOf(
                        TackyUnary(TackyUnaryOP.NEGATE, TackyConstant(8), TackyVar("tmp.0")),
                        TackyUnary(TackyUnaryOP.COMPLEMENT, TackyVar("tmp.0"), TackyVar("tmp.1")),
                        TackyUnary(TackyUnaryOP.NEGATE, TackyVar("tmp.1"), TackyVar("tmp.2")),
                        TackyRet(TackyVar("tmp.2"))
                    )
                )
            )
        assertEquals(expected, tackyProgram)
    }

    @Test
    fun `it should handle a simple binary expression`() {
        // Arrange: Create the AST for "return 10 - 3;"
        val ast =
            SimpleProgram(
                SimpleFunction(
                    name = Identifier("main"),
                    body =
                    ReturnStatement(
                        BinaryExpression(
                            left = IntExpression(10),
                            operator = Token(TokenType.NEGATION, "-", 1, 1), // Token type can be NEGATION for minus
                            right = IntExpression(3)
                        )
                    )
                )
            )
        val visitor = TackyGenVisitor()

        // Act
        val visitorResult = ast.accept(visitor)
        val tackyProgram = TackyProgram(TackyFunction("main", visitorResult.instructions))

        // Assert
        val expected =
            TackyProgram(
                TackyFunction(
                    name = "main",
                    body =
                    listOf(
                        TackyBinary(TackyBinaryOP.SUBTRACT, TackyConstant(10), TackyConstant(3), TackyVar("tmp.0")),
                        TackyRet(TackyVar("tmp.0"))
                    )
                )
            )
        assertEquals(expected, tackyProgram)
    }

    @Test
    fun `it should handle nested binary expressions`() {
        // Arrange: Create the AST for "return (20 / 2) * 3;"
        val ast =
            SimpleProgram(
                SimpleFunction(
                    name = Identifier("main"),
                    body =
                    ReturnStatement(
                        BinaryExpression(
                            left =
                            BinaryExpression(
                                left = IntExpression(20),
                                operator = Token(TokenType.DIVIDE, "/", 1, 1),
                                right = IntExpression(2)
                            ),
                            operator = Token(TokenType.MULTIPLY, "*", 1, 1),
                            right = IntExpression(3)
                        )
                    )
                )
            )
        val visitor = TackyGenVisitor()

        // Act
        val visitorResult = ast.accept(visitor)
        val tackyProgram = TackyProgram(TackyFunction("main", visitorResult.instructions))

        // Assert: Check for the chain of temporary variables
        val expected =
            TackyProgram(
                TackyFunction(
                    name = "main",
                    body =
                    listOf(
                        TackyBinary(TackyBinaryOP.DIVIDE, TackyConstant(20), TackyConstant(2), TackyVar("tmp.0")),
                        TackyBinary(TackyBinaryOP.MULTIPLY, TackyVar("tmp.0"), TackyConstant(3), TackyVar("tmp.1")),
                        TackyRet(TackyVar("tmp.1"))
                    )
                )
            )
        assertEquals(expected, tackyProgram)
    }
}
