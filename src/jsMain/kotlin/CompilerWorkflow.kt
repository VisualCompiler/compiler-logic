package compiler

import assembly.AsmConstruct
import assembly.InstructionFixer
import assembly.PseudoEliminator
import compiler.symanticAnalysis.IdentifierResolution
import compiler.symanticAnalysis.LabelCollector
import compiler.symanticAnalysis.LoopLabeling
import compiler.symanticAnalysis.TypeChecker
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
        private val identifierResolution = IdentifierResolution()
        private val typeChecker = TypeChecker()
        private val labelAnalysis = LabelCollector.LabelAnalysis()
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
            val transformedAst = identifierResolution.analyze(ast) as SimpleProgram
            labelAnalysis.analyze(transformedAst)
            typeChecker.analyze(transformedAst)
            loopLabeling.visit(transformedAst)
            return transformedAst
        }

        fun take(ast: ASTNode): TackyConstruct {
            val tacky = ast.accept(tackyGenVisitor) as TackyConstruct
            return tacky
        }

        fun take(tacky: TackyConstruct): AsmConstruct {
            val asm = tackyToAsmConverter.convert(tacky as TackyProgram)
            val asmWithStackSizes = pseudoEliminator.eliminate(asm)
            val finalAsmProgram = instructionFixer.fix(asmWithStackSizes)
            return finalAsmProgram
        }
    }
}
