package parser

import exceptions.DuplicateVariableDeclaration
import exceptions.MissingDeclarationException
import semanticAnalysis.IdentifierResolution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// Helper constant for test locations
val VAR_TEST_LOCATION = SourceLocation(1, 1, 1, 1)

class VariableResolutionTest {
    private val identifierResolution = IdentifierResolution()

    @Test
    fun testVariableRenamingAndResolution() {
        val ast =
            SimpleProgram(
                functionDeclaration = listOf(
                    FunctionDeclaration(
                        name = "main",
                        params = emptyList(),
                        body = Block(
                            items = listOf(
                                D(VarDecl(VariableDeclaration("a", null, VAR_TEST_LOCATION))),
                                S(
                                    ExpressionStatement(
                                        AssignmentExpression(
                                            VariableExpression("a", VAR_TEST_LOCATION),
                                            IntExpression(1, VAR_TEST_LOCATION),
                                            VAR_TEST_LOCATION
                                        ),
                                        VAR_TEST_LOCATION
                                    )
                                ),
                                S(ReturnStatement(VariableExpression("a", VAR_TEST_LOCATION), VAR_TEST_LOCATION))
                            ),
                            VAR_TEST_LOCATION
                        ),
                        VAR_TEST_LOCATION
                    )
                ),
                VAR_TEST_LOCATION
            )

        // Act
        val resolved = identifierResolution.analyze(ast)

        // Assert: Also add the dummy location to the expected output
        val expected =
            SimpleProgram(
                functionDeclaration = listOf(
                    FunctionDeclaration(
                        name = "main",
                        params = emptyList(),
                        body = Block(
                            items = listOf(
                                D(VarDecl(VariableDeclaration("a.0", null, VAR_TEST_LOCATION))),
                                S(
                                    ExpressionStatement(
                                        AssignmentExpression(
                                            VariableExpression("a.0", VAR_TEST_LOCATION),
                                            IntExpression(1, VAR_TEST_LOCATION),
                                            VAR_TEST_LOCATION
                                        ),
                                        VAR_TEST_LOCATION
                                    )
                                ),
                                S(ReturnStatement(VariableExpression("a.0", VAR_TEST_LOCATION), VAR_TEST_LOCATION))
                            ),
                            VAR_TEST_LOCATION
                        ),
                        VAR_TEST_LOCATION
                    )
                ),
                VAR_TEST_LOCATION
            )

        assertEquals(expected, resolved)
    }

    @Test
    fun testDuplicateDeclarationThrows() {
        val ast =
            SimpleProgram(
                functionDeclaration = listOf(
                    FunctionDeclaration(
                        "main",
                        emptyList(),
                        Block(
                            listOf(
                                D(VarDecl(VariableDeclaration("a", null, VAR_TEST_LOCATION))),
                                D(VarDecl(VariableDeclaration("a", null, VAR_TEST_LOCATION)))
                            ),
                            VAR_TEST_LOCATION
                        ),
                        VAR_TEST_LOCATION
                    )
                ),
                VAR_TEST_LOCATION
            )

        assertFailsWith<DuplicateVariableDeclaration> {
            identifierResolution.analyze(ast)
        }
    }

    @Test
    fun testUndeclaredVariableThrows() {
        val ast =
            SimpleProgram(
                functionDeclaration = listOf(
                    FunctionDeclaration(
                        "main",
                        emptyList(),
                        Block(
                            listOf(
                                S(ReturnStatement(VariableExpression("x", VAR_TEST_LOCATION), VAR_TEST_LOCATION))
                            ),
                            VAR_TEST_LOCATION
                        ),
                        VAR_TEST_LOCATION
                    )
                ),
                VAR_TEST_LOCATION
            )

        assertFailsWith<MissingDeclarationException> {
            identifierResolution.analyze(ast)
        }
    }
}
