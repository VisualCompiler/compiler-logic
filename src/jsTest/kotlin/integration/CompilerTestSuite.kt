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
            if (testCase.expectedAst != null) {
                assertEquals(
                    expected = testCase.expectedAst,
                    actual = ast,
                    message =
                    """
                        |Test case $index failed with:
                        |Expected:${testCase.expectedAst}
                        |Actual:  $ast
                    """.trimMargin()
                )
            }

            // Tacky generation stage
            val tacky = CompilerWorkflow.take(ast)
            assertIs<TackyProgram>(tacky)
            if (testCase.expectedTacky != null) {
                assertEquals(
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
                assertEquals(
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
                assertFailsWith(InvalidTestCases.testCases[i].expectedException, "Test case $i failed with: ") {
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
