package integration

import assembly.AsmProgram
import compiler.CompilerStage
import compiler.CompilerWorkflow
import parser.SimpleProgram
import tacky.TackyProgram
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class CompilerTestSuite {

    private fun assertAstEquals(expected: SimpleProgram, actual: SimpleProgram, message: String) {
        // Compare function declarations
        assertEquals(expected.functionDeclaration.size, actual.functionDeclaration.size, "$message - Function count mismatch")

        expected.functionDeclaration.forEachIndexed { index, expectedFunc ->
            val actualFunc = actual.functionDeclaration[index]
            assertEquals(expectedFunc.name, actualFunc.name, "$message - Function name mismatch at index $index")
            assertEquals(expectedFunc.params, actualFunc.params, "$message - Function params mismatch at index $index")

            // Compare function bodies (ignoring SourceLocation)
            if (expectedFunc.body != null && actualFunc.body != null) {
                assertBlockEquals(expectedFunc.body, actualFunc.body, "$message - Function body mismatch at index $index")
            } else {
                assertEquals(expectedFunc.body, actualFunc.body, "$message - Function body null mismatch at index $index")
            }
        }
    }

    private fun assertBlockEquals(expected: parser.Block, actual: parser.Block, message: String) {
        assertEquals(expected.items.size, actual.items.size, "$message - Block item count mismatch")

        expected.items.forEachIndexed { index, expectedItem ->
            val actualItem = actual.items[index]
            when (expectedItem) {
                is parser.S -> {
                    assertIs<parser.S>(actualItem, "$message - Expected S but got ${actualItem::class.simpleName} at index $index")
                    assertStatementEquals(expectedItem.statement, actualItem.statement, "$message - Statement mismatch at index $index")
                }
                is parser.D -> {
                    assertIs<parser.D>(actualItem, "$message - Expected D but got ${actualItem::class.simpleName} at index $index")
                    assertDeclarationEquals(expectedItem.declaration, actualItem.declaration, "$message - Declaration mismatch at index $index")
                }
                else -> assertEquals(expectedItem, actualItem, "$message - Block item mismatch at index $index")
            }
        }
    }

    private fun assertStatementEquals(expected: parser.Statement, actual: parser.Statement, message: String) {
        when (expected) {
            is parser.ReturnStatement -> {
                assertIs<parser.ReturnStatement>(actual, "$message - Expected ReturnStatement but got ${actual::class.simpleName}")
                assertExpressionEquals(expected.expression, actual.expression, "$message - Return expression mismatch")
            }
            is parser.ExpressionStatement -> {
                assertIs<parser.ExpressionStatement>(actual, "$message - Expected ExpressionStatement but got ${actual::class.simpleName}")
                assertExpressionEquals(expected.expression, actual.expression, "$message - Expression mismatch")
            }
            is parser.NullStatement -> {
                assertIs<parser.NullStatement>(actual, "$message - Expected NullStatement but got ${actual::class.simpleName}")
            }
            is parser.WhileStatement -> {
                assertIs<parser.WhileStatement>(actual, "$message - Expected WhileStatement but got ${actual::class.simpleName}")
                assertExpressionEquals(expected.condition, actual.condition, "$message - While condition mismatch")
                assertStatementEquals(expected.body, actual.body, "$message - While body mismatch")
            }
            is parser.ForStatement -> {
                assertIs<parser.ForStatement>(actual, "$message - Expected ForStatement but got ${actual::class.simpleName}")
                assertForInitEquals(expected.init, actual.init, "$message - For init mismatch")
                if (expected.condition != null && actual.condition != null) {
                    assertExpressionEquals(expected.condition, actual.condition, "$message - For condition mismatch")
                } else {
                    assertEquals(expected.condition, actual.condition, "$message - For condition null mismatch")
                }
                if (expected.post != null && actual.post != null) {
                    assertExpressionEquals(expected.post, actual.post, "$message - For post mismatch")
                } else {
                    assertEquals(expected.post, actual.post, "$message - For post null mismatch")
                }
                assertStatementEquals(expected.body, actual.body, "$message - For body mismatch")
            }
            is parser.DoWhileStatement -> {
                assertIs<parser.DoWhileStatement>(actual, "$message - Expected DoWhileStatement but got ${actual::class.simpleName}")
                assertStatementEquals(expected.body, actual.body, "$message - DoWhile body mismatch")
                assertExpressionEquals(expected.condition, actual.condition, "$message - DoWhile condition mismatch")
            }
            is parser.IfStatement -> {
                assertIs<parser.IfStatement>(actual, "$message - Expected IfStatement but got ${actual::class.simpleName}")
                assertExpressionEquals(expected.condition, actual.condition, "$message - If condition mismatch")
                assertStatementEquals(expected.then, actual.then, "$message - If then mismatch")
                if (expected._else != null && actual._else != null) {
                    assertStatementEquals(expected._else, actual._else, "$message - If else mismatch")
                } else {
                    assertEquals(expected._else, actual._else, "$message - If else null mismatch")
                }
            }
            is parser.BreakStatement -> {
                assertIs<parser.BreakStatement>(actual, "$message - Expected BreakStatement but got ${actual::class.simpleName}")
                assertEquals(expected.label, actual.label, "$message - Break label mismatch")
            }
            is parser.ContinueStatement -> {
                assertIs<parser.ContinueStatement>(actual, "$message - Expected ContinueStatement but got ${actual::class.simpleName}")
                assertEquals(expected.label, actual.label, "$message - Continue label mismatch")
            }
            is parser.GotoStatement -> {
                assertIs<parser.GotoStatement>(actual, "$message - Expected GotoStatement but got ${actual::class.simpleName}")
                assertEquals(expected.label, actual.label, "$message - Goto label mismatch")
            }
            is parser.LabeledStatement -> {
                assertIs<parser.LabeledStatement>(actual, "$message - Expected LabeledStatement but got ${actual::class.simpleName}")
                assertEquals(expected.label, actual.label, "$message - LabeledStatement label mismatch")
                assertStatementEquals(expected.statement, actual.statement, "$message - LabeledStatement statement mismatch")
            }
            is parser.CompoundStatement -> {
                assertIs<parser.CompoundStatement>(actual, "$message - Expected CompoundStatement but got ${actual::class.simpleName}")
                assertBlockEquals(expected.block, actual.block, "$message - CompoundStatement block mismatch")
            }
            else -> assertEquals(expected, actual, "$message - Statement type mismatch")
        }
    }

    private fun assertExpressionEquals(expected: parser.Expression, actual: parser.Expression, message: String) {
        when (expected) {
            is parser.IntExpression -> {
                assertIs<parser.IntExpression>(actual, "$message - Expected IntExpression but got ${actual::class.simpleName}")
                assertEquals(expected.value, actual.value, "$message - Int value mismatch")
            }
            is parser.VariableExpression -> {
                assertIs<parser.VariableExpression>(actual, "$message - Expected VariableExpression but got ${actual::class.simpleName}")
                assertEquals(expected.name, actual.name, "$message - Variable name mismatch")
            }
            is parser.BinaryExpression -> {
                assertIs<parser.BinaryExpression>(actual, "$message - Expected BinaryExpression but got ${actual::class.simpleName}")
                assertExpressionEquals(expected.left, actual.left, "$message - Binary left mismatch")
                assertEquals(expected.operator, actual.operator, "$message - Binary operator mismatch")
                assertExpressionEquals(expected.right, actual.right, "$message - Binary right mismatch")
            }
            is parser.UnaryExpression -> {
                assertIs<parser.UnaryExpression>(actual, "$message - Expected UnaryExpression but got ${actual::class.simpleName}")
                assertEquals(expected.operator, actual.operator, "$message - Unary operator mismatch")
                assertExpressionEquals(expected.expression, actual.expression, "$message - Unary expression mismatch")
            }
            is parser.AssignmentExpression -> {
                assertIs<parser.AssignmentExpression>(actual, "$message - Expected AssignmentExpression but got ${actual::class.simpleName}")
                assertExpressionEquals(expected.lvalue, actual.lvalue, "$message - Assignment lvalue mismatch")
                assertExpressionEquals(expected.rvalue, actual.rvalue, "$message - Assignment rvalue mismatch")
            }
            is parser.FunctionCall -> {
                assertIs<parser.FunctionCall>(actual, "$message - Expected FunctionCall but got ${actual::class.simpleName}")
                assertEquals(expected.name, actual.name, "$message - Function call name mismatch")
                assertEquals(expected.arguments.size, actual.arguments.size, "$message - Function call argument count mismatch")
                expected.arguments.forEachIndexed { index, expectedArg ->
                    assertExpressionEquals(expectedArg, actual.arguments[index], "$message - Function call argument $index mismatch")
                }
            }
            is parser.ConditionalExpression -> {
                assertIs<parser.ConditionalExpression>(actual, "$message - Expected ConditionalExpression but got ${actual::class.simpleName}")
                assertExpressionEquals(expected.codition, actual.codition, "$message - Conditional condition mismatch")
                assertExpressionEquals(expected.thenExpression, actual.thenExpression, "$message - Conditional then mismatch")
                assertExpressionEquals(expected.elseExpression, actual.elseExpression, "$message - Conditional else mismatch")
            }
            else -> assertEquals(expected, actual, "$message - Expression type mismatch")
        }
    }

    private fun assertForInitEquals(expected: parser.ForInit, actual: parser.ForInit, message: String) {
        when (expected) {
            is parser.InitDeclaration -> {
                assertIs<parser.InitDeclaration>(actual, "$message - Expected InitDeclaration but got ${actual::class.simpleName}")
                assertDeclarationEquals(expected.varDeclaration, actual.varDeclaration, "$message - InitDeclaration variable mismatch")
            }
            is parser.InitExpression -> {
                assertIs<parser.InitExpression>(actual, "$message - Expected InitExpression but got ${actual::class.simpleName}")
                if (expected.expression != null && actual.expression != null) {
                    assertExpressionEquals(expected.expression, actual.expression, "$message - InitExpression mismatch")
                } else {
                    assertEquals(expected.expression, actual.expression, "$message - InitExpression null mismatch")
                }
            }
            else -> assertEquals(expected, actual, "$message - ForInit type mismatch")
        }
    }

    private fun assertTackyEquals(expected: TackyProgram, actual: TackyProgram, message: String) {
        assertEquals(expected.functions.size, actual.functions.size, "$message - Tacky function count mismatch")

        expected.functions.forEachIndexed { index, expectedFunc ->
            val actualFunc = actual.functions[index]
            assertEquals(expectedFunc.name, actualFunc.name, "$message - Tacky function name mismatch at index $index")
            assertEquals(expectedFunc.args, actualFunc.args, "$message - Tacky function args mismatch at index $index")
            assertEquals(expectedFunc.body.size, actualFunc.body.size, "$message - Tacky function body size mismatch at index $index")

            expectedFunc.body.forEachIndexed { instrIndex, expectedInstr ->
                val actualInstr = actualFunc.body[instrIndex]
                assertTackyInstructionEquals(expectedInstr, actualInstr, "$message - Tacky instruction mismatch at function $index, instruction $instrIndex")
            }
        }
    }

    private fun assertTackyInstructionEquals(expected: tacky.TackyInstruction, actual: tacky.TackyInstruction, message: String) {
        when (expected) {
            is tacky.TackyRet -> {
                assertIs<tacky.TackyRet>(actual, "$message - Expected TackyRet but got ${actual::class.simpleName}")
                assertTackyValueEquals(expected.value, actual.value, "$message - TackyRet value mismatch")
            }
            is tacky.TackyBinary -> {
                assertIs<tacky.TackyBinary>(actual, "$message - Expected TackyBinary but got ${actual::class.simpleName}")
                assertEquals(expected.operator, actual.operator, "$message - TackyBinary operator mismatch")
                assertTackyValueEquals(expected.src1, actual.src1, "$message - TackyBinary src1 mismatch")
                assertTackyValueEquals(expected.src2, actual.src2, "$message - TackyBinary src2 mismatch")
                assertTackyValueEquals(expected.dest, actual.dest, "$message - TackyBinary dest mismatch")
            }
            is tacky.TackyUnary -> {
                assertIs<tacky.TackyUnary>(actual, "$message - Expected TackyUnary but got ${actual::class.simpleName}")
                assertEquals(expected.operator, actual.operator, "$message - TackyUnary operator mismatch")
                assertTackyValueEquals(expected.src, actual.src, "$message - TackyUnary src mismatch")
                assertTackyValueEquals(expected.dest, actual.dest, "$message - TackyUnary dest mismatch")
            }
            is tacky.TackyCopy -> {
                assertIs<tacky.TackyCopy>(actual, "$message - Expected TackyCopy but got ${actual::class.simpleName}")
                assertTackyValueEquals(expected.src, actual.src, "$message - TackyCopy src mismatch")
                assertTackyValueEquals(expected.dest, actual.dest, "$message - TackyCopy dest mismatch")
            }
            is tacky.TackyFunCall -> {
                assertIs<tacky.TackyFunCall>(actual, "$message - Expected TackyFunCall but got ${actual::class.simpleName}")
                assertEquals(expected.funName, actual.funName, "$message - TackyFunCall name mismatch")
                assertEquals(expected.args.size, actual.args.size, "$message - TackyFunCall args size mismatch")
                expected.args.forEachIndexed { index, expectedArg ->
                    assertTackyValueEquals(expectedArg, actual.args[index], "$message - TackyFunCall arg $index mismatch")
                }
                assertTackyValueEquals(expected.dest, actual.dest, "$message - TackyFunCall dest mismatch")
            }
            is tacky.TackyJump -> {
                assertIs<tacky.TackyJump>(actual, "$message - Expected TackyJump but got ${actual::class.simpleName}")
                assertTackyLabelEquals(expected.target, actual.target, "$message - TackyJump target mismatch")
            }
            is tacky.JumpIfZero -> {
                assertIs<tacky.JumpIfZero>(actual, "$message - Expected JumpIfZero but got ${actual::class.simpleName}")
                assertTackyValueEquals(expected.condition, actual.condition, "$message - JumpIfZero condition mismatch")
                assertTackyLabelEquals(expected.target, actual.target, "$message - JumpIfZero target mismatch")
            }
            is tacky.JumpIfNotZero -> {
                assertIs<tacky.JumpIfNotZero>(actual, "$message - Expected JumpIfNotZero but got ${actual::class.simpleName}")
                assertTackyValueEquals(expected.condition, actual.condition, "$message - JumpIfNotZero condition mismatch")
                assertTackyLabelEquals(expected.target, actual.target, "$message - JumpIfNotZero target mismatch")
            }
            else -> assertEquals(expected, actual, "$message - Tacky instruction type mismatch")
        }
    }

    private fun assertTackyLabelEquals(expected: tacky.TackyLabel, actual: tacky.TackyLabel, message: String) {
        assertEquals(expected.name, actual.name, "$message - TackyLabel name mismatch")
    }

    private fun assertTackyValueEquals(expected: tacky.TackyVal, actual: tacky.TackyVal, message: String) {
        when (expected) {
            is tacky.TackyConstant -> {
                assertIs<tacky.TackyConstant>(actual, "$message - Expected TackyConstant but got ${actual::class.simpleName}")
                assertEquals(expected.value, actual.value, "$message - TackyConstant value mismatch")
            }
            is tacky.TackyVar -> {
                assertIs<tacky.TackyVar>(actual, "$message - Expected TackyVar but got ${actual::class.simpleName}")
                assertEquals(expected.name, actual.name, "$message - TackyVar name mismatch")
            }
            else -> assertEquals(expected, actual, "$message - Tacky value type mismatch")
        }
    }

    private fun assertAssemblyEquals(expected: AsmProgram, actual: AsmProgram, message: String) {
        assertEquals(expected.functions.size, actual.functions.size, "$message - Assembly function count mismatch")

        expected.functions.forEachIndexed { index, expectedFunc ->
            val actualFunc = actual.functions[index]
            assertEquals(expectedFunc.name, actualFunc.name, "$message - Assembly function name mismatch at index $index")
            assertEquals(expectedFunc.body.size, actualFunc.body.size, "$message - Assembly function body size mismatch at index $index")
            assertEquals(expectedFunc.stackSize, actualFunc.stackSize, "$message - Assembly function stack size mismatch at index $index")

            expectedFunc.body.forEachIndexed { instrIndex, expectedInstr ->
                val actualInstr = actualFunc.body[instrIndex]
                assertAssemblyInstructionEquals(expectedInstr, actualInstr, "$message - Assembly instruction mismatch at function $index, instruction $instrIndex")
            }
        }
    }

    private fun assertAssemblyInstructionEquals(expected: assembly.Instruction, actual: assembly.Instruction, message: String) {
        when (expected) {
            is assembly.Mov -> {
                assertIs<assembly.Mov>(actual, "$message - Expected Mov but got ${actual::class.simpleName}")
                assertAssemblyOperandEquals(expected.src, actual.src, "$message - Mov src mismatch")
                assertAssemblyOperandEquals(expected.dest, actual.dest, "$message - Mov dest mismatch")
            }
            is assembly.AsmUnary -> {
                assertIs<assembly.AsmUnary>(actual, "$message - Expected AsmUnary but got ${actual::class.simpleName}")
                assertEquals(expected.op, actual.op, "$message - AsmUnary operator mismatch")
                assertAssemblyOperandEquals(expected.dest, actual.dest, "$message - AsmUnary dest mismatch")
            }
            is assembly.AsmBinary -> {
                assertIs<assembly.AsmBinary>(actual, "$message - Expected AsmBinary but got ${actual::class.simpleName}")
                assertEquals(expected.op, actual.op, "$message - AsmBinary operator mismatch")
                assertAssemblyOperandEquals(expected.src, actual.src, "$message - AsmBinary src mismatch")
                assertAssemblyOperandEquals(expected.dest, actual.dest, "$message - AsmBinary dest mismatch")
            }
            is assembly.Idiv -> {
                assertIs<assembly.Idiv>(actual, "$message - Expected Idiv but got ${actual::class.simpleName}")
                assertAssemblyOperandEquals(expected.divisor, actual.divisor, "$message - Idiv divisor mismatch")
            }
            is assembly.Cdq -> {
                assertIs<assembly.Cdq>(actual, "$message - Expected Cdq but got ${actual::class.simpleName}")
            }
            is assembly.AllocateStack -> {
                assertIs<assembly.AllocateStack>(actual, "$message - Expected AllocateStack but got ${actual::class.simpleName}")
                assertEquals(expected.size, actual.size, "$message - AllocateStack size mismatch")
            }
            is assembly.DeAllocateStack -> {
                assertIs<assembly.DeAllocateStack>(actual, "$message - Expected DeAllocateStack but got ${actual::class.simpleName}")
                assertEquals(expected.size, actual.size, "$message - DeAllocateStack size mismatch")
            }
            is assembly.Push -> {
                assertIs<assembly.Push>(actual, "$message - Expected Push but got ${actual::class.simpleName}")
                assertAssemblyOperandEquals(expected.operand, actual.operand, "$message - Push operand mismatch")
            }
            is assembly.Call -> {
                assertIs<assembly.Call>(actual, "$message - Expected Call but got ${actual::class.simpleName}")
                assertEquals(expected.identifier, actual.identifier, "$message - Call identifier mismatch")
            }
            is assembly.Label -> {
                assertIs<assembly.Label>(actual, "$message - Expected Label but got ${actual::class.simpleName}")
                assertEquals(expected.name, actual.name, "$message - Label name mismatch")
            }
            is assembly.Jmp -> {
                assertIs<assembly.Jmp>(actual, "$message - Expected Jmp but got ${actual::class.simpleName}")
                assertAssemblyLabelEquals(expected.label, actual.label, "$message - Jmp label mismatch")
            }
            is assembly.JmpCC -> {
                assertIs<assembly.JmpCC>(actual, "$message - Expected JmpCC but got ${actual::class.simpleName}")
                assertEquals(expected.condition, actual.condition, "$message - JmpCC condition mismatch")
                assertAssemblyLabelEquals(expected.label, actual.label, "$message - JmpCC label mismatch")
            }
            is assembly.Cmp -> {
                assertIs<assembly.Cmp>(actual, "$message - Expected Cmp but got ${actual::class.simpleName}")
                assertAssemblyOperandEquals(expected.src, actual.src, "$message - Cmp src mismatch")
                assertAssemblyOperandEquals(expected.dest, actual.dest, "$message - Cmp dest mismatch")
            }
            is assembly.SetCC -> {
                assertIs<assembly.SetCC>(actual, "$message - Expected SetCC but got ${actual::class.simpleName}")
                assertEquals(expected.condition, actual.condition, "$message - SetCC condition mismatch")
                assertAssemblyOperandEquals(expected.dest, actual.dest, "$message - SetCC dest mismatch")
            }
            is assembly.Ret -> {
                assertIs<assembly.Ret>(actual, "$message - Expected Ret but got ${actual::class.simpleName}")
            }
            else -> assertEquals(expected, actual, "$message - Assembly instruction type mismatch")
        }
    }

    private fun assertAssemblyLabelEquals(expected: assembly.Label, actual: assembly.Label, message: String) {
        assertEquals(expected.name, actual.name, "$message - Assembly Label name mismatch")
    }

    private fun assertAssemblyOperandEquals(expected: assembly.Operand, actual: assembly.Operand, message: String) {
        when (expected) {
            is assembly.Imm -> {
                assertIs<assembly.Imm>(actual, "$message - Expected Imm but got ${actual::class.simpleName}")
                assertEquals(expected.value, actual.value, "$message - Imm value mismatch")
            }
            is assembly.Register -> {
                assertIs<assembly.Register>(actual, "$message - Expected Register but got ${actual::class.simpleName}")
                assertEquals(expected.name, actual.name, "$message - Register name mismatch")
            }
            is assembly.Stack -> {
                assertIs<assembly.Stack>(actual, "$message - Expected Stack but got ${actual::class.simpleName}")
                assertEquals(expected.offset, actual.offset, "$message - Stack offset mismatch")
            }
            is assembly.Pseudo -> {
                assertIs<assembly.Pseudo>(actual, "$message - Expected Pseudo but got ${actual::class.simpleName}")
                assertEquals(expected.name, actual.name, "$message - Pseudo name mismatch")
            }
            else -> assertEquals(expected, actual, "$message - Assembly operand type mismatch")
        }
    }

    private fun assertFunctionDeclarationEquals(expected: parser.FunctionDeclaration, actual: parser.FunctionDeclaration, message: String) {
        assertEquals(expected.name, actual.name, "$message - Function declaration name mismatch")
        assertEquals(expected.params, actual.params, "$message - Function declaration params mismatch")
        if (expected.body != null && actual.body != null) {
            assertBlockEquals(expected.body, actual.body, "$message - Function declaration body mismatch")
        } else {
            assertEquals(expected.body, actual.body, "$message - Function declaration body null mismatch")
        }
    }

    private fun assertDeclarationEquals(expected: parser.Declaration, actual: parser.Declaration, message: String) {
        when (expected) {
            is parser.VarDecl -> {
                assertIs<parser.VarDecl>(actual, "$message - Expected VarDecl but got ${actual::class.simpleName}")
                assertDeclarationEquals(expected.varDecl, actual.varDecl, "$message - VarDecl variable mismatch")
            }
            is parser.FunDecl -> {
                assertIs<parser.FunDecl>(actual, "$message - Expected FunDecl but got ${actual::class.simpleName}")
                assertFunctionDeclarationEquals(expected.funDecl, actual.funDecl, "$message - FunDecl function mismatch")
            }
            is parser.VariableDeclaration -> {
                assertIs<parser.VariableDeclaration>(actual, "$message - Expected VariableDeclaration but got ${actual::class.simpleName}")
                assertEquals(expected.name, actual.name, "$message - Variable declaration name mismatch")
                if (expected.init != null && actual.init != null) {
                    assertExpressionEquals(expected.init, actual.init, "$message - Variable declaration init mismatch")
                } else {
                    assertEquals(expected.init, actual.init, "$message - Variable declaration init null mismatch")
                }
            }
            is parser.FunctionDeclaration -> {
                assertIs<parser.FunctionDeclaration>(actual, "$message - Expected FunctionDeclaration but got ${actual::class.simpleName}")
                assertEquals(expected.name, actual.name, "$message - Function declaration name mismatch")
                assertEquals(expected.params, actual.params, "$message - Function declaration params mismatch")
                if (expected.body != null && actual.body != null) {
                    assertBlockEquals(expected.body, actual.body, "$message - Function declaration body mismatch")
                } else {
                    assertEquals(expected.body, actual.body, "$message - Function declaration body null mismatch")
                }
            }
            else -> assertEquals(expected, actual, "$message - Declaration type mismatch")
        }
    }

    @Test
    fun testValidPrograms() {
        ValidTestCases.testCases.forEachIndexed { index, testCase ->
            val tokens = CompilerWorkflow.take(testCase.code)
            if (testCase.expectedTokenList != null) {
                assertEquals(
                    expected = testCase.expectedTokenList,
                    actual = tokens,
                    message =
                    """
                        |Test case $index failed with:
                        |Expected:${testCase.expectedTokenList}
                        |Actual:  $tokens
                    """.trimMargin()
                )
            }

            // Parser stage
            val ast = CompilerWorkflow.take(tokens)
            assertIs<SimpleProgram>(ast)
            val simpleProgram = ast as SimpleProgram
            if (testCase.expectedAst != null) {
                assertIs<SimpleProgram>(testCase.expectedAst, "Expected AST should be SimpleProgram")
                assertAstEquals(
                    expected = testCase.expectedAst as SimpleProgram,
                    actual = simpleProgram,
                    message =
                    """
                        |Test case $index failed with:
                        |Expected:${testCase.expectedAst}
                        |Actual:  $simpleProgram
                    """.trimMargin()
                )
            }

            // Tacky generation stage
            val tacky = CompilerWorkflow.take(ast)
            assertIs<TackyProgram>(tacky)
            if (testCase.expectedTacky != null) {
                assertTackyEquals(
                    expected = testCase.expectedTacky,
                    actual = tacky,
                    message =
                    """
                        |Test case $index failed with: 
                        |Expected:${testCase.expectedTacky}
                        |Actual:  $tacky
                    """.trimMargin()
                )
            }

            // Assembly generation stage
            val asm = CompilerWorkflow.take(tacky)
            assertIs<AsmProgram>(asm)
            if (testCase.expectedAssembly != null) {
                assertAssemblyEquals(
                    expected = testCase.expectedAssembly,
                    actual = asm,
                    message =
                    """
                        |Test case $index failed with: 
                        |Expected:${testCase.expectedAssembly}
                        |Actual:  $asm
                    """.trimMargin()
                )
            }
        }
    }

    @Test
    fun testInvalidPrograms() {
        for (i in InvalidTestCases.testCases.indices) {
            if (InvalidTestCases.testCases[i].failingStage == CompilerStage.LEXER) {
                assertFailsWith(InvalidTestCases.testCases[i].expectedException, "Test case $i failed with: ") {
                    CompilerWorkflow.take(InvalidTestCases.testCases[i].code)
                }
                continue
            }
            val tokens = CompilerWorkflow.take(InvalidTestCases.testCases[i].code)

            if (InvalidTestCases.testCases[i].failingStage == CompilerStage.PARSER) {
                assertFailsWith(
                    InvalidTestCases.testCases[i].expectedException,
                    "Test case $i failed with: ${InvalidTestCases.testCases[i].code}"
                ) {
                    CompilerWorkflow.take(tokens)
                }
                continue
            }
            val ast = CompilerWorkflow.take(tokens) as SimpleProgram

            if (InvalidTestCases.testCases[i].failingStage == CompilerStage.TACKY) {
                assertFailsWith(InvalidTestCases.testCases[i].expectedException, "Test case $i failed with: ") {
                    CompilerWorkflow.take(ast)
                }
                continue
            }
            val tackyProgram = CompilerWorkflow.take(ast) as TackyProgram

            // Assembly stage
            if (InvalidTestCases.testCases[i].failingStage == CompilerStage.ASSEMBLY) {
                assertFailsWith(InvalidTestCases.testCases[i].expectedException, message = "Test case $i failed with: ") {
                    CompilerWorkflow.take(tackyProgram)
                }
                continue
            }
        }
    }
}
