package compiler

import assembly.AsmConstruct
import assembly.InstructionFixer
import assembly.PseudoEliminator
import compiler.parser.LabelAnalysis
import compiler.parser.LoopLabeling
import compiler.parser.VariableResolution
import lexer.Lexer
import lexer.Token
import parser.ASTNode
import parser.Parser
import parser.SimpleProgram
import tacky.TackyConstruct
import tacky.TackyGenVisitor
import tacky.TackyProgram
import tacky.TackyToAsm

enum class CompilerStage {
    LEXER,
    PARSER,
    TACKY,
    ASSEMBLY
}

sealed class CompilerWorkflow {
    companion object {
        private val parser = Parser()
        private val tackyGenVisitor = TackyGenVisitor()
        private val variableResolution = VariableResolution()
        private val labelAnalysis = LabelAnalysis()
        private val loopLabeling = LoopLabeling()
        private val tackyToAsmConverter = TackyToAsm()
        private val instructionFixer = InstructionFixer()
        private val pseudoEliminator = PseudoEliminator()

        fun fullCompile(code: String): Map<CompilerStage, Any> {
            val tokens = take(code)
            val ast = take(tokens)
            val tacky = take(ast)
            val asm = take(tacky)

            return mapOf(
                CompilerStage.LEXER to tokens,
                CompilerStage.PARSER to ast,
                CompilerStage.TACKY to tacky,
                CompilerStage.ASSEMBLY to asm
            )
        }

        fun take(code: String): List<Token> {
            val lexer = Lexer(code)
            return lexer.tokenize()
        }

        fun take(tokens: List<Token>): ASTNode {
            val ast = parser.parseTokens(tokens) as SimpleProgram
            val transformedAst = variableResolution.visit(ast) as SimpleProgram
            labelAnalysis.analyze(transformedAst)
            loopLabeling.visit(transformedAst)
            return transformedAst
        }

        fun take(ast: ASTNode): TackyConstruct {
            val tacky = ast.accept(tackyGenVisitor) as TackyConstruct
            return tacky
        }

        fun take(tacky: TackyConstruct): AsmConstruct {
            val asm = tackyToAsmConverter.convert(tacky as TackyProgram)
            val (asmWithStack, stackSpaceNeeded) = pseudoEliminator.eliminate(asm)
            val finalAsmProgram = instructionFixer.fix(asmWithStack, stackSpaceNeeded)
            return finalAsmProgram
        }
    }
}
