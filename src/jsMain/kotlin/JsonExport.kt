package org.example

import assembly.CodeGenerator
import lexer.Lexer
import org.example.parser.Parser

@OptIn(ExperimentalJsExport::class)
@JsExport
class JsonExport {
    fun exportCompilationResults(code: String): Array<String> {
        val lexer = Lexer(code)
        val tokens = lexer.tokenize()
        val parser = Parser()
        val ast = parser.parseTokens(tokens)
        val codeGenerator = CodeGenerator()
        val asm = codeGenerator.generateAsm(ast as org.example.parser.SimpleProgram)

        return arrayOf(
            lexer.toJsonString(),
            ast.toJsonString(),
            asm.toAsm(0)
        )
    }
}
