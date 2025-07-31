import lexer.Lexer
import lexer.TokenType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LexerTest {

    @Test
    fun `test keywords and identifier`() {
        val source = "int main"
        val lexer = Lexer(source)
        val tokens = lexer.tokenize()

        assertEquals(TokenType.KEYWORD_INT, tokens[0].type)
        assertEquals("int", tokens[0].lexeme)
        assertEquals(TokenType.IDENTIFIER, tokens[1].type)
        assertEquals("main", tokens[1].lexeme)
    }

    @Test
    fun `test integer values`() {
        val source = "563"
        val lexer = Lexer(source)
        val tokens = lexer.tokenize()

        assertEquals(TokenType.INT_LITERAL, tokens[0].type)
        assertEquals("563", tokens[0].lexeme)
    }

    @Test
    fun `test arithmetic expression`() {
        val source = "x = a + b - 432;"
        val lexer = Lexer(source)
        val tokens = lexer.tokenize()
        val types = tokens.map { it.type }
        val expected_tokens =
            listOf(
                TokenType.IDENTIFIER,
                TokenType.ASSIGN,
                TokenType.IDENTIFIER,
                TokenType.PLUS,
                TokenType.IDENTIFIER,
                TokenType.MINUS,
                TokenType.INT_LITERAL,
                TokenType.SEMICOLON,
                TokenType.EOF
            )

        assertEquals(expected_tokens, types)
    }

    @Test
    fun `test parantheses and brackets and invalid`() {
        val source = "if (a){ return 0;}"
        val lexer = Lexer(source)
        val tokens = lexer.tokenize()

        val expected_tokens =
            listOf(
                TokenType.IDENTIFIER, // remove it when we add if as a keyword
                TokenType.LEFT_PAREN,
                TokenType.IDENTIFIER,
                TokenType.RIGHT_PAREN,
                TokenType.LEFT_BRACK,
                TokenType.KEYWORD_RETURN,
                TokenType.INT_LITERAL,
                TokenType.SEMICOLON,
                TokenType.RIGHT_BRACK,
                TokenType.EOF
            )

        val types = tokens.map { it.type }

        assertEquals(expected_tokens, types)
    }
}
