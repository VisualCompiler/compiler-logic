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
import lexer.Token
import lexer.TokenType
import parser.ASTNode
import parser.AssignmentExpression
import parser.BinaryExpression
import parser.D
import parser.Declaration
import parser.ExpressionStatement
import parser.Function
import parser.IntExpression
import parser.ReturnStatement
import parser.S
import parser.SimpleProgram
import parser.UnaryExpression
import parser.VariableExpression
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
    val expectedTokenList: List<Token>? = null,
    val expectedAst: ASTNode? = null,
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
                    Token(TokenType.KEYWORD_INT, "int", 1, 1),
                    Token(TokenType.IDENTIFIER, "main", 1, 5),
                    Token(TokenType.LEFT_PAREN, "(", 1, 9),
                    Token(TokenType.KEYWORD_VOID, "void", 1, 10),
                    Token(TokenType.RIGHT_PAREN, ")", 1, 14),
                    Token(TokenType.LEFT_BRACK, "{", 2, 2),
                    Token(TokenType.KEYWORD_RETURN, "return", 2, 4),
                    Token(TokenType.LEFT_PAREN, "(", 2, 11),
                    Token(TokenType.INT_LITERAL, "5", 2, 12),
                    Token(TokenType.NEGATION, "-", 2, 14),
                    Token(TokenType.INT_LITERAL, "3", 2, 16),
                    Token(TokenType.RIGHT_PAREN, ")", 2, 17),
                    Token(TokenType.MULTIPLY, "*", 2, 19),
                    Token(TokenType.INT_LITERAL, "4", 2, 21),
                    Token(TokenType.PLUS, "+", 2, 23),
                    Token(TokenType.TILDE, "~", 2, 25),
                    Token(TokenType.LEFT_PAREN, "(", 2, 26),
                    Token(TokenType.NEGATION, "-", 2, 27),
                    Token(TokenType.INT_LITERAL, "5", 2, 28),
                    Token(TokenType.RIGHT_PAREN, ")", 2, 29),
                    Token(TokenType.DIVIDE, "/", 2, 31),
                    Token(TokenType.INT_LITERAL, "6", 2, 33),
                    Token(TokenType.REMAINDER, "%", 2, 35),
                    Token(TokenType.INT_LITERAL, "3", 2, 37),
                    Token(TokenType.SEMICOLON, ";", 2, 38),
                    Token(TokenType.RIGHT_BRACK, "}", 2, 40),
                    Token(TokenType.EOF, "", 2, 41)
                ),
                expectedAst =
                SimpleProgram(
                    functionDefinition =
                    Function(
                        name = "main",
                        body =
                        listOf(
                            S(
                                ReturnStatement(
                                    expression =
                                    BinaryExpression(
                                        left =
                                        BinaryExpression(
                                            left =
                                            BinaryExpression(
                                                left = IntExpression(5),
                                                operator = Token(TokenType.NEGATION, "-", 2, 14),
                                                right = IntExpression(3)
                                            ),
                                            operator = Token(TokenType.MULTIPLY, "*", 2, 19),
                                            right = IntExpression(4)
                                        ),
                                        operator = Token(TokenType.PLUS, "+", 2, 23),
                                        right =
                                        BinaryExpression(
                                            left =
                                            BinaryExpression(
                                                left =
                                                UnaryExpression(
                                                    operator = Token(TokenType.TILDE, "~", 2, 25),
                                                    expression =
                                                    UnaryExpression(
                                                        operator =
                                                        Token(
                                                            TokenType.NEGATION,
                                                            "-",
                                                            2,
                                                            27
                                                        ),
                                                        expression = IntExpression(5)
                                                    )
                                                ),
                                                operator = Token(TokenType.DIVIDE, "/", 2, 31),
                                                right = IntExpression(6)
                                            ),
                                            operator = Token(TokenType.REMAINDER, "%", 2, 35),
                                            right = IntExpression(3)
                                        )
                                    )
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
                            TackyRet(TackyVar("tmp.6")),
                            // Return 0
                            TackyRet(TackyConstant(0))
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
                            // --- Block for tmp.0 = 5 - 3 ---
                            Mov(Imm(5), Stack(-4)),
                            AsmBinary(AsmBinaryOp.SUB, Imm(3), Stack(-4)),
                            // --- Block for tmp.1 = tmp.0 * 4 ---
                            // Note: We use R10D here because the destination of the MOV is a register
                            Mov(Stack(-4), Register(HardwareRegister.R10D)),
                            Mov(Register(HardwareRegister.R10D), Stack(-8)),
                            // --- THIS IS THE KEY CHANGE ---
                            // The fixer sees `imul Imm(4), Stack(-8)` and replaces it
                            Mov(Stack(-8), Register(HardwareRegister.R11D)), // Load dest
                            AsmBinary(AsmBinaryOp.MUL, Imm(4), Register(HardwareRegister.R11D)), // Multiply
                            Mov(Register(HardwareRegister.R11D), Stack(-8)), // Store result
                            // --- The rest of the blocks are now correct based on this change ---
                            // Block for tmp.2 = -5
                            Mov(Imm(5), Stack(-12)),
                            AsmUnary(AsmUnaryOp.NEG, Stack(-12)),
                            // Block for tmp.3 = ~tmp.2
                            Mov(Stack(-12), Register(HardwareRegister.R10D)),
                            Mov(Register(HardwareRegister.R10D), Stack(-16)),
                            AsmUnary(AsmUnaryOp.NOT, Stack(-16)),
                            // Block for tmp.4 = tmp.3 / 6
                            Mov(Stack(-16), Register(HardwareRegister.EAX)),
                            Cdq,
                            // FIXER: The Idiv(Imm(6)) will be fixed here
                            Mov(Imm(6), Register(HardwareRegister.R10D)),
                            Idiv(Register(HardwareRegister.R10D)),
                            Mov(Register(HardwareRegister.EAX), Stack(-20)),
                            // Block for tmp.5 = tmp.4 % 3
                            Mov(Stack(-20), Register(HardwareRegister.EAX)),
                            Cdq,
                            // FIXER: The Idiv(Imm(3)) will be fixed here
                            Mov(Imm(3), Register(HardwareRegister.R10D)),
                            Idiv(Register(HardwareRegister.R10D)),
                            Mov(Register(HardwareRegister.EDX), Stack(-24)),
                            // Block for tmp.6 = tmp.1 + tmp.5
                            Mov(Stack(-8), Register(HardwareRegister.R10D)),
                            Mov(Register(HardwareRegister.R10D), Stack(-28)),
                            Mov(Stack(-24), Register(HardwareRegister.R10D)),
                            AsmBinary(AsmBinaryOp.ADD, Register(HardwareRegister.R10D), Stack(-28)),
                            // Final return
                            Mov(Stack(-28), Register(HardwareRegister.EAX)),
                            Mov(src = Imm(value = 0), dest = Register(name = HardwareRegister.EAX))
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
                            TackyRet(TackyVar("tmp.0")),
                            TackyRet(TackyConstant(0))
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
                            // Block for tmp.1 = (1 == 0)
                            Mov(src = Imm(value = 1), dest = Register(name = HardwareRegister.R11D)),
                            Cmp(src = Imm(value = 0), dest = Register(name = HardwareRegister.R11D)),
                            Mov(src = Imm(value = 0), dest = Stack(offset = -4)),
                            SetCC(condition = ConditionCode.E, dest = Stack(offset = -4)),
                            // First jump for OR: if (tmp.1 != 0) goto .L_or_true_0
                            Cmp(src = Imm(value = 0), dest = Stack(offset = -4)),
                            JmpCC(condition = ConditionCode.NE, label = Label(name = ".L_or_true_0")),
                            // Block for tmp.3 = (5 > 2)
                            Mov(src = Imm(value = 5), dest = Register(name = HardwareRegister.R11D)),
                            Cmp(src = Imm(value = 2), dest = Register(name = HardwareRegister.R11D)),
                            Mov(src = Imm(value = 0), dest = Stack(offset = -8)),
                            SetCC(condition = ConditionCode.G, dest = Stack(offset = -8)),
                            // First jump for AND: if (tmp.3 == 0) goto .L_and_false_2
                            Cmp(src = Imm(value = 0), dest = Stack(offset = -8)),
                            JmpCC(condition = ConditionCode.E, label = Label(name = ".L_and_false_2")),
                            // Block for tmp.4 = (10 <= 20)
                            Mov(src = Imm(value = 10), dest = Register(name = HardwareRegister.R11D)),
                            Cmp(src = Imm(value = 20), dest = Register(name = HardwareRegister.R11D)),
                            Mov(src = Imm(value = 0), dest = Stack(offset = -12)),
                            SetCC(condition = ConditionCode.LE, dest = Stack(offset = -12)),
                            // Second jump for AND: if (tmp.4 == 0) goto .L_and_false_2
                            Cmp(src = Imm(value = 0), dest = Stack(offset = -12)),
                            JmpCC(condition = ConditionCode.E, label = Label(name = ".L_and_false_2")),
                            // AND is true path (tmp.2 = 1)
                            Mov(src = Imm(value = 1), dest = Stack(offset = -16)),
                            Jmp(label = Label(name = ".L_and_end_3")),
                            // AND is false path
                            Label(name = ".L_and_false_2"),
                            Mov(src = Imm(value = 0), dest = Stack(offset = -16)),
                            Label(name = ".L_and_end_3"),
                            // Second jump for OR: if (tmp.2 != 0) goto .L_or_true_0
                            Cmp(src = Imm(value = 0), dest = Stack(offset = -16)),
                            JmpCC(condition = ConditionCode.NE, label = Label(name = ".L_or_true_0")),
                            // OR is false path (tmp.0 = 0)
                            Mov(src = Imm(value = 0), dest = Stack(offset = -20)),
                            Jmp(label = Label(name = ".L_or_end_1")),
                            // OR is true path
                            Label(name = ".L_or_true_0"),
                            Mov(src = Imm(value = 1), dest = Stack(offset = -20)),
                            Label(name = ".L_or_end_1"),
                            // Final return tmp.0
                            Mov(src = Stack(offset = -20), dest = Register(name = HardwareRegister.EAX)),
                            Mov(src = Imm(value = 0), dest = Register(name = HardwareRegister.EAX))
                        )
                    )
                )
            ),
            ValidTestCase(
                title = "Local variables",
                code =
                """int main(void) { 
                    |int b; 
                    |int a = 10 + 1;
                    |b = (a=2) * 2;
                    |return b; 
                    |}
                """.trimMargin(),
                expectedTokenList =
                listOf(
                    Token(TokenType.KEYWORD_INT, "int", 1, 1),
                    Token(TokenType.IDENTIFIER, "main", 1, 5),
                    Token(TokenType.LEFT_PAREN, "(", 1, 9),
                    Token(TokenType.KEYWORD_VOID, "void", 1, 10),
                    Token(TokenType.RIGHT_PAREN, ")", 1, 14),
                    Token(TokenType.LEFT_BRACK, "{", 1, 16),
                    // int b;
                    Token(TokenType.KEYWORD_INT, "int", 2, 1),
                    Token(TokenType.IDENTIFIER, "b", 2, 5),
                    Token(TokenType.SEMICOLON, ";", 2, 6),
                    // int a = 10 + 1;
                    Token(TokenType.KEYWORD_INT, "int", 3, 1),
                    Token(TokenType.IDENTIFIER, "a", 3, 5),
                    Token(TokenType.ASSIGN, "=", 3, 7),
                    Token(TokenType.INT_LITERAL, "10", 3, 9),
                    Token(TokenType.PLUS, "+", 3, 12),
                    Token(TokenType.INT_LITERAL, "1", 3, 14),
                    Token(TokenType.SEMICOLON, ";", 3, 15),
                    // b = (a=2) * 2;
                    Token(TokenType.IDENTIFIER, "b", 4, 1),
                    Token(TokenType.ASSIGN, "=", 4, 3),
                    Token(TokenType.LEFT_PAREN, "(", 4, 5),
                    Token(TokenType.IDENTIFIER, "a", 4, 6),
                    Token(TokenType.ASSIGN, "=", 4, 7),
                    Token(TokenType.INT_LITERAL, "2", 4, 8),
                    Token(TokenType.RIGHT_PAREN, ")", 4, 9),
                    Token(TokenType.MULTIPLY, "*", 4, 11),
                    Token(TokenType.INT_LITERAL, "2", 4, 13),
                    Token(TokenType.SEMICOLON, ";", 4, 14),
                    // return b;
                    Token(TokenType.KEYWORD_RETURN, "return", 5, 1),
                    Token(TokenType.IDENTIFIER, "b", 5, 8),
                    Token(TokenType.SEMICOLON, ";", 5, 9),
                    Token(TokenType.RIGHT_BRACK, "}", 6, 1),
                    Token(TokenType.EOF, "", 6, 2)
                ),
                expectedAst =
                SimpleProgram(
                    functionDefinition =
                    Function(
                        name = "main",
                        body =
                        listOf(
                            D(Declaration(name = "b.0", init = null)),
                            D(
                                Declaration(
                                    name = "a.1",
                                    init =
                                    BinaryExpression(
                                        left = IntExpression(10),
                                        operator = Token(TokenType.PLUS, "+", 3, 12),
                                        right = IntExpression(1)
                                    )
                                )
                            ),
                            S(
                                ExpressionStatement(
                                    AssignmentExpression(
                                        lvalue = VariableExpression("b.0"),
                                        rvalue =
                                        BinaryExpression(
                                            left =
                                            AssignmentExpression(
                                                lvalue = VariableExpression("a.1"),
                                                rvalue = IntExpression(2)
                                            ),
                                            operator = Token(TokenType.MULTIPLY, "*", 4, 11),
                                            right = IntExpression(2)
                                        )
                                    )
                                )
                            ),
                            S(
                                ReturnStatement(
                                    expression = VariableExpression("b.0")
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
                            TackyBinary(TackyBinaryOP.ADD, TackyConstant(10), TackyConstant(1), TackyVar("tmp.0")),
                            TackyCopy(TackyVar("tmp.0"), TackyVar("a.1")),
                            TackyCopy(TackyConstant(2), TackyVar("a.1")),
                            TackyBinary(TackyBinaryOP.MULTIPLY, TackyVar("a.1"), TackyConstant(2), TackyVar("tmp.1")),
                            TackyCopy(TackyVar("tmp.1"), TackyVar("b.0")),
                            TackyRet(TackyVar("b.0")),
                            TackyRet(TackyConstant(0))
                        )
                    )
                ),
                expectedAssembly = null
            )
        )
}
// <SimpleProgram(functionDefinition=Function(name=main, body=[D(declaration=Declaration(name=b, init=null)), D(declaration=Declaration(name=a, init=BinaryExpression(left=IntExpression(value=10), operator=Token(type=PLUS, lexeme=+, line=1, column=1), right=IntExpression(value=1)))), S(statement=ExpressionStatement(expression=AssignmentExpression(lvalue=VariableExpression(name=b), rvalue=BinaryExpression(left=AssignmentExpression(lvalue=VariableExpression(name=a), rvalue=IntExpression(value=2)), operator=Token(type=MULTIPLY, lexeme=*, line=1, column=1), right=IntExpression(value=2))))), S(statement=ReturnStatement(expression=VariableExpression(name=b)))]))>, actual
// <SimpleProgram(functionDefinition=Function(name=main, body=[D(declaration=Declaration(name=b, init=null)), D(declaration=Declaration(name=a, init=BinaryExpression(left=IntExpression(value=10), operator=Token(type=PLUS, lexeme=+, line=3, column=12), right=IntExpression(value=1)))), S(statement=ExpressionStatement(expression=AssignmentExpression(lvalue=VariableExpression(name=b), rvalue=BinaryExpression(left=AssignmentExpression(lvalue=VariableExpression(name=a), rvalue=IntExpression(value=2)), operator=Token(type=MULTIPLY, lexeme=*, line=4, column=11), right=IntExpression(value=2))))), S(statement=ReturnStatement(expression=VariableExpression(name=b)))]))>.
// <TackyProgram(function=TackyFunction(name=main, body=[TackyBinary(operator=ADD, src1=TackyConstant(value=10), src2=TackyConstant(value=1), dest=TackyVar(name=tmp.0)), TackyCopy(src=TackyVar(name=tmp.0), dest=TackyVar(name=var.1)), TackyCopy(src=TackyConstant(value=2), dest=TackyVar(name=var.1)), TackyBinary(operator=MULTIPLY, src1=TackyVar(name=var.1), src2=TackyConstant(value=2), dest=TackyVar(name=tmp.1)), TackyCopy(src=TackyVar(name=tmp.1), dest=TackyVar(name=var.0)), TackyRet(value=TackyVar(name=var.0))]))>, actual
// <TackyProgram(function=TackyFunction(name=main, body=[TackyBinary(operator=ADD, src1=TackyConstant(value=10), src2=TackyConstant(value=1), dest=TackyVar(name=tmp.0)), TackyCopy(src=TackyVar(name=tmp.0), dest=TackyVar(name=var.1)), TackyCopy(src=TackyConstant(value=2), dest=TackyVar(name=a)), TackyBinary(operator=MULTIPLY, src1=TackyVar(name=a), src2=TackyConstant(value=2), dest=TackyVar(name=tmp.1)), TackyCopy(src=TackyVar(name=tmp.1), dest=TackyVar(name=b)), TackyRet(value=TackyVar(name=b)), TackyRet(value=TackyConstant(value=0))]))>.
// <TackyProgram(function=TackyFunction(name=main, body=[TackyBinary(operator=ADD, src1=TackyConstant(value=10), src2=TackyConstant(value=1), dest=TackyVar(name=tmp.0)), TackyCopy(src=TackyVar(name=tmp.0), dest=TackyVar(name=var.1)), TackyCopy(src=TackyConstant(value=2), dest=TackyVar(name=var.1)), TackyBinary(operator=MULTIPLY, src1=TackyVar(name=var.1), src2=TackyConstant(value=2), dest=TackyVar(name=tmp.1)), TackyCopy(src=TackyVar(name=tmp.1), dest=TackyVar(name=var.0)), TackyRet(value=TackyVar(name=var.0)), TackyRet(value=TackyConstant(value=0))]))>, actual
// <TackyProgram(function=TackyFunction(name=main, body=[TackyBinary(operator=ADD, src1=TackyConstant(value=10), src2=TackyConstant(value=1), dest=TackyVar(name=tmp.0)), TackyCopy(src=TackyVar(name=tmp.0), dest=TackyVar(name=a)), TackyCopy(src=TackyConstant(value=2), dest=TackyVar(name=tmp.1)), TackyBinary(operator=MULTIPLY, src1=TackyVar(name=tmp.1), src2=TackyConstant(value=2), dest=TackyVar(name=tmp.2)), TackyCopy(src=TackyVar(name=tmp.2), dest=TackyVar(name=tmp.3)), TackyRet(value=TackyVar(name=b)), TackyRet(value=TackyConstant(value=0))]))>.
// AssertionError: Expected <TackyProgram(function=TackyFunction(name=main, body=[TackyBinary(operator=ADD, src1=TackyConstant(value=10), src2=TackyConstant(value=1), dest=TackyVar(name=tmp.0)), TackyCopy(src=TackyVar(name=tmp.0), dest=TackyVar(name=a.1)), TackyCopy(src=TackyConstant(value=2), dest=TackyVar(name=a.1)), TackyBinary(operator=MULTIPLY, src1=TackyVar(name=a.1), src2=TackyConstant(value=2), dest=TackyVar(name=tmp.1)), TackyCopy(src=TackyVar(name=tmp.1), dest=TackyVar(name=b.0)), TackyRet(value=TackyVar(name=b.0)), TackyRet(value=TackyConstant(value=0))]))>, actual <TackyProgram(function=TackyFunction(name=main, body=[TackyBinary(operator=ADD, src1=TackyConstant(value=10), src2=TackyConstant(value=1), dest=TackyVar(name=tmp.0)), TackyCopy(src=TackyVar(name=tmp.0), dest=TackyVar(name=a)), TackyCopy(src=TackyConstant(value=2), dest=TackyVar(name=a)), TackyBinary(operator=MULTIPLY, src1=TackyVar(name=a), src2=TackyConstant(value=2), dest=TackyVar(name=tmp.1)), TackyCopy(src=TackyVar(name=tmp.1), dest=TackyVar(name=b)), TackyRet(value=TackyVar(name=b)), TackyRet(value=TackyConstant(value=0))]))>.
