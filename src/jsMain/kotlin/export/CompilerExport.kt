package export

import assembly.AsmProgram
import assembly.CodeEmitter
import assembly.InstructionFixer
import assembly.PseudoEliminator
import compiler.parser.LabelAnalysis
import compiler.parser.VariableResolution
import exceptions.CodeGenerationException
import exceptions.CompilationExceptions
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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

        val parser = Parser()
        val variableResolution = VariableResolution()
        val labelAnalysis = LabelAnalysis()
        val tackyGenVisitor = TackyGenVisitor()
        val tackyToAsmConverter = TackyToAsm()
        val pseudoEliminator = PseudoEliminator()
        val instructionFixer = InstructionFixer()
        val codeEmitter = CodeEmitter()

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
            } catch (e: CompilationExceptions) {
                val error =
                    CompilationError(
                        type = ErrorType.LEXICAL,
                        message = e.message ?: "Unknown lexical error",
                        line = e.line ?: -1,
                        column = e.column ?: -1
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
                    ast = parser.parseTokens(tokens)
                    labelAnalysis.analyze(ast)
                    ast = ast.accept(variableResolution)
                    ParserOutput(
                        errors = emptyArray(),
                        ast = ast.accept(ASTExport())
                    )
                } catch (e: CompilationExceptions) {
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
                    val result = ast.accept(tackyGenVisitor)
                    tackyProgram = result as? TackyProgram
                    TackyOutput(
                        tackyPretty = tackyProgram?.toPseudoCode(),
                        errors = emptyArray()
                    )
                } catch (e: CompilationExceptions) {
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
                    val asmWithPseudos = tackyToAsmConverter.convert(tackyProgram!!)
                    val (asmWithStack, stackSpaceNeeded) = pseudoEliminator.eliminate(asmWithPseudos)
                    val finalAsmProgram = instructionFixer.fix(asmWithStack, stackSpaceNeeded)
                    val finalAssemblyString = codeEmitter.emit(finalAsmProgram as AsmProgram)

                    CodeGeneratorOutput(
                        errors = emptyArray(),
                        assembly = finalAssemblyString
                    )

                    CodeGeneratorOutput(
                        errors = emptyArray(),
                        assembly = finalAssemblyString
                    )
                } catch (e: CodeGenerationException) {
                    val error =
                        CompilationError(
                            type = ErrorType.CODE_GENERATION,
                            message = e.message ?: "Unknown code generation error",
                            line = e.line ?: -1,
                            column = e.column ?: -1
                        )
                    overallErrors.add(error)
                    CodeGeneratorOutput(
                        errors = arrayOf(error)
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

    fun Lexer.toJsonString(): String {
        val jsonTokens =
            this.tokens.map { token ->
                JsonObject(
                    mapOf(
                        "line" to JsonPrimitive(token.line),
                        "column" to JsonPrimitive(token.column),
                        "type" to JsonPrimitive(token.type.toString()),
                        "lexeme" to JsonPrimitive(token.lexeme)
                    )
                )
            }

        return Json.encodeToString(JsonArray(jsonTokens))
    }
}
