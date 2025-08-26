package integration

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import lexer.Lexer
import kotlin.test.Test
import kotlin.test.assertTrue

class ToJsonStringTest {
    @Test
    fun `lexer toJsonString produces an array of tokens with correct fields`() {
        val code = "int main(void) { return 123; }"
        val lexer = Lexer(code)
        lexer.tokenize()
        val json = lexer.toJsonString()
        val parsed = Json.Default.parseToJsonElement(json).jsonArray

        assertTrue(parsed.isNotEmpty())
        val token = parsed.first().jsonObject
        assertTrue(token.containsKey("type"))
        assertTrue(token.containsKey("lexeme"))
        assertTrue(token.containsKey("line"))
        assertTrue(token.containsKey("column"))
    }
}
