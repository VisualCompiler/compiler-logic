package integration

import assembly.AllocateStack
import assembly.AsmBinary
import assembly.AsmBinaryOp
import assembly.AsmFunction
import assembly.AsmProgram
import assembly.AsmUnary
import assembly.AsmUnaryOp
import assembly.Call
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
import assembly.Ret
import assembly.SetCC
import assembly.Stack
import lexer.Token
import lexer.TokenType
import parser.ASTNode
import parser.AssignmentExpression
import parser.BinaryExpression
import parser.Block
import parser.D
import parser.DoWhileStatement
import parser.ExpressionStatement
import parser.ForStatement
import parser.FunctionDeclaration
import parser.InitDeclaration
import parser.InitExpression
import parser.IntExpression
import parser.NullStatement
import parser.ReturnStatement
import parser.S
import parser.SimpleProgram
import parser.UnaryExpression
import parser.VarDecl
import parser.VariableDeclaration
import parser.VariableExpression
import parser.WhileStatement
import tacky.JumpIfNotZero
import tacky.JumpIfZero
import tacky.TackyBinary
import tacky.TackyBinaryOP
import tacky.TackyConstant
import tacky.TackyCopy
import tacky.TackyFunCall
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
                    functionDeclaration =
                    listOf(
                        FunctionDeclaration(
                            name = "main",
                            params = emptyList(),
                            body =
                            Block(
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
                        )
                    )
                ),
                expectedTacky =
                TackyProgram(
                    functions =
                    listOf( // TackyProgram now holds a LIST of functions
                        TackyFunction(
                            name = "main",
                            args = emptyList(), // No parameters for main
                            body =
                            listOf(
                                // (5 - 3) -> tmp.0
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
                    )
                ),
                //
                expectedAssembly =
                AsmProgram(
                    functions =
                    listOf( // AsmProgram now holds a LIST of functions
                        AsmFunction(
                            name = "main",
                            stackSize = 28, // 7 temporary variables * 4 bytes
                            body =
                            listOf(
                                AllocateStack(32), // 28 rounded up to 16
                                // tmp.0 = 5 - 3
                                Mov(Imm(5), Stack(-4)),
                                AsmBinary(AsmBinaryOp.SUB, Imm(3), Stack(-4)),
                                // tmp.1 = tmp.0 * 4
                                Mov(Stack(-4), Register(HardwareRegister.R10D)),
                                Mov(Register(HardwareRegister.R10D), Stack(-8)),
                                Mov(Stack(-8), Register(HardwareRegister.R11D)),
                                AsmBinary(AsmBinaryOp.MUL, Imm(4), Register(HardwareRegister.R11D)),
                                Mov(Register(HardwareRegister.R11D), Stack(-8)),
                                // tmp.2 = -5
                                Mov(Imm(5), Stack(-12)),
                                AsmUnary(AsmUnaryOp.NEG, Stack(-12)),
                                // tmp.3 = ~tmp.2
                                Mov(Stack(-12), Register(HardwareRegister.R10D)),
                                Mov(Register(HardwareRegister.R10D), Stack(-16)),
                                AsmUnary(AsmUnaryOp.NOT, Stack(-16)),
                                // tmp.4 = tmp.3 / 6
                                Mov(Stack(-16), Register(HardwareRegister.EAX)),
                                Cdq,
                                Mov(Imm(6), Register(HardwareRegister.R10D)),
                                Idiv(Register(HardwareRegister.R10D)),
                                Mov(Register(HardwareRegister.EAX), Stack(-20)),
                                // tmp.5 = tmp.4 % 3
                                Mov(Stack(-20), Register(HardwareRegister.EAX)),
                                Cdq,
                                Mov(Imm(3), Register(HardwareRegister.R10D)),
                                Idiv(Register(HardwareRegister.R10D)),
                                Mov(Register(HardwareRegister.EDX), Stack(-24)),
                                // tmp.6 = tmp.1 + tmp.5
                                Mov(Stack(-8), Register(HardwareRegister.R10D)),
                                Mov(Register(HardwareRegister.R10D), Stack(-28)),
                                Mov(Stack(-24), Register(HardwareRegister.R10D)),
                                AsmBinary(AsmBinaryOp.ADD, Register(HardwareRegister.R10D), Stack(-28)),
                                // return tmp.6
                                Mov(Stack(-28), Register(HardwareRegister.EAX)),
                                Ret
                                // The implicit return 0
                            )
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
                    functions =
                    listOf( // TackyProgram holds a LIST of functions
                        TackyFunction(
                            name = "main",
                            args = emptyList(), // No parameters for main
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
                                // OR is false -> tmp.0 = 0 (using a new final temp)
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
                    )
                ),
                expectedAssembly =
                AsmProgram(
                    functions =
                    listOf( // AsmProgram holds a LIST of functions
                        AsmFunction(
                            name = "main",
                            stackSize = 20, // 5 temporary variables * 4 bytes
                            body =
                            listOf(
                                AllocateStack(32), // 20 rounded up to 16 is 32
                                // tmp.1 = (1 == 0)
                                Mov(Imm(1), Register(HardwareRegister.R11D)),
                                Cmp(Imm(0), Register(HardwareRegister.R11D)),
                                Mov(Imm(0), Stack(-4)),
                                SetCC(ConditionCode.E, Stack(-4)),
                                // if (tmp.1 != 0) goto .L_or_true_0
                                Cmp(Imm(0), Stack(-4)),
                                JmpCC(ConditionCode.NE, Label(".L_or_true_0")),
                                // tmp.3 = (5 > 2)
                                Mov(Imm(5), Register(HardwareRegister.R11D)),
                                Cmp(Imm(2), Register(HardwareRegister.R11D)),
                                Mov(Imm(0), Stack(-8)),
                                SetCC(ConditionCode.G, Stack(-8)),
                                // if (tmp.3 == 0) goto .L_and_false_2
                                Cmp(Imm(0), Stack(-8)),
                                JmpCC(ConditionCode.E, Label(".L_and_false_2")),
                                // tmp.4 = (10 <= 20)
                                Mov(Imm(10), Register(HardwareRegister.R11D)),
                                Cmp(Imm(20), Register(HardwareRegister.R11D)),
                                Mov(Imm(0), Stack(-12)),
                                SetCC(ConditionCode.LE, Stack(-12)),
                                // if (tmp.4 == 0) goto .L_and_false_2
                                Cmp(Imm(0), Stack(-12)),
                                JmpCC(ConditionCode.E, Label(".L_and_false_2")),
                                // tmp.2 = 1 (AND is true)
                                Mov(Imm(1), Stack(-16)),
                                Jmp(Label(".L_and_end_3")),
                                // .L_and_false_2: (AND is false)
                                Label(".L_and_false_2"),
                                Mov(Imm(0), Stack(-16)),
                                // .L_and_end_3:
                                Label(".L_and_end_3"),
                                // if (tmp.2 != 0) goto .L_or_true_0
                                Cmp(Imm(0), Stack(-16)),
                                JmpCC(ConditionCode.NE, Label(".L_or_true_0")),
                                // tmp.0 = 0 (OR is false)
                                Mov(Imm(0), Stack(-20)),
                                Jmp(Label(".L_or_end_1")),
                                // .L_or_true_0: (OR is true)
                                Label(".L_or_true_0"),
                                Mov(Imm(1), Stack(-20)),
                                // .L_or_end_1:
                                Label(".L_or_end_1"),
                                // return tmp.0
                                Mov(Stack(-20), Register(HardwareRegister.EAX)),
                                Ret
                            )
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
                    |;
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
                    Token(TokenType.SEMICOLON, ";", 6, 1),
                    Token(TokenType.RIGHT_BRACK, "}", 7, 1),
                    Token(TokenType.EOF, "", 7, 2)
                ),
                expectedAst =
                SimpleProgram(
                    functionDeclaration =
                    listOf(
                        FunctionDeclaration(
                            name = "main",
                            params = emptyList(),
                            body = Block(
                                block =
                                listOf(
                                    D(VarDecl(VariableDeclaration(name = "b.0", init = null))),
                                    D(
                                        VarDecl(
                                            VariableDeclaration(
                                                name = "a.1",
                                                init =
                                                BinaryExpression(
                                                    left = IntExpression(10),
                                                    operator = Token(TokenType.PLUS, "+", 3, 12),
                                                    right = IntExpression(1)
                                                )
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
                                    ),
                                    S(NullStatement())
                                )
                            )
                        )
                    )
                ),
                expectedTacky =
                TackyProgram(
                    functions =
                    listOf(
                        TackyFunction(
                            name = "main",
                            // No parameters for main function
                            args = emptyList(),
                            body =
                            listOf(
                                TackyBinary(TackyBinaryOP.ADD, TackyConstant(10), TackyConstant(1), TackyVar("tmp.0")),
                                TackyCopy(TackyVar("tmp.0"), TackyVar("a.1")),
                                TackyCopy(TackyConstant(2), TackyVar("a.1")),
                                TackyBinary(TackyBinaryOP.MULTIPLY, TackyVar("a.1"), TackyConstant(2), TackyVar("tmp.1")),
                                TackyCopy(TackyVar("tmp.1"), TackyVar("b.0")),
                                TackyRet(TackyVar("b.0"))
                                // TackyRet(TackyConstant(0))
                            )
                        )
                    )
                    // No need to test the assembly here since this feature doesn't effect the assembly generation stage
                )
            ),
            // --- Test Case for an IF-ELSE statement ---
            ValidTestCase(
                title = "Testing an if-else statement.",
                code =
                """
                    int main(void) {
                        int a = 0;
                        if (a == 0)
                            return 10;
                        else
                            return 20;
                    }
                """.trimIndent(),
                expectedTacky =
                TackyProgram(
                    functions =
                    listOf(
                        TackyFunction(
                            name = "main",
                            args = emptyList(),
                            body =
                            listOf(
                                // int a = 0;
                                TackyCopy(TackyConstant(0), TackyVar("a.0")),
                                // tmp.0 = a == 0
                                TackyBinary(TackyBinaryOP.EQUAL, TackyVar("a.0"), TackyConstant(0), TackyVar("tmp.0")),
                                // if (tmp.0 == 0) goto .L_else_label_1
                                JumpIfZero(TackyVar("tmp.0"), TackyLabel(".L_else_label_1")),
                                // then block: return 10;
                                TackyRet(TackyConstant(10)),
                                // goto .L_end_0;
                                TackyJump(TackyLabel(".L_end_0")),
                                // else block
                                TackyLabel(".L_else_label_1"),
                                TackyRet(TackyConstant(20)),
                                // end of if
                                TackyLabel(".L_end_0"),
                                // implicit return 0
                                TackyRet(TackyConstant(0))
                            )
                        )
                    )
                ),
                expectedAssembly =
                AsmProgram(
                    functions =
                    listOf(
                        AsmFunction(
                            name = "main",
                            stackSize = 8,
                            body =
                            listOf(
                                AllocateStack(16),
                                // int a = 0;
                                Mov(Imm(0), Stack(-4)),
                                // tmp.0 = a == 0;
                                Cmp(Imm(0), Stack(-4)),
                                Mov(Imm(0), Stack(-8)),
                                SetCC(ConditionCode.E, Stack(-8)),
                                // if (tmp.0 == 0) goto .L_else_label_1
                                Cmp(Imm(0), Stack(-8)),
                                JmpCC(ConditionCode.E, Label(".L_else_label_1")),
                                // return 10;
                                Mov(Imm(10), Register(HardwareRegister.EAX)),
                                Ret,
                                Jmp(Label(".L_end_0")),
                                // .L_else_label_1:
                                Label(".L_else_label_1"),
                                // return 20;
                                Mov(Imm(20), Register(HardwareRegister.EAX)),
                                Ret,
                                // .L_end_0:
                                Label(".L_end_0"),
                                // implicit return 0
                                Mov(Imm(0), Register(HardwareRegister.EAX)),
                                Ret
                            )
                        )
                    )
                )
            ),
// --- Test Case for GOTO and LABELS ---
            ValidTestCase(
                title = "Testing goto and labeled statements.",
                code =
                """
                    int main(void) {
                        int a = 0;
                    start:
                        a = a + 1;
                        if (a < 3)
                            goto start;
                        return a;
                    }
                """.trimIndent(),
                expectedTacky =
                TackyProgram(
                    functions =
                    listOf( // TackyProgram holds a LIST of functions
                        TackyFunction(
                            name = "main",
                            args = emptyList(), // No parameters for main function
                            body =
                            listOf(
                                // int a = 0;
                                TackyCopy(TackyConstant(0), TackyVar("a.0")),
                                // start:
                                TackyLabel("start"),
                                // tmp.0 = a + 1;
                                TackyBinary(TackyBinaryOP.ADD, TackyVar("a.0"), TackyConstant(1), TackyVar("tmp.0")),
                                // a = tmp.0
                                TackyCopy(TackyVar("tmp.0"), TackyVar("a.0")),
                                // tmp.1 = a < 3
                                TackyBinary(TackyBinaryOP.LESS, TackyVar("a.0"), TackyConstant(3), TackyVar("tmp.1")),
                                // if (tmp.1 == 0) goto .L_end_0;
                                JumpIfZero(TackyVar("tmp.1"), TackyLabel(".L_end_0")),
                                // goto start;
                                TackyJump(TackyLabel("start")),
                                // end of if
                                TackyLabel(".L_end_0"),
                                // return a;
                                TackyRet(TackyVar("a.0"))
                                // TackyRet(TackyConstant(0))
                            )
                        )
                    )
                ),
                expectedAssembly =
                AsmProgram(
                    functions =
                    listOf( // AsmProgram holds a LIST of functions
                        AsmFunction(
                            name = "main",
                            stackSize = 12, // 1 variable (a) + 2 temporaries (tmp.0, tmp.1) = 3 * 4 = 12
                            body =
                            listOf(
                                AllocateStack(16), // 12 rounded up to nearest 16
                                // a = 0
                                Mov(Imm(0), Stack(-4)), // Stack slot for a.0
                                // start:
                                Label("start"),
                                // tmp.0 = a + 1
                                Mov(Stack(-4), Register(HardwareRegister.R10D)),
                                Mov(Register(HardwareRegister.R10D), Stack(-8)), // Stack slot for tmp.0
                                AsmBinary(AsmBinaryOp.ADD, Imm(1), Stack(-8)),
                                // a = tmp.0
                                Mov(Stack(-8), Register(HardwareRegister.R10D)),
                                Mov(Register(HardwareRegister.R10D), Stack(-4)),
                                // tmp.1 = a < 3
                                Cmp(Imm(3), Stack(-4)),
                                Mov(Imm(0), Stack(-12)), // Stack slot for tmp.1
                                SetCC(ConditionCode.L, Stack(-12)),
                                // if (tmp.1 == 0) goto .L_end_0
                                Cmp(Imm(0), Stack(-12)),
                                JmpCC(ConditionCode.E, Label(".L_end_0")),
                                // goto start
                                Jmp(Label("start")),
                                // .L_end_0:
                                Label(".L_end_0"),
                                // return a
                                Mov(Stack(-4), Register(HardwareRegister.EAX)),
                                Ret
                            )
                        )
                    )
                )
            ),
// --- Test Case for MULTIPLE FUNCTIONS with FUNCTION CALLS ---
            ValidTestCase(
                title = "Testing multiple functions with function declarations and function calls.",
                code =
                """
                    int add(int a, int b) {
                        return a + b;
                    }
                    
                    int main(void) {
                        int result = add(5, 3);
                        return result;
                    }
                """.trimIndent(),
                expectedTacky =
                TackyProgram(
                    functions =
                    listOf(
                        TackyFunction(
                            name = "add",
                            args = listOf("a.0", "b.1"),
                            body =
                            listOf(
                                // return a + b;
                                TackyBinary(TackyBinaryOP.ADD, TackyVar("a.0"), TackyVar("b.1"), TackyVar("tmp.0")),
                                TackyRet(TackyVar("tmp.0"))
                            )
                        ),
                        TackyFunction(
                            name = "main",
                            args = emptyList(),
                            body =
                            listOf(
                                // int result = add(5, 3);
                                TackyFunCall("add", listOf(TackyConstant(5), TackyConstant(3)), TackyVar("tmp.1")),
                                TackyCopy(TackyVar("tmp.1"), TackyVar("result.2")),
                                // return result;
                                TackyRet(TackyVar("result.2"))
                            )
                        )
                    )
                ),
                expectedAssembly =
                AsmProgram(
                    functions =
                    listOf(
                        AsmFunction(
                            name = "add",
                            stackSize = 12,
                            body =
                            listOf(
                                AllocateStack(16),
                                // Parameter setup: a.0 -> Stack(-4), b.1 -> Stack(-8)
                                Mov(Register(HardwareRegister.EDI), Stack(-4)),
                                Mov(Register(HardwareRegister.ESI), Stack(-8)),
                                // tmp.0 = a + b (using R10D for temporary)
                                Mov(Stack(-4), Register(HardwareRegister.R10D)),
                                Mov(Register(HardwareRegister.R10D), Stack(-12)),
                                Mov(Stack(-8), Register(HardwareRegister.R10D)),
                                AsmBinary(AsmBinaryOp.ADD, Register(HardwareRegister.R10D), Stack(-12)),
                                // return tmp.0
                                Mov(Stack(-12), Register(HardwareRegister.EAX)),
                                Ret
                            )
                        ),
                        AsmFunction(
                            name = "main",
                            stackSize = 8,
                            body =
                            listOf(
                                AllocateStack(16),
                                // Call add(5, 3)
                                Mov(Imm(5), Register(HardwareRegister.EDI)),
                                Mov(Imm(3), Register(HardwareRegister.ESI)),
                                Call("add"),
                                // Store result in result.2
                                Mov(Register(HardwareRegister.EAX), Stack(-4)),
                                // Copy result to R10D for intermediate storage
                                Mov(Stack(-4), Register(HardwareRegister.R10D)),
                                Mov(Register(HardwareRegister.R10D), Stack(-8)),
                                // return result
                                Mov(Stack(-8), Register(HardwareRegister.EAX)),
                                Ret
                            )
                        )
                    )
                )
            ),
// --- Test Case for FUNCTION DECLARATION WITHOUT BODY ---
            ValidTestCase(
                title = "Testing function declaration without body (function prototype) at global level.",
                code =
                """
                    int foo(void);
                    
                    int foo(void) {
                        return 5;
                    }
                    
                    int main(void) {
                        int foo(void);
                        int foo(void);
                        return foo();
                    }
                """.trimIndent(),
                expectedTacky =
                TackyProgram(
                    functions =
                    listOf(
                        TackyFunction(
                            name = "foo",
                            args = emptyList(),
                            body =
                            listOf(
                                // return 5;
                                TackyRet(TackyConstant(5))
                            )
                        ),
                        TackyFunction(
                            name = "main",
                            args = emptyList(),
                            body =
                            listOf(
                                // Implicit return for function without explicit return at start
                                TackyRet(TackyConstant(0)),
                                // int foo(void); (function prototype - no Tacky instructions)
                                // int foo(void); (function prototype - no Tacky instructions)
                                // return foo();
                                TackyFunCall("foo", emptyList(), TackyVar("tmp.0")),
                                TackyRet(TackyVar("tmp.0"))
                            )
                        )
                    )
                ),
                expectedAssembly =
                AsmProgram(
                    functions =
                    listOf(
                        AsmFunction(
                            name = "foo",
                            stackSize = 0,
                            body =
                            listOf(
                                // return 5;
                                Mov(Imm(5), Register(HardwareRegister.EAX)),
                                Ret
                            )
                        ),
                        AsmFunction(
                            name = "main",
                            stackSize = 4,
                            body =
                            listOf(
                                AllocateStack(16),
                                // Implicit return 0
                                Mov(Imm(0), Register(HardwareRegister.EAX)),
                                Ret,
                                // Call foo() (no arguments)
                                Call("foo"),
                                // Store result and return it
                                Mov(Register(HardwareRegister.EAX), Stack(-4)),
                                Mov(Stack(-4), Register(HardwareRegister.EAX)),
                                Ret
                            )
                        )
                    )
                )
            ),
// --- Test Case for LOOP STATEMENTS ---
            ValidTestCase(
                title = "Testing loop statements",
                code =
                """int main(void) { 
    |int a = 10;
    |while(a > 0)
    |   a = a - 1;
    |for(int i = 0; i < 10; i = i + 1) 
    |   a = a + 1;
    |for(;a > 0;)
    |   a = a + 1;
    |do 
    |   a = a + 1;
    |while(a > 0);
    |return 0;
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
                    // int a = 10;
                    Token(TokenType.KEYWORD_INT, "int", 2, 1),
                    Token(TokenType.IDENTIFIER, "a", 2, 5),
                    Token(TokenType.ASSIGN, "=", 2, 7),
                    Token(TokenType.INT_LITERAL, "10", 2, 9),
                    Token(TokenType.SEMICOLON, ";", 2, 11),
                    // while(a > 0)
                    Token(TokenType.KEYWORD_WHILE, "while", 3, 1),
                    Token(TokenType.LEFT_PAREN, "(", 3, 6),
                    Token(TokenType.IDENTIFIER, "a", 3, 7),
                    Token(TokenType.GREATER, ">", 3, 9),
                    Token(TokenType.INT_LITERAL, "0", 3, 11),
                    Token(TokenType.RIGHT_PAREN, ")", 3, 12),
                    // a = a - 1;
                    Token(TokenType.IDENTIFIER, "a", 4, 4),
                    Token(TokenType.ASSIGN, "=", 4, 6),
                    Token(TokenType.IDENTIFIER, "a", 4, 8),
                    Token(TokenType.NEGATION, "-", 4, 10),
                    Token(TokenType.INT_LITERAL, "1", 4, 12),
                    Token(TokenType.SEMICOLON, ";", 4, 13),
                    // for(int i = 0; i < 10; i = i + 1)
                    Token(TokenType.KEYWORD_FOR, "for", 5, 1),
                    Token(TokenType.LEFT_PAREN, "(", 5, 4),
                    Token(TokenType.KEYWORD_INT, "int", 5, 5),
                    Token(TokenType.IDENTIFIER, "i", 5, 9),
                    Token(TokenType.ASSIGN, "=", 5, 11),
                    Token(TokenType.INT_LITERAL, "0", 5, 13),
                    Token(TokenType.SEMICOLON, ";", 5, 14),
                    Token(TokenType.IDENTIFIER, "i", 5, 16),
                    Token(TokenType.LESS, "<", 5, 18),
                    Token(TokenType.INT_LITERAL, "10", 5, 20),
                    Token(TokenType.SEMICOLON, ";", 5, 22),
                    Token(TokenType.IDENTIFIER, "i", 5, 24),
                    Token(TokenType.ASSIGN, "=", 5, 26),
                    Token(TokenType.IDENTIFIER, "i", 5, 28),
                    Token(TokenType.PLUS, "+", 5, 30),
                    Token(TokenType.INT_LITERAL, "1", 5, 32),
                    Token(TokenType.RIGHT_PAREN, ")", 5, 33),
                    // a = a + 1;
                    Token(TokenType.IDENTIFIER, "a", 6, 4),
                    Token(TokenType.ASSIGN, "=", 6, 6),
                    Token(TokenType.IDENTIFIER, "a", 6, 8),
                    Token(TokenType.PLUS, "+", 6, 10),
                    Token(TokenType.INT_LITERAL, "1", 6, 12),
                    Token(TokenType.SEMICOLON, ";", 6, 13),
                    // for(;a > 0;)
                    Token(TokenType.KEYWORD_FOR, "for", 7, 1),
                    Token(TokenType.LEFT_PAREN, "(", 7, 4),
                    Token(TokenType.SEMICOLON, ";", 7, 5),
                    Token(TokenType.IDENTIFIER, "a", 7, 6),
                    Token(TokenType.GREATER, ">", 7, 8),
                    Token(TokenType.INT_LITERAL, "0", 7, 10),
                    Token(TokenType.SEMICOLON, ";", 7, 11),
                    Token(TokenType.RIGHT_PAREN, ")", 7, 12),
                    // a = a + 1;
                    Token(TokenType.IDENTIFIER, "a", 8, 4),
                    Token(TokenType.ASSIGN, "=", 8, 6),
                    Token(TokenType.IDENTIFIER, "a", 8, 8),
                    Token(TokenType.PLUS, "+", 8, 10),
                    Token(TokenType.INT_LITERAL, "1", 8, 12),
                    Token(TokenType.SEMICOLON, ";", 8, 13),
                    // do
                    Token(TokenType.KEYWORD_DO, "do", 9, 1),
                    // a = a + 1;
                    Token(TokenType.IDENTIFIER, "a", 10, 4),
                    Token(TokenType.ASSIGN, "=", 10, 6),
                    Token(TokenType.IDENTIFIER, "a", 10, 8),
                    Token(TokenType.PLUS, "+", 10, 10),
                    Token(TokenType.INT_LITERAL, "1", 10, 12),
                    Token(TokenType.SEMICOLON, ";", 10, 13),
                    // while(a > 0);
                    Token(TokenType.KEYWORD_WHILE, "while", 11, 1),
                    Token(TokenType.LEFT_PAREN, "(", 11, 6),
                    Token(TokenType.IDENTIFIER, "a", 11, 7),
                    Token(TokenType.GREATER, ">", 11, 9),
                    Token(TokenType.INT_LITERAL, "0", 11, 11),
                    Token(TokenType.RIGHT_PAREN, ")", 11, 12),
                    Token(TokenType.SEMICOLON, ";", 11, 13),
                    // return 0;
                    Token(TokenType.KEYWORD_RETURN, "return", 12, 1),
                    Token(TokenType.INT_LITERAL, "0", 12, 8),
                    Token(TokenType.SEMICOLON, ";", 12, 9),
                    Token(TokenType.RIGHT_BRACK, "}", 13, 1),
                    Token(TokenType.EOF, "", 13, 2)
                ),
                expectedAst =
                SimpleProgram(
                    functionDeclaration = listOf(
                        FunctionDeclaration(
                            name = "main",
                            params = emptyList(),
                            body = Block(
                                block =
                                listOf(
                                    D(VarDecl(VariableDeclaration(name = "a.0", init = IntExpression(10)))),
                                    S(
                                        WhileStatement(
                                            condition =
                                            BinaryExpression(
                                                left = VariableExpression("a.0"),
                                                operator = Token(TokenType.GREATER, ">", 3, 9),
                                                right = IntExpression(0)
                                            ),
                                            body =
                                            ExpressionStatement(
                                                AssignmentExpression(
                                                    lvalue = VariableExpression("a.0"),
                                                    rvalue =
                                                    BinaryExpression(
                                                        left = VariableExpression("a.0"),
                                                        operator = Token(TokenType.NEGATION, "-", 4, 10),
                                                        right = IntExpression(1)
                                                    )
                                                )
                                            ),
                                            label = "loop.0"
                                        )
                                    ),
                                    S(
                                        ForStatement(
                                            init =
                                            InitDeclaration(
                                                VariableDeclaration(name = "i.1", init = IntExpression(0))
                                            ),
                                            condition =
                                            BinaryExpression(
                                                left = VariableExpression("i.1"),
                                                operator = Token(TokenType.LESS, "<", 5, 18),
                                                right = IntExpression(10)
                                            ),
                                            post =
                                            AssignmentExpression(
                                                lvalue = VariableExpression("i.1"),
                                                rvalue =
                                                BinaryExpression(
                                                    left = VariableExpression("i.1"),
                                                    operator = Token(TokenType.PLUS, "+", 5, 30),
                                                    right = IntExpression(1)
                                                )
                                            ),
                                            body =
                                            ExpressionStatement(
                                                AssignmentExpression(
                                                    lvalue = VariableExpression("a.0"),
                                                    rvalue =
                                                    BinaryExpression(
                                                        left = VariableExpression("a.0"),
                                                        operator = Token(TokenType.PLUS, "+", 6, 10),
                                                        right = IntExpression(1)
                                                    )
                                                )
                                            ),
                                            label = "loop.1"
                                        )
                                    ),
                                    S(
                                        ForStatement(
                                            init =
                                            InitExpression(
                                                expression = null
                                            ),
                                            condition =
                                            BinaryExpression(
                                                left = VariableExpression("a.0"),
                                                operator = Token(TokenType.GREATER, ">", 7, 8),
                                                right = IntExpression(0)
                                            ),
                                            post = null,
                                            body =
                                            ExpressionStatement(
                                                AssignmentExpression(
                                                    lvalue = VariableExpression("a.0"),
                                                    rvalue =
                                                    BinaryExpression(
                                                        left = VariableExpression("a.0"),
                                                        operator = Token(TokenType.PLUS, "+", 8, 10),
                                                        right = IntExpression(1)
                                                    )
                                                )
                                            ),
                                            label = "loop.2"
                                        )
                                    ),
                                    S(
                                        DoWhileStatement(
                                            condition =
                                            BinaryExpression(
                                                left = VariableExpression("a.0"),
                                                operator = Token(TokenType.GREATER, ">", 11, 9),
                                                right = IntExpression(0)
                                            ),
                                            body =
                                            ExpressionStatement(
                                                AssignmentExpression(
                                                    lvalue = VariableExpression("a.0"),
                                                    rvalue =
                                                    BinaryExpression(
                                                        left = VariableExpression("a.0"),
                                                        operator = Token(TokenType.PLUS, "+", 10, 10),
                                                        right = IntExpression(1)
                                                    )
                                                )
                                            ),
                                            label = "loop.3"
                                        )
                                    ),
                                    S(ReturnStatement(expression = IntExpression(0)))
                                )
                            )
                        )
                    )
                ),
                expectedTacky =
                TackyProgram(
                    functions =
                    listOf(
                        TackyFunction(
                            name = "main",
                            args = emptyList(),
                            body =
                            listOf(
                                // a.0 = 10
                                TackyCopy(TackyConstant(10), TackyVar("a.0")),
                                // while (a.0 > 0) ... label loop.0
                                TackyLabel("continue_loop.0"),
                                TackyBinary(TackyBinaryOP.GREATER, TackyVar("a.0"), TackyConstant(0), TackyVar("tmp.0")),
                                JumpIfZero(TackyVar("tmp.0"), TackyLabel("break_loop.0")),
                                // a.0 = a.0 - 1
                                TackyBinary(TackyBinaryOP.SUBTRACT, TackyVar("a.0"), TackyConstant(1), TackyVar("tmp.1")),
                                TackyCopy(TackyVar("tmp.1"), TackyVar("a.0")),
                                TackyJump(TackyLabel("continue_loop.0")),
                                TackyLabel("break_loop.0"),
                                // for (int i.1 = 0; i.1 < 10; i.1 = i.1 + 1) ... label loop.1
                                TackyCopy(TackyConstant(0), TackyVar("i.1")),
                                TackyLabel("start_loop.1"),
                                TackyBinary(TackyBinaryOP.LESS, TackyVar("i.1"), TackyConstant(10), TackyVar("tmp.2")),
                                JumpIfZero(TackyVar("tmp.2"), TackyLabel("break_loop.1")),
                                // body: a.0 = a.0 + 1
                                TackyBinary(TackyBinaryOP.ADD, TackyVar("a.0"), TackyConstant(1), TackyVar("tmp.3")),
                                TackyCopy(TackyVar("tmp.3"), TackyVar("a.0")),
                                TackyLabel("continue_loop.1"),
                                // post: i.1 = i.1 + 1
                                TackyBinary(TackyBinaryOP.ADD, TackyVar("i.1"), TackyConstant(1), TackyVar("tmp.4")),
                                TackyCopy(TackyVar("tmp.4"), TackyVar("i.1")),
                                TackyJump(TackyLabel("start_loop.1")),
                                TackyLabel("break_loop.1"),
                                // for (; a.0 > 0;) ... label loop.2
                                TackyLabel("start_loop.2"),
                                TackyBinary(TackyBinaryOP.GREATER, TackyVar("a.0"), TackyConstant(0), TackyVar("tmp.5")),
                                JumpIfZero(TackyVar("tmp.5"), TackyLabel("break_loop.2")),
                                // body: a.0 = a.0 + 1
                                TackyBinary(TackyBinaryOP.ADD, TackyVar("a.0"), TackyConstant(1), TackyVar("tmp.6")),
                                TackyCopy(TackyVar("tmp.6"), TackyVar("a.0")),
                                TackyLabel("continue_loop.2"),
                                TackyJump(TackyLabel("start_loop.2")),
                                TackyLabel("break_loop.2"),
                                // do { a.0 = a.0 + 1; } while (a.0 > 0); label loop.3
                                TackyLabel("start_loop.3"),
                                TackyBinary(TackyBinaryOP.ADD, TackyVar("a.0"), TackyConstant(1), TackyVar("tmp.7")),
                                TackyCopy(TackyVar("tmp.7"), TackyVar("a.0")),
                                TackyLabel("continue_loop.3"),
                                TackyBinary(TackyBinaryOP.GREATER, TackyVar("a.0"), TackyConstant(0), TackyVar("tmp.8")),
                                JumpIfNotZero(TackyVar("tmp.8"), TackyLabel("start_loop.3")),
                                TackyLabel("break_loop.3"),
                                // return 0; (explicit) and implicit final return 0
                                TackyRet(TackyConstant(0))
                            )
                        )
                    )
                ),
                expectedAssembly = null
            )
        )
}
