package integration

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import lexer.Lexer
import parser.Parser
import tacky.TackyBinary
import tacky.TackyBinaryOP
import tacky.TackyConstant
import tacky.TackyFunction
import tacky.TackyProgram
import tacky.TackyRet
import tacky.TackyVar
import kotlin.test.Test
import kotlin.test.assertEquals
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

    @Test
    fun `ast toJsonString produces valid JsonObject with expected keys`() {
        val code = "int main(void) { return 5 + 3; }"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize()
        val parser = Parser()
        val ast = parser.parseTokens(tokens)

        val json = ast.toJsonString()
        val parsed = Json.Default.parseToJsonElement(json).jsonObject

        // Basic structure checks
        assertEquals("SimpleProgram", parsed["type"]!!.jsonPrimitive.content)
        assertTrue(parsed.containsKey("children"))
        assertTrue(parsed.containsKey("label"))
    }

    @Test
    fun `tacky toJsonString produces valid JsonObject with expected keys`() {
        val tacky =
            TackyProgram(
                TackyFunction(
                    name = "main",
                    body =
                    listOf(
                        TackyBinary(TackyBinaryOP.ADD, TackyConstant(5), TackyConstant(3), TackyVar("tmp.0")),
                        TackyRet(TackyVar("tmp.0"))
                    )
                )
            )

        val json = tacky.toJsonString()
        val parsed = Json.Default.parseToJsonElement(json).jsonObject

        // Basic structure checks for program level
        assertEquals("TackyProgram", parsed["type"]!!.jsonPrimitive.content)
        assertTrue(parsed.containsKey("children"))
        assertTrue(parsed.containsKey("label"))
    }
}
