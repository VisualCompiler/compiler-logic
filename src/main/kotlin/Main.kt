package org.example

import Lexer
import lexer.Lexer

fun main() {
    val code = """
    int main(void){
    int x = 5
    int y = 3
    int z = 5+3
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
}
