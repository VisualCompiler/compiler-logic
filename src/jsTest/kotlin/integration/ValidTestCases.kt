package integration

import assembly.AllocateStack
import assembly.AsmBinary
import assembly.AsmBinaryOp
import assembly.AsmFunction
import assembly.AsmProgram
import assembly.AsmUnary
import assembly.AsmUnaryOp
import assembly.Cdq
import assembly.Cmp
import assembly.ConditionCode
import assembly.HardwareRegister
import assembly.Idiv
import assembly.Imm
import assembly.Jmp
import assembly.JmpCC
import assembly.Label
import assembly.Mov
import assembly.Register
import assembly.SetCC
import assembly.Stack
import tacky.JumpIfNotZero
import tacky.JumpIfZero
import tacky.TackyBinary
import tacky.TackyBinaryOP
import tacky.TackyConstant
import tacky.TackyCopy
import tacky.TackyFunction
import tacky.TackyJump
import tacky.TackyLabel
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
            ),
            ValidTestCase(
                title = "Testing complex relational and logical operators with precedence and short-circuiting.",
                // The expression is parsed as: (1 == 0) || ( (5 > 2) && (10 <= 20) )
                code = "int main(void) { return 1 == 0 || 5 > 2 && 10 <= 20; }",
                expectedTacky =
                TackyProgram(
                    TackyFunction(
                        name = "main",
                        body =
                        listOf(
                            // tmp.1 = (1 == 0)
                            TackyBinary(TackyBinaryOP.EQUAL, TackyConstant(1), TackyConstant(0), TackyVar("tmp.1")),
                            // if (tmp.1 != 0) goto .L_or_true_0
                            JumpIfNotZero(TackyVar("tmp.1"), TackyLabel(".L_or_true_0")),
                            // -- Start of the AND expression --
                            // tmp.3 = (5 > 2)
                            TackyBinary(TackyBinaryOP.GREATER, TackyConstant(5), TackyConstant(2), TackyVar("tmp.3")),
                            // if (tmp.3 == 0) goto .L_and_false_2
                            JumpIfZero(TackyVar("tmp.3"), TackyLabel(".L_and_false_2")),
                            // tmp.4 = (10 <= 20)
                            TackyBinary(TackyBinaryOP.LESS_EQUAL, TackyConstant(10), TackyConstant(20), TackyVar("tmp.4")),
                            // if (tmp.4 == 0) goto .L_and_false_2
                            JumpIfZero(TackyVar("tmp.4"), TackyLabel(".L_and_false_2")),
                            // AND is true -> tmp.2 = 1
                            TackyCopy(TackyConstant(1), TackyVar("tmp.2")),
                            TackyJump(TackyLabel(".L_and_end_3")),
                            // AND is false -> tmp.2 = 0
                            TackyLabel(".L_and_false_2"),
                            TackyCopy(TackyConstant(0), TackyVar("tmp.2")),
                            // End of AND block
                            TackyLabel(".L_and_end_3"),
                            // -- Resume OR logic --
                            // if (tmp.2 != 0) goto .L_or_true_0
                            JumpIfNotZero(TackyVar("tmp.2"), TackyLabel(".L_or_true_0")),
                            // OR is false -> tmp.0 = 0
                            TackyCopy(TackyConstant(0), TackyVar("tmp.0")),
                            TackyJump(TackyLabel(".L_or_end_1")),
                            // OR is true -> tmp.0 = 1
                            TackyLabel(".L_or_true_0"),
                            TackyCopy(TackyConstant(1), TackyVar("tmp.0")),
                            // End of OR block
                            TackyLabel(".L_or_end_1"),
                            // Final return
                            TackyRet(TackyVar("tmp.0"))
                        )
                    )
                ),
                expectedAssembly =
                AsmProgram(
                    AsmFunction(
                        name = "main",
                        body =
                        listOf(
                            AllocateStack(size = 20),
                            Mov(src = Imm(value = 1), dest = Register(name = HardwareRegister.EAX)),
                            Cmp(src = Imm(value = 0), dest = Register(name = HardwareRegister.EAX)),
                            Mov(src = Imm(value = 0), dest = Stack(offset = -4)),
                            SetCC(condition = ConditionCode.E, dest = Stack(offset = -4)),
                            // This is the first jump for the OR operation
                            Cmp(src = Imm(value = 0), dest = Stack(offset = -4)),
                            JmpCC(condition = ConditionCode.NE, label = Label(name = ".L_or_true_0")),
                            // Start of the AND block
                            Mov(src = Imm(value = 5), dest = Register(name = HardwareRegister.EAX)),
                            Cmp(src = Imm(value = 2), dest = Register(name = HardwareRegister.EAX)),
                            Mov(src = Imm(value = 0), dest = Stack(offset = -8)),
                            SetCC(condition = ConditionCode.G, dest = Stack(offset = -8)),
                            Cmp(src = Imm(value = 0), dest = Stack(offset = -8)),
                            JmpCC(condition = ConditionCode.E, label = Label(name = ".L_and_false_2")),
                            Mov(src = Imm(value = 10), dest = Register(name = HardwareRegister.EAX)),
                            Cmp(src = Imm(value = 20), dest = Register(name = HardwareRegister.EAX)),
                            Mov(src = Imm(value = 0), dest = Stack(offset = -12)),
                            SetCC(condition = ConditionCode.LE, dest = Stack(offset = -12)),
                            Cmp(src = Imm(value = 0), dest = Stack(offset = -12)),
                            JmpCC(condition = ConditionCode.E, label = Label(name = ".L_and_false_2")),
                            // AND is true path
                            Mov(src = Imm(value = 1), dest = Stack(offset = -16)),
                            Jmp(label = Label(name = ".L_and_end_3")),
                            // AND is false path
                            Label(name = ".L_and_false_2"),
                            Mov(src = Imm(value = 0), dest = Stack(offset = -16)),
                            Label(name = ".L_and_end_3"),
                            // Resume OR logic, checking the result of the AND block
                            Cmp(src = Imm(value = 0), dest = Stack(offset = -16)),
                            JmpCC(condition = ConditionCode.NE, label = Label(name = ".L_or_true_0")),
                            // OR is false path
                            Mov(src = Imm(value = 0), dest = Stack(offset = -20)),
                            Jmp(label = Label(name = ".L_or_end_1")),
                            // OR is true path
                            Label(name = ".L_or_true_0"),
                            Mov(src = Imm(value = 1), dest = Stack(offset = -20)),
                            Label(name = ".L_or_end_1"),
                            // Final return
                            Mov(src = Stack(offset = -20), dest = Register(name = HardwareRegister.EAX))
                        )
                    )
                )
            )
        )
}
