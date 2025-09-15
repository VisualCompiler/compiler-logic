package export

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import lexer.Token
import lexer.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompilerExportTest {

    @Test
    fun testTokenExportIncludesCompleteLocationInfo() {
        // Create a test token with complete location information
        val token = Token(
            type = TokenType.IDENTIFIER,
            lexeme = "testVar",
            startLine = 2,
            startColumn = 5,
            endLine = 2,
            endColumn = 12
        )

        // Export the token
        val jsonResult = listOf(token).toJsonString()

        // Parse the JSON result
        val jsonArray = Json.parseToJsonElement(jsonResult) as JsonArray
        val tokenJson = jsonArray[0] as JsonObject

        // Verify basic token information
        assertEquals("IDENTIFIER", tokenJson["type"]?.toString()?.removeSurrounding("\""))
        assertEquals("testVar", tokenJson["lexeme"]?.toString()?.removeSurrounding("\""))

        // Verify that complete location information is included
        assertTrue(tokenJson.containsKey("location"), "Token JSON should contain location information")

        val location = tokenJson["location"] as JsonObject
        assertEquals(2, location["startLine"]?.toString()?.toInt(), "Start line should be 2")
        assertEquals(5, location["startCol"]?.toString()?.toInt(), "Start column should be 5")
        assertEquals(2, location["endLine"]?.toString()?.toInt(), "End line should be 2")
        assertEquals(12, location["endCol"]?.toString()?.toInt(), "End column should be 12")

        println("Test passed! Token export includes complete location information:")
        println("Location: startLine=2, startCol=5, endLine=2, endCol=12")
    }

    @Test
    fun testTokenExportStructure() {
        // Create multiple tokens to test array structure
        val tokens = listOf(
            Token(TokenType.KEYWORD_INT, "int", 1, 1, 1, 3),
            Token(TokenType.IDENTIFIER, "main", 1, 5, 1, 8),
            Token(TokenType.LEFT_PAREN, "(", 1, 9, 1, 9)
        )

        // Export the tokens
        val jsonResult = tokens.toJsonString()

        // Parse the JSON result
        val jsonArray = Json.parseToJsonElement(jsonResult) as JsonArray

        // Verify array structure
        assertEquals(3, jsonArray.size, "Should have 3 tokens")

        // Verify each token has the expected structure
        for (i in 0 until jsonArray.size) {
            val tokenJson = jsonArray[i] as JsonObject
            assertTrue(tokenJson.containsKey("type"), "Token $i should have type")
            assertTrue(tokenJson.containsKey("lexeme"), "Token $i should have lexeme")
            assertTrue(tokenJson.containsKey("location"), "Token $i should have location")

            val location = tokenJson["location"] as JsonObject
            assertTrue(location.containsKey("startLine"), "Token $i location should have startLine")
            assertTrue(location.containsKey("startCol"), "Token $i location should have startCol")
            assertTrue(location.containsKey("endLine"), "Token $i location should have endLine")
            assertTrue(location.containsKey("endCol"), "Token $i location should have endCol")
        }
    }
}