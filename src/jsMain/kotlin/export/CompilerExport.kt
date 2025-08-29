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
import tacky.TackyProgram

@OptIn(ExperimentalJsExport::class)
@JsExport
class CompilerExport {
    fun exportCompilationResults(code: String): String {
        val outputs = mutableListOf<CompilationOutput>()
        val overallErrors = mutableListOf<CompilationError>()
        val codeEmitter = CodeEmitter()
        try {
            val tokens = CompilerWorkflow.take(code)
            val lexer = Lexer(code)
            outputs.add(
                LexerOutput(
                    tokens = lexer.toJsonString(),
                    errors = emptyArray()
                )
            )
            val ast = CompilerWorkflow.take(tokens)
            outputs.add(
                ParserOutput(
                    errors = emptyArray(),
                    ast = ast.accept(ASTExport())
                )
            )
            val tacky = CompilerWorkflow.take(ast)
            val tackyProgram = tacky as? TackyProgram
            outputs.add(
                TackyOutput(
                    tackyPretty = tackyProgram?.toPseudoCode(),
                    errors = emptyArray()
                )
            )
            val asm = CompilerWorkflow.take(tacky)
            val finalAssemblyString = codeEmitter.emit(asm as AsmProgram)
            outputs.add(
                AssemblyOutput(
                    errors = emptyArray(),
                    assembly = finalAssemblyString
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
                CompilerStage.LEXER -> outputs.add(LexerOutput(errors = arrayOf(error)))
                CompilerStage.PARSER -> outputs.add(ParserOutput(errors = arrayOf(error)))
                CompilerStage.TACKY -> outputs.add(TackyOutput(errors = arrayOf(error)))
                CompilerStage.ASSEMBLY -> outputs.add(AssemblyOutput(errors = arrayOf(error)))
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
                outputs.add(ParserOutput(errors = emptyArray()))
            }
            outputs.add(AssemblyOutput(errors = arrayOf(error)))
        }

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
