package org.example

import assembly.CodeGenerator
import exceptions.SyntaxError
import lexer.Lexer
import lexer.Token
import org.example.parser.ASTNode
import org.example.parser.Parser
import org.example.parser.SimpleProgram

@OptIn(ExperimentalJsExport::class)
@JsExport
class CompilerExport {
    fun exportCompilationResults(code: String): String {
        val outputs = mutableListOf<CompilationOutput>()
        val overallErrors = mutableListOf<CompilationError>()

        var tokens: List<Token>? = null
        var ast: ASTNode? = null

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

        // Code generation (only if parser succeeded)
        val codeGeneratorOutput =
            if (parserOutput.errors.isEmpty() && ast != null) {
                try {
                    val codeGenerator = CodeGenerator()
                    val asm = codeGenerator.generateAsm(ast as SimpleProgram)
                    CodeGeneratorOutput(
                        errors = emptyArray(),
                        assembly = asm.toAsm()
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
