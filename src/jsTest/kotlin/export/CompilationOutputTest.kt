package export

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompilationOutputTest {

    @Test
    fun testSourceLocationInfoStructure() {
        val sourceLocation = SourceLocationInfo(
            startLine = 1,
            startColumn = 1,
            endLine = 5,
            endColumn = 10,
            totalLines = 5
        )

        // Test that all fields are accessible
        assertEquals(1, sourceLocation.startLine)
        assertEquals(1, sourceLocation.startColumn)
        assertEquals(5, sourceLocation.endLine)
        assertEquals(10, sourceLocation.endColumn)
        assertEquals(5, sourceLocation.totalLines)
    }

    @Test
    fun testLexerOutputIncludesSourceLocation() {
        val sourceLocation = SourceLocationInfo(1, 1, 3, 15, 3)
        val lexerOutput = LexerOutput(
            tokens = "[]",
            errors = emptyArray(),
            sourceLocation = sourceLocation
        )

        assertEquals("lexer", lexerOutput.stage)
        assertEquals("[]", lexerOutput.tokens)
        assertTrue(lexerOutput.errors.isEmpty())
        assertEquals(sourceLocation, lexerOutput.sourceLocation)
    }

    @Test
    fun testParserOutputIncludesSourceLocation() {
        val sourceLocation = SourceLocationInfo(1, 1, 3, 15, 3)
        val parserOutput = ParserOutput(
            ast = "{}",
            errors = emptyArray(),
            sourceLocation = sourceLocation
        )

        assertEquals("parser", parserOutput.stage)
        assertEquals("{}", parserOutput.ast)
        assertTrue(parserOutput.errors.isEmpty())
        assertEquals(sourceLocation, parserOutput.sourceLocation)
    }

    @Test
    fun testTackyOutputIncludesSourceLocation() {
        val sourceLocation = SourceLocationInfo(1, 1, 3, 15, 3)
        val tackyOutput = TackyOutput(
            tackyPretty = "tacky code",
            errors = emptyArray(),
            sourceLocation = sourceLocation
        )

        assertEquals("tacky", tackyOutput.stage)
        assertEquals("tacky code", tackyOutput.tackyPretty)
        assertTrue(tackyOutput.errors.isEmpty())
        assertEquals(sourceLocation, tackyOutput.sourceLocation)
    }

    @Test
    fun testAssemblyOutputIncludesSourceLocation() {
        val sourceLocation = SourceLocationInfo(1, 1, 3, 15, 3)
        val assemblyOutput = AssemblyOutput(
            assembly = "mov eax, 1",
            errors = emptyArray(),
            sourceLocation = sourceLocation
        )

        assertEquals("assembly", assemblyOutput.stage)
        assertEquals("mov eax, 1", assemblyOutput.assembly)
        assertTrue(assemblyOutput.errors.isEmpty())
        assertEquals(sourceLocation, assemblyOutput.sourceLocation)
    }

    @Test
    fun testSourceLocationInfoSerialization() {
        val sourceLocation = SourceLocationInfo(1, 1, 5, 20, 5)
        val lexerOutput = LexerOutput(
            tokens = "[]",
            errors = emptyArray(),
            sourceLocation = sourceLocation
        )

        // Test that the output can be serialized to JSON
        val result = CompilationResult(
            outputs = arrayOf(lexerOutput),
            overallSuccess = true,
            overallErrors = emptyArray()
        )

        val jsonString = result.toJsonString()
        assertTrue(jsonString.isNotEmpty(), "JSON string should not be empty")

        // Parse back to verify structure
        val json = Json.parseToJsonElement(jsonString) as JsonObject
        assertTrue(json.containsKey("outputs"), "JSON should contain outputs")
    }
}
