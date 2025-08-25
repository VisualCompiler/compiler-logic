package integration

import assembly.InstructionFixer
import assembly.PseudoEliminator
import lexer.Lexer
import parser.Parser
import tacky.TackyGenVisitor
import tacky.TackyProgram
import tacky.TackyToAsm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

enum class CompilerStage {
    LEXER,
    PARSER,
    TACKY,
    ASSEMBLY
}

class CompilerTestSuite {
    @Test
    fun testValidPrograms() {
        ValidTestCases.testCases.forEach { testCase ->
            // Lexer stage
            val lexer = Lexer(testCase.code)
            val tokens = lexer.tokenize()
            if (testCase.expectedTokenList != null) {
                assertEquals(testCase.expectedTokenList, tokens)
            }

            // Parser stage
            val parser = Parser()
            val ast = parser.parseTokens(tokens)
            if (testCase.expectedAst != null) {
                assertEquals(testCase.expectedAst, ast)
            }

            // Tacky generation stage
            val tackyGenVisitor = TackyGenVisitor()
            val tackyResult = ast.accept(tackyGenVisitor)
            val tackyProgram = tackyResult as? TackyProgram
            if (testCase.expectedTacky != null) {
                assertEquals(testCase.expectedTacky, tackyProgram)
            }

            // Assembly generation stage
            val tackyToAsmConverter = TackyToAsm()
            val asmWithPseudos = tackyToAsmConverter.convert(tackyProgram!!)
            val pseudoEliminator = PseudoEliminator()
            val (asmWithStack, stackSpaceNeeded) = pseudoEliminator.eliminate(asmWithPseudos)
            val instructionFixer = InstructionFixer()
            val finalAsmProgram = instructionFixer.fix(asmWithStack, stackSpaceNeeded)
            if (testCase.expectedAssembly != null) {
                assertEquals(testCase.expectedAssembly, finalAsmProgram)
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
