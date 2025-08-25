package lexer

import exceptions.LexicalException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

sealed class TokenType {
    // keywords
    object KEYWORD_INT : TokenType()

    object KEYWORD_VOID : TokenType()

    object KEYWORD_RETURN : TokenType()

    object IDENTIFIER : TokenType()

    // literals
    object INT_LITERAL : TokenType()

    // Symbols
    object PLUS : TokenType()

    object MULTIPLY : TokenType()

    object DIVIDE : TokenType()

    object REMAINDER : TokenType()

    object TILDE : TokenType()

    object NEGATION : TokenType()

    object DECREMENT : TokenType()

    object ASSIGN : TokenType()

    object SEMICOLON : TokenType()

    object LEFT_PAREN : TokenType()

    object RIGHT_PAREN : TokenType()

    object LEFT_BRACK : TokenType()

    object RIGHT_BRACK : TokenType()

    // Special token for End of File
    object EOF : TokenType()

    override fun toString(): String {
        return this::class.simpleName ?: "" // Returns the name of the current token type object
    }
}

data class Token(
    val type: TokenType,
    val lexeme: String,
    val line: Int,
    val column: Int
)

class Lexer(
    val source: String
) {
    private val tokens = mutableListOf<Token>()
    private var current = 0
    private var start = 0
    private var line = 1
    private var lineStart = 0

    // add later more keywords
    private val keywords =
        mapOf(
            "int" to TokenType.KEYWORD_INT,
            "void" to TokenType.KEYWORD_VOID,
            "return" to TokenType.KEYWORD_RETURN
        )

    fun tokenize(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(TokenType.EOF, "", line, current - lineStart + 1))
        return tokens
    }

    // check if we are at end of the source code
    private fun isAtEnd(): Boolean =
        if (current >= source.length) {
            true
        } else {
            false
        }

    //
    private fun advance(): Char {
        return source[current++] // gets current char, then increments current
    }

    // use pattern matching for assigning values to token
    private fun scanToken() {
        when (val char = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACK)
            '}' -> addToken(TokenType.RIGHT_BRACK)
            ';' -> addToken(TokenType.SEMICOLON)
            '+' -> addToken(TokenType.PLUS)
            '%' -> addToken(TokenType.REMAINDER)
            '*' -> addToken(TokenType.MULTIPLY)
            '/' -> addToken(TokenType.DIVIDE)
            '=' -> addToken(TokenType.ASSIGN)

            '~' -> addToken(TokenType.TILDE)
            '-' -> {
                // If a second '-' follows the first one
                if (match('-')) {
                    // it's a decrement token.
                    addToken(TokenType.DECREMENT)
                } else {
                    // it's just a negation/minus token.
                    addToken(TokenType.NEGATION)
                }
            }

            ' ' -> {}
            '\n' -> {
                line++
                lineStart = current
            }

            else -> {
                if (isDigit(char)) {
                    number()
                } else if (isAlphabetic(char)) {
                    identifier()
                } else {
                    throw LexicalException(char, line, current - lineStart)
                }
            }
        }
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.subSequence(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun number() {
        while (isDigit(peek())) advance()
        addToken(TokenType.INT_LITERAL)
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++ // Consume the character if it matches
        return true
    }

    private fun peek(): Char = if (isAtEnd()) '\u0000' else source[current]

    private fun isDigit(c: Char): Boolean = c in '0'..'9'

    private fun isAlphabetic(c: Char): Boolean = (c in 'a'..'z' || (c in 'A'..'Z') || c == '_')

    private fun isAlphaNumeric(c: Char): Boolean = isDigit(c) || isAlphabetic(c)

    private fun addToken(type: TokenType) {
        val text = source.substring(start, current)
        val column = start - lineStart + 1
        tokens.add(Token(type, text, line, column))
    }

    fun toJsonString(): String {
        val jsonTokens =
            tokens.map { token ->
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
