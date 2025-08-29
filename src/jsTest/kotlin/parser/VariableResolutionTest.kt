package parser

import compiler.parser.VariableResolution
import exceptions.DuplicateVariableDeclaration
import exceptions.UndeclaredVariableException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VariableResolutionTest {
    @Test
    fun testVariableRenamingAndResolution() {
        val ast: ASTNode =
            SimpleProgram(
                functionDefinition =
                Function(
                    name = "main",
                    body =
                    Block(
                        listOf(
                            D(Declaration(name = "a", init = null)),
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

        val resolved = VariableResolution().visit(ast as SimpleProgram) as SimpleProgram

        val expected: ASTNode =
            SimpleProgram(
                functionDefinition =
                Function(
                    name = "main",
                    body =
                    Block(
                        listOf(
                            D(Declaration(name = "a.0", init = null)),
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

        assertEquals(expected, resolved)
    }

    @Test
    fun testDuplicateDeclarationThrows() {
        val ast: ASTNode =
            SimpleProgram(
                functionDefinition =
                Function(
                    name = "main",
                    body =
                    Block(
                        listOf(
                            D(Declaration(name = "a", init = null)),
                            D(Declaration(name = "a", init = null))
                        )
                    )
                )
            )

        assertFailsWith<DuplicateVariableDeclaration> {
            VariableResolution().visit(ast as SimpleProgram)
        }
    }

    @Test
    fun testUndeclaredVariableThrows() {
        val ast: ASTNode =
            SimpleProgram(
                functionDefinition =
                Function(
                    name = "main",
                    body =
                    Block(
                        listOf(
                            S(ReturnStatement(expression = VariableExpression("x")))
                        )
                    )
                )
            )

        assertFailsWith<UndeclaredVariableException> {
            VariableResolution().visit(ast as SimpleProgram)
        }
    }
}
