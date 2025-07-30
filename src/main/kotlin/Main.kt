package org.example

import lexer.Lexer
import org.example.parser.Parser

fun main() {
    val code =
        """
        int main(void){
        return 5;
        }
        """.trimIndent()
    val lexer = Lexer(code)
    val tokens = lexer.tokenize()
    tokens.forEach { token ->
        println(
            "Type: ${token.type.toString().padEnd(18)} " +
                "Lexeme: '${token.lexeme.padEnd(6)}' " +
                "Position: [L:${token.line}, C:${token.column}]"
        )
    }

    val parser = Parser()
    val ast = parser.parseTokens(tokens)
    println(ast.prettyPrint())
}
