package integration

import org.example.CompilerExport
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CompilerExportTest {
    private val compilerExport = CompilerExport()

    @Test
    fun `test successful compilation`() {
        val code =
            """
            int main(void) {
                return 5;
            }
            """.trimIndent()

        val result = compilerExport.exportCompilationResults(code)
        assertNotNull(result)
        assertTrue(result.isNotEmpty())

        // Verify it's valid JSON
        assertTrue(result.contains("outputs"))
        assertTrue(result.contains("overallSuccess"))
        assertTrue(result.contains("overallErrors"))
    }

    @Test
    fun `test compilation with syntax error`() {
        val code =
            """
            int main(void) {
                return 5
            }
            """.trimIndent()
        // Missing semicolon

        val result = compilerExport.exportCompilationResults(code)
        assertNotNull(result)
        assertTrue(result.isNotEmpty())

        // Should contain error information
        assertTrue(result.contains("overallErrors"))
    }

    @Test
    fun `test empty code`() {
        val result = compilerExport.exportCompilationResults("")
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }
}
