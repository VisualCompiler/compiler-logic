package integration

import assembly.AllocateStack
import assembly.AsmBinary
import assembly.AsmBinaryOp
import assembly.AsmFunction
import assembly.AsmProgram
import assembly.AsmUnary
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
    val code: String,
    val expectedTokenList: List<lexer.Token>? = null,
    val expectedAst: parser.ASTNode? = null,
    val expectedTacky: TackyProgram? = null,
    val expectedAssembly: AsmProgram? = null
)

object ValidTestCases {
    val testCases: List<ValidTestCase> =
        listOf(
            // Basic arithmetic operations
            ValidTestCase(
                code = "int main(void) { return 5 + 3; }",
                expectedTacky =
                TackyProgram(
                    TackyFunction(
                        name = "main",
                        body =
                        listOf(
                            TackyBinary(
                                TackyBinaryOP.ADD,
                                TackyConstant(5),
                                TackyConstant(3),
                                TackyVar("tmp.0")
                            ),
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
                            AllocateStack(4),
                            Mov(Imm(5), Stack(-4)),
                            AsmBinary(AsmBinaryOp.ADD, Imm(3), Stack(-4)),
                            Mov(Stack(-4), Register(HardwareRegister.EAX))
                        )
                    )
                )
            ),
            ValidTestCase(
                code = "int main(void) \n      { return 10 - 4; }",
                expectedTacky =
                TackyProgram(
                    TackyFunction(
                        name = "main",
                        body =
                        listOf(
                            TackyBinary(
                                TackyBinaryOP.SUBTRACT,
                                TackyConstant(10),
                                TackyConstant(4),
                                TackyVar("tmp.0")
                            ),
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
                            AllocateStack(4),
                            Mov(Imm(10), Stack(-4)),
                            AsmBinary(AsmBinaryOp.SUB, Imm(4), Stack(-4)),
                            Mov(Stack(-4), Register(HardwareRegister.EAX))
                        )
                    )
                )
            ),
            ValidTestCase(
                code = "int main(void) { return 6 * 7; }",
                expectedTacky =
                TackyProgram(
                    TackyFunction(
                        name = "main",
                        body =
                        listOf(
                            TackyBinary(
                                TackyBinaryOP.MULTIPLY,
                                TackyConstant(6),
                                TackyConstant(7),
                                TackyVar("tmp.0")
                            ),
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
                            AllocateStack(4),
                            Mov(Imm(6), Stack(-4)),
                            AsmBinary(AsmBinaryOp.MUL, Imm(7), Stack(-4)),
                            Mov(Stack(-4), Register(HardwareRegister.EAX))
                        )
                    )
                )
            ),
            ValidTestCase(
                code = "int main(void) { return 20 / 4; }",
                expectedTacky =
                TackyProgram(
                    TackyFunction(
                        name = "main",
                        body =
                        listOf(
                            TackyBinary(
                                TackyBinaryOP.DIVIDE,
                                TackyConstant(20),
                                TackyConstant(4),
                                TackyVar("tmp.0")
                            ),
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
                            AllocateStack(4),
                            Mov(Imm(20), Register(HardwareRegister.EAX)),
                            Cdq,
                            Idiv(Imm(4)),
                            Mov(Register(HardwareRegister.EAX), Stack(-4)),
                            Mov(Stack(-4), Register(HardwareRegister.EAX))
                        )
                    )
                )
            ),
            ValidTestCase(
                code = "int main(void) { return 17 % 5; }",
                expectedTacky =
                TackyProgram(
                    TackyFunction(
                        name = "main",
                        body =
                        listOf(
                            TackyBinary(
                                TackyBinaryOP.REMAINDER,
                                TackyConstant(17),
                                TackyConstant(5),
                                TackyVar("tmp.0")
                            ),
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
                            AllocateStack(4),
                            Mov(Imm(17), Register(HardwareRegister.EAX)),
                            Cdq,
                            Idiv(Imm(5)),
                            Mov(Register(HardwareRegister.EDX), Stack(-4)),
                            Mov(Stack(-4), Register(HardwareRegister.EAX))
                        )
                    )
                )
            ),
            // Unary operations
            ValidTestCase(
                code = "int main(void) { return ~(-2); }",
                expectedTacky =
                TackyProgram(
                    TackyFunction(
                        name = "main",
                        body =
                        listOf(
                            TackyUnary(
                                TackyUnaryOP.NEGATE,
                                TackyConstant(2),
                                TackyVar("tmp.0")
                            ),
                            TackyUnary(
                                TackyUnaryOP.COMPLEMENT,
                                TackyVar("tmp.0"),
                                TackyVar("tmp.1")
                            ),
                            TackyRet(TackyVar("tmp.1"))
                        )
                    )
                ),
                expectedAssembly =
                AsmProgram(
                    AsmFunction(
                        name = "main",
                        body =
                        listOf(
                            AllocateStack(8),
                            Mov(Imm(2), Stack(-4)),
                            AsmUnary(assembly.AsmUnaryOp.NEG, Stack(-4)),
                            Mov(Stack(-4), Register(HardwareRegister.R10D)),
                            Mov(Register(HardwareRegister.R10D), Stack(-8)),
                            AsmUnary(assembly.AsmUnaryOp.NOT, Stack(-8)),
                            Mov(Stack(-8), Register(HardwareRegister.EAX))
                        )
                    )
                )
            ),
            ValidTestCase(
                code = "int main(void) { return -42; }",
                expectedTacky =
                TackyProgram(
                    TackyFunction(
                        name = "main",
                        body =
                        listOf(
                            TackyUnary(
                                TackyUnaryOP.NEGATE,
                                TackyConstant(42),
                                TackyVar("tmp.0")
                            ),
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
                            AllocateStack(4),
                            Mov(Imm(42), Stack(-4)),
                            AsmUnary(assembly.AsmUnaryOp.NEG, Stack(-4)),
                            Mov(Stack(-4), Register(HardwareRegister.EAX))
                        )
                    )
                )
            ),
            // Mixed
            ValidTestCase(
                code = "int main(void) { return (5 + 3) * 2; }",
                expectedTacky =
                TackyProgram(
                    TackyFunction(
                        name = "main",
                        body =
                        listOf(
                            TackyBinary(
                                TackyBinaryOP.ADD,
                                TackyConstant(5),
                                TackyConstant(3),
                                TackyVar("tmp.0")
                            ),
                            TackyBinary(
                                TackyBinaryOP.MULTIPLY,
                                TackyVar("tmp.0"),
                                TackyConstant(2),
                                TackyVar("tmp.1")
                            ),
                            TackyRet(TackyVar("tmp.1"))
                        )
                    )
                ),
                expectedAssembly =
                AsmProgram(
                    AsmFunction(
                        name = "main",
                        body =
                        listOf(
                            AllocateStack(8),
                            Mov(Imm(5), Stack(-4)),
                            AsmBinary(AsmBinaryOp.ADD, Imm(3), Stack(-4)),
                            Mov(Stack(-4), Register(HardwareRegister.R10D)),
                            Mov(Register(HardwareRegister.R10D), Stack(-8)),
                            AsmBinary(AsmBinaryOp.MUL, Imm(2), Stack(-8)),
                            Mov(Stack(-8), Register(HardwareRegister.EAX))
                        )
                    )
                )
            ),
            ValidTestCase(
                code = "int main(void) { return 5 + 3 * 2; }",
                expectedTacky =
                TackyProgram(
                    TackyFunction(
                        name = "main",
                        body =
                        listOf(
                            TackyBinary(
                                TackyBinaryOP.MULTIPLY,
                                TackyConstant(3),
                                TackyConstant(2),
                                TackyVar("tmp.0")
                            ),
                            TackyBinary(
                                TackyBinaryOP.ADD,
                                TackyConstant(5),
                                TackyVar("tmp.0"),
                                TackyVar("tmp.1")
                            ),
                            TackyRet(TackyVar("tmp.1"))
                        )
                    )
                ),
                expectedAssembly =
                AsmProgram(
                    AsmFunction(
                        name = "main",
                        body =
                        listOf(
                            AllocateStack(8),
                            Mov(Imm(3), Stack(-4)),
                            AsmBinary(AsmBinaryOp.MUL, Imm(2), Stack(-4)),
                            Mov(Imm(5), Stack(-8)),
                            Mov(src = Stack(offset = -4), dest = Register(name = HardwareRegister.R10D)),
                            AsmBinary(AsmBinaryOp.ADD, Register(HardwareRegister.R10D), Stack(-8)),
                            Mov(Stack(-8), Register(HardwareRegister.EAX))
                        )
                    )
                )
            ),
            ValidTestCase(
                code = "int main(void) { return -5 + 3; }",
                expectedTacky =
                TackyProgram(
                    TackyFunction(
                        name = "main",
                        body =
                        listOf(
                            TackyUnary(
                                TackyUnaryOP.NEGATE,
                                TackyConstant(5),
                                TackyVar("tmp.0")
                            ),
                            TackyBinary(
                                TackyBinaryOP.ADD,
                                TackyVar("tmp.0"),
                                TackyConstant(3),
                                TackyVar("tmp.1")
                            ),
                            TackyRet(TackyVar("tmp.1"))
                        )
                    )
                ),
                expectedAssembly =
                AsmProgram(
                    AsmFunction(
                        name = "main",
                        body =
                        listOf(
                            AllocateStack(8),
                            Mov(Imm(5), Stack(-4)),
                            AsmUnary(assembly.AsmUnaryOp.NEG, Stack(-4)),
                            Mov(Stack(-4), Register(HardwareRegister.R10D)),
                            Mov(Register(HardwareRegister.R10D), Stack(-8)),
                            AsmBinary(AsmBinaryOp.ADD, Imm(3), Stack(-8)),
                            Mov(Stack(-8), Register(HardwareRegister.EAX))
                        )
                    )
                )
            )
        )
}
