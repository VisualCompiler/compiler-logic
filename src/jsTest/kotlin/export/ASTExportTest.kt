package export

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import parser.IntExpression
import parser.SourceLocation
import parser.VariableExpression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ASTExportTest {

    @Test
    fun testASTExportIncludesLocationAndId() {
        // Create a simple test AST node with location and ID
        val location = SourceLocation(1, 1, 1, 5)
        val intExpr = IntExpression(42, location)

        // Export the AST
        val export = ASTExport()
        val jsonResult = intExpr.accept(export)

        // Parse the JSON result
        val json = Json.parseToJsonElement(jsonResult) as JsonObject

        // Verify that location and ID are included
        assertTrue(json.containsKey("location"), "JSON should contain location information")
        assertTrue(json.containsKey("id"), "JSON should contain ID information")

        // Check location details
        val loc = json["location"] as JsonObject
        assertEquals(1, loc["startLine"]?.toString()?.toInt(), "Start line should be 1")
        assertEquals(1, loc["startCol"]?.toString()?.toInt(), "Start column should be 1")
        assertEquals(1, loc["endLine"]?.toString()?.toInt(), "End line should be 1")
        assertEquals(5, loc["endCol"]?.toString()?.toInt(), "End column should be 5")

        // Check that ID is not empty
        val id = json["id"]?.toString()?.removeSurrounding("\"")
        assertTrue(!id.isNullOrEmpty(), "ID should not be empty")

        println("Test passed! Location and ID are included in AST export:")
        println("Location: startLine=1, startCol=1, endLine=1, endCol=5")
        println("ID: $id")
    }

    @Test
    fun testASTExportForVariableExpression() {
        // Create a variable expression with location
        val location = SourceLocation(2, 5, 2, 8)
        val varExpr = VariableExpression("test", location)

        // Export the AST
        val export = ASTExport()
        val jsonResult = varExpr.accept(export)

        // Parse the JSON result
        val json = Json.parseToJsonElement(jsonResult) as JsonObject

        // Verify basic structure
        assertEquals("Expression", json["type"]?.toString()?.removeSurrounding("\""))
        assertEquals("Variable(test)", json["label"]?.toString()?.removeSurrounding("\""))

        // Verify location and ID are included
        assertTrue(json.containsKey("location"), "Variable expression should have location")
        assertTrue(json.containsKey("id"), "Variable expression should have ID")

        // Check location details
        val loc = json["location"] as JsonObject
        assertEquals(2, loc["startLine"]?.toString()?.toInt(), "Start line should be 2")
        assertEquals(5, loc["startCol"]?.toString()?.toInt(), "Start column should be 5")
        assertEquals(2, loc["endLine"]?.toString()?.toInt(), "End line should be 2")
        assertEquals(8, loc["endCol"]?.toString()?.toInt(), "End column should be 8")
    }
}
