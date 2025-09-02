package parser

import exceptions.DuplicateVariableDeclaration
import exceptions.MissingDeclarationException
import semanticAnalysis.IdentifierResolution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VariableResolutionTest {
    @Test
    fun testVariableRenamingAndResolution() {
        val ast: ASTNode =
            SimpleProgram(
                functionDeclaration =
                listOf(
                    FunctionDeclaration(
                        name = "main",
                        params = emptyList(),
                        body =
                        Block(
                            listOf(
                                D(VarDecl(VariableDeclaration(name = "a", init = null))),
                                S(
                                    ExpressionStatement(
                                        AssignmentExpression(
                                            lvalue = VariableExpression("a"),
                                            rvalue = IntExpression(1)
                                        )
                                    )
                                ),
                                S(ReturnStatement(expression = VariableExpression("a")))
                            )
                        )
                    )
                )
            )

        val resolved = IdentifierResolution().analyze(ast as SimpleProgram) as SimpleProgram

        val expected: ASTNode =
            SimpleProgram(
                functionDeclaration =
                listOf(
                    FunctionDeclaration(
                        name = "main",
                        params = emptyList(),
                        body =
                        Block(
                            block =
                            listOf(
                                D(VarDecl(VariableDeclaration(name = "a.0", init = null))),
                                S(
                                    ExpressionStatement(
                                        AssignmentExpression(
                                            lvalue = VariableExpression("a.0"),
                                            rvalue = IntExpression(1)
                                        )
                                    )
                                ),
                                S(ReturnStatement(expression = VariableExpression("a.0")))
                            )
                        )
                    )
                )
            )
        assertEquals(expected, resolved)
    }

    @Test
    fun testDuplicateDeclarationThrows() {
        val ast: ASTNode =
            SimpleProgram(
                functionDeclaration =
                listOf(
                    FunctionDeclaration(
                        name = "main",
                        params = emptyList(),
                        body =
                        Block(
                            block =
                            listOf(
                                D(VarDecl(VariableDeclaration(name = "a", init = null))),
                                D(VarDecl(VariableDeclaration(name = "a", init = null)))
                            )
                        )
                    )
                )
            )

        // Act & Assert
        assertFailsWith<DuplicateVariableDeclaration> {
            IdentifierResolution().analyze(ast as SimpleProgram)
        }
    }

    @Test
    fun testUndeclaredVariableThrows() {
        val ast: ASTNode =
            SimpleProgram(
                functionDeclaration =
                listOf(
                    FunctionDeclaration(
                        name = "main",
                        params = emptyList(),
                        body =
                        Block(
                            block =
                            listOf(
                                S(ReturnStatement(expression = VariableExpression("x")))
                            )
                        )
                    )
                )
            )

        // Act & Assert
        assertFailsWith<MissingDeclarationException> {
            IdentifierResolution().analyze(ast as SimpleProgram)
        }
    }
}
