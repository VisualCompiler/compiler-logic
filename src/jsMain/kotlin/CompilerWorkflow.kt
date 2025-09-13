package compiler

import assembly.AsmConstruct
import assembly.InstructionFixer
import assembly.PseudoEliminator
import lexer.Lexer
import lexer.Token
import optimizations.ConstantFolding
import optimizations.ControlFlowGraph
import optimizations.DeadStoreElimination
import optimizations.OptimizationType
import parser.ASTNode
import parser.Parser
import parser.SimpleProgram
import semanticAnalysis.IdentifierResolution
import semanticAnalysis.LabelCollector
import semanticAnalysis.LoopLabeling
import semanticAnalysis.TypeChecker
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
        private val constantFolding = ConstantFolding()
        private val deadStoreElimination = DeadStoreElimination()

        fun fullCompile(code: String): Map<CompilerStage, Any> {
            val tokens = take(code)
            val ast = take(tokens)
            val tacky = take(ast)
            val asm = take(tacky as TackyProgram)

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
            val transformedAst = identifierResolution.analyze(ast)
            labelAnalysis.analyze(transformedAst)
            typeChecker.analyze(transformedAst)
            loopLabeling.visit(transformedAst)
            return transformedAst
        }

        fun take(ast: ASTNode): TackyConstruct {
            val tacky = ast.accept(tackyGenVisitor) as TackyConstruct
            return tacky
        }

        fun take(tacky: TackyProgram, optimizations: Set<OptimizationType>): TackyProgram {
            tacky.functions.forEach {
                var cfg = ControlFlowGraph().construct(it.name, it.body)
                for (optimization in optimizations) {
                    if (optimization == OptimizationType.CONSTANT_FOLDING) {
                        cfg = constantFolding.apply(cfg)
                    } else if (optimization == OptimizationType.DEAD_STORE_ELIMINATION) {
                        cfg = deadStoreElimination.apply(cfg)
                    }
                }
                it.body = cfg.toInstructions()
            }
            return tacky
        }

        fun take(tacky: TackyProgram): AsmConstruct {
            val asm = tackyToAsmConverter.convert(tacky)
            val asmWithStackSizes = pseudoEliminator.eliminate(asm)
            val finalAsmProgram = instructionFixer.fix(asmWithStackSizes)
            return finalAsmProgram
        }
    }
}
