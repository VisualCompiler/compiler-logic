package integration

import assembly.InstructionFixer
import assembly.PseudoEliminator
import compiler.parser.VariableResolution
import lexer.Lexer
import parser.Parser
import parser.SimpleProgram
import tacky.TackyGenVisitor
import tacky.TackyProgram
import tacky.TackyToAsm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

enum class CompilerStage {
    LEXER,
    PARSER,
    TACKY,
    ASSEMBLY
}

class CompilerTestSuite {
    private val parser = Parser()
    private val tackyGenVisitor = TackyGenVisitor()
    private val variableResolution = VariableResolution()
    private val tackyToAsmConverter = TackyToAsm()
    private val instructionFixer = InstructionFixer()
    private val pseudoEliminator = PseudoEliminator()

    @Test
    fun testValidPrograms() {
        ValidTestCases.testCases.forEach { testCase ->
            // Lexer stage
            val lexer = Lexer(testCase.code)
            val tokens = lexer.tokenize()
            if (testCase.expectedTokenList != null) {
                assertEquals(
                    expected = testCase.expectedTokenList,
                    actual = tokens,
                    message =
                    """
                        |Expected:${testCase.expectedTokenList}
                        |                  Actual:$tokens
                    """.trimMargin()
                )
            }

            // Parser stage
            val ast = parser.parseTokens(tokens)
            assertIs<SimpleProgram>(ast)
            val transformedAst = variableResolution.visit(ast)
            if (testCase.expectedAst != null) {
                assertEquals(
                    expected = testCase.expectedAst,
                    actual = transformedAst,
                    message =
                    """
                        |Expected:${testCase.expectedAst}
                        |                  Actual:$transformedAst
                    """.trimMargin()
                )
            }

            // Tacky generation stage
            assertIs<SimpleProgram>(transformedAst)
            val tacky = transformedAst.accept(tackyGenVisitor)
            assertIs<TackyProgram>(tacky)
            if (testCase.expectedTacky != null) {
                assertEquals(
                    expected = testCase.expectedTacky,
                    actual = tacky,
                    message =
                    """
                        |Expected:${testCase.expectedTacky}
                        |                  Actual:$tacky
                    """.trimMargin()
                )
            }

            // Assembly generation stage
            val asmWithPseudos = tackyToAsmConverter.convert(tacky)
            val (asmWithStack, stackSpaceNeeded) = pseudoEliminator.eliminate(asmWithPseudos)
            val finalAsmProgram = instructionFixer.fix(asmWithStack, stackSpaceNeeded)
            if (testCase.expectedAssembly != null) {
                assertEquals(
                    expected = testCase.expectedAssembly,
                    actual = finalAsmProgram,
                    message =
                    """
                        |Expected:${testCase.expectedAssembly}
                        |                  Actual:$finalAsmProgram
                    """.trimMargin()
                )
            }
        }
    }

    @Test
    fun testInvalidPrograms() {
        for (testCase in InvalidTestCases.testCases) {
            // Lexer stage
            val lexer = Lexer(testCase.code)
            if (testCase.failingStage == CompilerStage.LEXER) {
                assertFailsWith(testCase.expectedException) {
                    lexer.tokenize()
                }
                continue
            }
            val tokens = lexer.tokenize()

            // Parser stage
            if (testCase.failingStage == CompilerStage.PARSER) {
                assertFailsWith(testCase.expectedException) {
                    Parser().parseTokens(tokens)
                }
                continue
            }
            val ast = Parser().parseTokens(tokens)

            // Tacky generation stage
            if (testCase.failingStage == CompilerStage.TACKY) {
                assertFailsWith(testCase.expectedException) {
                    ast.accept(TackyGenVisitor())
                }
                continue
            }
            val tackyProgram = ast.accept(TackyGenVisitor()) as? TackyProgram

            // Assembly stage
            if (testCase.failingStage == CompilerStage.ASSEMBLY) {
                assertFailsWith(testCase.expectedException) {
                    val asmWithPseudos = TackyToAsm().convert(tackyProgram!!)
                    val (asmWithStack, stackSpaceNeeded) = PseudoEliminator().eliminate(asmWithPseudos)
                    InstructionFixer().fix(asmWithStack, stackSpaceNeeded)
                }
                continue
            }
        }
    }
}
