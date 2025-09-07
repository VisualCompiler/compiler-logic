package export

import assembly.AsmProgram
import assembly.CodeEmitter
import compiler.CompilerStage
import compiler.CompilerWorkflow
import exceptions.CompilationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import lexer.Lexer
import lexer.Token
import tacky.TackyProgram

@OptIn(ExperimentalJsExport::class)
@JsExport
class CompilerExport {

    private fun calculateSourceLocationInfo(code: String): SourceLocationInfo {
        val lines = code.split('\n')
        val totalLines = lines.size
        val lastLine = lines.lastOrNull() ?: ""
        val endColumn = lastLine.length + 1

        return SourceLocationInfo(
            startLine = 1,
            startColumn = 1,
            endLine = totalLines,
            endColumn = endColumn,
            totalLines = totalLines
        )
    }

    fun exportCompilationResults(code: String): String {
        val outputs = mutableListOf<CompilationOutput>()
        val overallErrors = mutableListOf<CompilationError>()
        val codeEmitter = CodeEmitter()
        val sourceLocationInfo = calculateSourceLocationInfo(code)

        try {
            val tokens = CompilerWorkflow.take(code)
            Lexer(code)
            outputs.add(
                LexerOutput(
                    tokens = exportTokens(tokens),
                    errors = emptyArray(),
                    sourceLocation = sourceLocationInfo
                )
            )
            val ast = CompilerWorkflow.take(tokens)
            outputs.add(
                ParserOutput(
                    errors = emptyArray(),
                    ast = ast.accept(ASTExport()),
                    sourceLocation = sourceLocationInfo
                )
            )
            val tacky = CompilerWorkflow.take(ast)
            val tackyProgram = tacky as? TackyProgram
            outputs.add(
                TackyOutput(
                    tackyPretty = tackyProgram?.toPseudoCode(),
                    errors = emptyArray(),
                    sourceLocation = sourceLocationInfo
                )
            )
            val asm = CompilerWorkflow.take(tacky)
            val finalAssemblyString = codeEmitter.emit(asm as AsmProgram)
            outputs.add(
                AssemblyOutput(
                    errors = emptyArray(),
                    assembly = finalAssemblyString,
                    sourceLocation = sourceLocationInfo
                )
            )
        } catch (e: CompilationException) {
            val stage =
                when {
                    outputs.isEmpty() -> CompilerStage.LEXER
                    outputs.size == 1 -> CompilerStage.PARSER
                    outputs.size == 2 -> CompilerStage.TACKY
                    else -> CompilerStage.ASSEMBLY
                }
            val error =
                CompilationError(
                    stage = stage.name.lowercase(),
                    message = e.message ?: "Unknown ${stage.name.lowercase()} error",
                    line = e.line ?: -1,
                    column = e.column ?: -1
                )
            overallErrors.add(error)
            when (stage) {
                CompilerStage.LEXER -> outputs.add(LexerOutput(errors = arrayOf(error), sourceLocation = sourceLocationInfo))
                CompilerStage.PARSER -> outputs.add(ParserOutput(errors = arrayOf(error), sourceLocation = sourceLocationInfo))
                CompilerStage.TACKY -> outputs.add(TackyOutput(errors = arrayOf(error), sourceLocation = sourceLocationInfo))
                CompilerStage.ASSEMBLY -> outputs.add(AssemblyOutput(errors = arrayOf(error), sourceLocation = sourceLocationInfo))
            }
        } catch (e: Exception) {
            // Fallback for any unexpected runtime errors
            val error =
                CompilationError(
                    message = e.message ?: "Unknown error",
                    line = -1,
                    column = -1
                )
            overallErrors.add(error)
            // ensure we return four stages
            while (outputs.size < 3) {
                outputs.add(ParserOutput(errors = emptyArray(), sourceLocation = sourceLocationInfo))
            }
            outputs.add(AssemblyOutput(errors = arrayOf(error), sourceLocation = sourceLocationInfo))
        }

        val result =
            CompilationResult(
                outputs = outputs.toTypedArray(),
                overallSuccess = overallErrors.isEmpty(),
                overallErrors = overallErrors.toTypedArray()
            )

        return result.toJsonString()
    }

    fun exportTokens(tokens: List<Token>): String = tokens.toJsonString()
}

fun List<Token>.toJsonString(): String {
    val jsonTokens =
        this.map { token ->
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(token.type.toString()),
                    "lexeme" to JsonPrimitive(token.lexeme),
                    "location" to JsonObject(
                        mapOf(
                            "startLine" to JsonPrimitive(token.startLine),
                            "startCol" to JsonPrimitive(token.startColumn),
                            "endLine" to JsonPrimitive(token.endLine),
                            "endCol" to JsonPrimitive(token.endColumn)
                        )
                    )
                )
            )
        }

    return Json.encodeToString(JsonArray(jsonTokens))
}
