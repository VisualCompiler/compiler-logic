package org.example

import assembly.InstructionFixer
import assembly.PseudoEliminator
import exceptions.SyntaxError
import lexer.Lexer
import lexer.Token
import parser.ASTNode
import parser.Parser
import tacky.TackyGenVisitor
import tacky.TackyProgram
import tacky.TackyToAsm

@OptIn(ExperimentalJsExport::class)
@JsExport
class CompilerExport {
    fun exportCompilationResults(code: String): String {
        val outputs = mutableListOf<CompilationOutput>()
        val overallErrors = mutableListOf<CompilationError>()

        var tokens: List<Token>? = null
        var ast: ASTNode? = null
        var tackyProgram: TackyProgram? = null

        // Lexing
        val lexerOutput =
            try {
                val lexer = Lexer(code)
                tokens = lexer.tokenize()
                LexerOutput(
                    tokens = lexer.toJsonString(),
                    errors = emptyArray()
                )
            } catch (e: Exception) {
                val error =
                    CompilationError(
                        type = ErrorType.LEXICAL,
                        message = e.message ?: "Unknown lexical error",
                        line = -1,
                        column = -1
                    )
                overallErrors.add(error)
                LexerOutput(
                    errors = arrayOf(error)
                )
            }
        outputs.add(lexerOutput)

        // Parsing (only if lexer succeeded)
        val parserOutput =
            if (lexerOutput.errors.isEmpty() && tokens != null) {
                try {
                    val parser = Parser()
                    ast = parser.parseTokens(tokens)
                    ParserOutput(
                        errors = emptyArray(),
                        ast = ast.toJsonString()
                    )
                } catch (e: SyntaxError) {
                    val error =
                        CompilationError(
                            type = ErrorType.SYNTAX,
                            message = e.message ?: "Unknown syntax error",
                            line = e.line ?: -1,
                            column = e.column ?: -1
                        )
                    overallErrors.add(error)
                    ParserOutput(
                        errors = arrayOf(error)
                    )
                }
            } else {
                ParserOutput(
                    errors = emptyArray()
                )
            }
        outputs.add(parserOutput)
        val tackyOutput =
            if (parserOutput.errors.isEmpty() && ast != null) {
                try {
                    val tackyGenVisitor = TackyGenVisitor()
                    val result = ast!!.accept(tackyGenVisitor)
                    tackyProgram = result as? TackyProgram

                    println("TackyGenVisitor returned TackyProgram: $tackyProgram")
                    TackyOutput(
                        tacky = tackyProgram?.toJsonString(),
                        errors = emptyArray()
                    )
                } catch (e: SyntaxError) {
                    val error =
                        CompilationError(
                            type = ErrorType.CODE_GENERATION,
                            message = e.message ?: "Unknown tacky generation error",
                            line = e.line ?: -1,
                            column = e.column ?: -1
                        )
                    overallErrors.add(error)
                    TackyOutput(
                        errors = arrayOf(error)
                    )
                }
            } else {
                TackyOutput(
                    errors = emptyArray()
                )
            }
        outputs.add(tackyOutput)
        // Code generation
        val codeGeneratorOutput =
            if (tackyOutput.errors.isEmpty() && tackyProgram != null) {
                try {
                    val tackyToAsmConverter = TackyToAsm()
                    val asmWithPseudos = tackyToAsmConverter.convert(tackyProgram!!)

                    val pseudoEliminator = PseudoEliminator()
                    val (asmWithStack, stackSpaceNeeded) = pseudoEliminator.eliminate(asmWithPseudos)

                    val instructionFixer = InstructionFixer()
                    val finalAsmProgram = instructionFixer.fix(asmWithStack, stackSpaceNeeded)

                    val finalAssemblyString = finalAsmProgram.toAsm()
                    CodeGeneratorOutput(
                        errors = emptyArray(),
                        assembly = finalAssemblyString
                    )
                } catch (e: Exception) {
                    val error =
                        CompilationError(
                            type = ErrorType.CODE_GENERATION,
                            message = e.message ?: "Unknown code generation error",
                            line = -1,
                            column = -1
                        )
                    overallErrors.add(error)
                    CodeGeneratorOutput(
                        errors = arrayOf(error)
                    )
                }
            } else {
                CodeGeneratorOutput(
                    errors = emptyArray()
                )
            }
        outputs.add(codeGeneratorOutput)

        val result =
            CompilationResult(
                outputs = outputs.toTypedArray(),
                overallSuccess = overallErrors.isEmpty(),
                overallErrors = overallErrors.toTypedArray()
            )

        return result.toJsonString()
    }
}
