package integration

import assembly.AllocateStack
import assembly.AsmBinary
import assembly.AsmBinaryOp
import assembly.AsmFunction
import assembly.AsmProgram
import assembly.AsmUnary
import assembly.AsmUnaryOp
import assembly.Cdq
import assembly.HardwareRegister
import assembly.Idiv
import assembly.Imm
import assembly.Mov
import assembly.Register
import assembly.Stack
import tacky.TackyBinary
import tacky.TackyBinaryOP
import tacky.TackyConstant
import tacky.TackyFunction
import tacky.TackyProgram
import tacky.TackyRet
import tacky.TackyUnary
import tacky.TackyUnaryOP
import tacky.TackyVar

data class ValidTestCase(
    val title: String? = null,
    val code: String,
    val expectedTokenList: List<lexer.Token>? = null,
    val expectedAst: parser.ASTNode? = null,
    val expectedTacky: TackyProgram? = null,
    val expectedAssembly: AsmProgram? = null
)

object ValidTestCases {
    val testCases: List<ValidTestCase> =
        listOf(
            ValidTestCase(
                title = "Testing simple function with return statement, arithmetic operations including precedence and associativity.",
                code = "int main(void)   \n { return (5 - 3) * 4 + ~(-5) / 6 % 3; }",
                expectedTokenList =
                listOf(
                    lexer.Token(lexer.TokenType.KEYWORD_INT, "int", 1, 1),
                    lexer.Token(lexer.TokenType.IDENTIFIER, "main", 1, 5),
                    lexer.Token(lexer.TokenType.LEFT_PAREN, "(", 1, 9),
                    lexer.Token(lexer.TokenType.KEYWORD_VOID, "void", 1, 10),
                    lexer.Token(lexer.TokenType.RIGHT_PAREN, ")", 1, 14),
                    lexer.Token(lexer.TokenType.LEFT_BRACK, "{", 2, 2),
                    lexer.Token(lexer.TokenType.KEYWORD_RETURN, "return", 2, 4),
                    lexer.Token(lexer.TokenType.LEFT_PAREN, "(", 2, 11),
                    lexer.Token(lexer.TokenType.INT_LITERAL, "5", 2, 12),
                    lexer.Token(lexer.TokenType.NEGATION, "-", 2, 14),
                    lexer.Token(lexer.TokenType.INT_LITERAL, "3", 2, 16),
                    lexer.Token(lexer.TokenType.RIGHT_PAREN, ")", 2, 17),
                    lexer.Token(lexer.TokenType.MULTIPLY, "*", 2, 19),
                    lexer.Token(lexer.TokenType.INT_LITERAL, "4", 2, 21),
                    lexer.Token(lexer.TokenType.PLUS, "+", 2, 23),
                    lexer.Token(lexer.TokenType.TILDE, "~", 2, 25),
                    lexer.Token(lexer.TokenType.LEFT_PAREN, "(", 2, 26),
                    lexer.Token(lexer.TokenType.NEGATION, "-", 2, 27),
                    lexer.Token(lexer.TokenType.INT_LITERAL, "5", 2, 28),
                    lexer.Token(lexer.TokenType.RIGHT_PAREN, ")", 2, 29),
                    lexer.Token(lexer.TokenType.DIVIDE, "/", 2, 31),
                    lexer.Token(lexer.TokenType.INT_LITERAL, "6", 2, 33),
                    lexer.Token(lexer.TokenType.REMAINDER, "%", 2, 35),
                    lexer.Token(lexer.TokenType.INT_LITERAL, "3", 2, 37),
                    lexer.Token(lexer.TokenType.SEMICOLON, ";", 2, 38),
                    lexer.Token(lexer.TokenType.RIGHT_BRACK, "}", 2, 40),
                    lexer.Token(lexer.TokenType.EOF, "", 2, 41)
                ),
                expectedAst =
                parser.SimpleProgram(
                    functionDefinition =
                    parser.SimpleFunction(
                        name = "main",
                        body =
                        parser.ReturnStatement(
                            expression =
                            parser.BinaryExpression(
                                left =
                                parser.BinaryExpression(
                                    left =
                                    parser.BinaryExpression(
                                        left = parser.IntExpression(5),
                                        operator = lexer.Token(lexer.TokenType.NEGATION, "-", 2, 14),
                                        right = parser.IntExpression(3)
                                    ),
                                    operator = lexer.Token(lexer.TokenType.MULTIPLY, "*", 2, 19),
                                    right = parser.IntExpression(4)
                                ),
                                operator = lexer.Token(lexer.TokenType.PLUS, "+", 2, 23),
                                right =
                                parser.BinaryExpression(
                                    left =
                                    parser.BinaryExpression(
                                        left =
                                        parser.UnaryExpression(
                                            operator = lexer.Token(lexer.TokenType.TILDE, "~", 2, 25),
                                            expression =
                                            parser.UnaryExpression(
                                                operator =
                                                lexer.Token(
                                                    lexer.TokenType.NEGATION,
                                                    "-",
                                                    2,
                                                    27
                                                ),
                                                expression = parser.IntExpression(5)
                                            )
                                        ),
                                        operator = lexer.Token(lexer.TokenType.DIVIDE, "/", 2, 31),
                                        right = parser.IntExpression(6)
                                    ),
                                    operator = lexer.Token(lexer.TokenType.REMAINDER, "%", 2, 35),
                                    right = parser.IntExpression(3)
                                )
                            )
                        )
                    )
                ),
                expectedTacky =
                TackyProgram(
                    TackyFunction(
                        name = "main",
                        body =
                        listOf(
                            // (5 + 3) -> tmp.0
                            TackyBinary(TackyBinaryOP.SUBTRACT, TackyConstant(5), TackyConstant(3), TackyVar("tmp.0")),
                            // tmp.0 * 4 -> tmp.1
                            TackyBinary(TackyBinaryOP.MULTIPLY, TackyVar("tmp.0"), TackyConstant(4), TackyVar("tmp.1")),
                            // -5 -> tmp.2
                            TackyUnary(TackyUnaryOP.NEGATE, TackyConstant(5), TackyVar("tmp.2")),
                            // ~tmp.2 -> tmp.3
                            TackyUnary(TackyUnaryOP.COMPLEMENT, TackyVar("tmp.2"), TackyVar("tmp.3")),
                            // tmp.3 / 6 -> tmp.4
                            TackyBinary(TackyBinaryOP.DIVIDE, TackyVar("tmp.3"), TackyConstant(6), TackyVar("tmp.4")),
                            // tmp.4 % 3 -> tmp.5
                            TackyBinary(TackyBinaryOP.REMAINDER, TackyVar("tmp.4"), TackyConstant(3), TackyVar("tmp.5")),
                            // tmp.1 + tmp.5 -> tmp.6
                            TackyBinary(TackyBinaryOP.ADD, TackyVar("tmp.1"), TackyVar("tmp.5"), TackyVar("tmp.6")),
                            // Return tmp.6
                            TackyRet(TackyVar("tmp.6"))
                        )
                    )
                ),
                expectedAssembly =
                AsmProgram(
                    AsmFunction(
                        name = "main",
                        body =
                        listOf(
                            AllocateStack(28),
                            Mov(Imm(5), Stack(-4)),
                            AsmBinary(AsmBinaryOp.SUB, Imm(3), Stack(-4)),
                            Mov(Stack(-4), Register(HardwareRegister.R10D)),
                            Mov(Register(HardwareRegister.R10D), Stack(-8)),
                            AsmBinary(AsmBinaryOp.MUL, Imm(4), Stack(-8)),
                            Mov(Imm(5), Stack(-12)),
                            AsmUnary(AsmUnaryOp.NEG, Stack(-12)),
                            Mov(Stack(-12), Register(HardwareRegister.R10D)),
                            Mov(Register(HardwareRegister.R10D), Stack(-16)),
                            AsmUnary(AsmUnaryOp.NOT, Stack(-16)),
                            Mov(Stack(-16), Register(HardwareRegister.EAX)),
                            Cdq,
                            Idiv(Imm(6)),
                            Mov(Register(HardwareRegister.EAX), Stack(-20)),
                            Mov(Stack(-20), Register(HardwareRegister.EAX)),
                            Cdq,
                            Idiv(Imm(3)),
                            Mov(Register(HardwareRegister.EDX), Stack(-24)),
                            Mov(Stack(-8), Register(HardwareRegister.R10D)),
                            Mov(Register(HardwareRegister.R10D), Stack(-28)),
                            Mov(Stack(-24), Register(HardwareRegister.R10D)),
                            AsmBinary(AsmBinaryOp.ADD, Register(HardwareRegister.R10D), Stack(-28)),
                            Mov(Stack(-28), Register(HardwareRegister.EAX))
                        )
                    )
                )
            )
        )
}
