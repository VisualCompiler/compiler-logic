package lexer

import exceptions.LexicalException

sealed class TokenType {
    // keywords
    object KEYWORD_INT : TokenType()

    object KEYWORD_VOID : TokenType()

    object KEYWORD_RETURN : TokenType()

    object KEYWORD_DO : TokenType()

    object KEYWORD_WHILE : TokenType()

    object KEYWORD_FOR : TokenType()

    object KEYWORD_BREAK : TokenType()

    object KEYWORD_CONTINUE : TokenType()

    object IDENTIFIER : TokenType()

    object IF : TokenType()

    object ELSE : TokenType()

    object GOTO : TokenType()

    // literals
    object INT_LITERAL : TokenType()

    // Binary
    object PLUS : TokenType()

    object MULTIPLY : TokenType()

    object DIVIDE : TokenType()

    object REMAINDER : TokenType()

    object DECREMENT : TokenType()

    object NEGATION : TokenType() // Binary and Unary (-)

    // Logical
    object AND : TokenType()

    object OR : TokenType()

    // Relational
    object LESS : TokenType()

    object GREATER : TokenType()

    object LESS_EQUAL : TokenType()

    object GREATER_EQUAL : TokenType()

    // Unary
    object TILDE : TokenType()

    object NOT : TokenType()
    // Symbol

    object ASSIGN : TokenType()

    object EQUAL_TO : TokenType()

    object NOT_EQUAL : TokenType()

    object SEMICOLON : TokenType()

    object LEFT_PAREN : TokenType()

    object RIGHT_PAREN : TokenType()

    object LEFT_BRACK : TokenType()

    object RIGHT_BRACK : TokenType()

    object QUESTION_MARK : TokenType()

    object COLON : TokenType()

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
) {
    override fun equals(other: Any?): Boolean = other is Token && other.type == this.type && other.lexeme == this.lexeme
}

class Lexer(
    val source: String
) {
    val tokens: MutableList<Token> = mutableListOf()
    private var current = 0
    private var start = 0
    private var line = 1
    private var lineStart = 0

    // add later more keywords
    private val keywords =
        mapOf(
            "int" to TokenType.KEYWORD_INT,
            "void" to TokenType.KEYWORD_VOID,
            "return" to TokenType.KEYWORD_RETURN,
            "if" to TokenType.IF,
            "else" to TokenType.ELSE,
            "goto" to TokenType.GOTO,
            "while" to TokenType.KEYWORD_WHILE,
            "for" to TokenType.KEYWORD_FOR,
            "break" to TokenType.KEYWORD_BREAK,
            "continue" to TokenType.KEYWORD_CONTINUE,
            "do" to TokenType.KEYWORD_DO
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
            '?' -> addToken(TokenType.QUESTION_MARK)
            ':' -> addToken(TokenType.COLON)

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
            '&' -> {
                if (match('&')) {
                    addToken(TokenType.AND)
                }
            }
            '|' -> {
                if (match('|')) {
                    addToken(TokenType.OR)
                }
            }
            '=' -> {
                if (match('=')) {
                    addToken(TokenType.EQUAL_TO)
                } else {
                    addToken(TokenType.ASSIGN)
                }
            }
            '!' -> {
                if (match('=')) {
                    addToken(TokenType.NOT_EQUAL)
                } else {
                    addToken(TokenType.NOT)
                }
            }
            '<' -> {
                if (match('=')) {
                    addToken(TokenType.LESS_EQUAL)
                } else {
                    addToken(TokenType.LESS)
                }
            }
            '>' -> {
                if (match('=')) {
                    addToken(TokenType.GREATER_EQUAL)
                } else {
                    addToken(TokenType.GREATER)
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
}
