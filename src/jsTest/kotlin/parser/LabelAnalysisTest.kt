package parser

import compiler.parser.LabelAnalysis
import exceptions.DuplicateLabelException
import exceptions.UndeclaredLabelException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LabelAnalysisTest {
    private val labelAnalysis = LabelAnalysis()

    @Test
    fun `test valid labels and gotos`() {
        // Arrange: A program with a forward jump and a backward jump.
        val ast: ASTNode =
            SimpleProgram(
                functionDefinition =
                Function(
                    name = "main",
                    body =
                    listOf(
                        S(GotoStatement("end")),
                        S(
                            LabeledStatement(
                                label = "start",
                                statement = ExpressionStatement(IntExpression(1))
                            )
                        ),
                        S(GotoStatement("start")),
                        S(
                            LabeledStatement(
                                label = "end",
                                statement = ReturnStatement(IntExpression(0))
                            )
                        )
                    )
                )
            )

        // Act & Assert: This should complete successfully without throwing an exception.
        // If it throws, the test will fail automatically.
        labelAnalysis.analyze(ast)
        assertTrue(true, "Analysis of valid labels and gotos should complete successfully.")
    }

    @Test
    fun `test duplicate label throws DuplicateLabelException`() {
        // Arrange: A program where the same label is defined twice.
        val ast: ASTNode =
            SimpleProgram(
                functionDefinition =
                Function(
                    name = "main",
                    body =
                    listOf(
                        S(
                            LabeledStatement(
                                label = "my_label",
                                statement = NullStatement()
                            )
                        ),
                        S(
                            LabeledStatement(
                                label = "my_label",
                                statement = ReturnStatement(IntExpression(0))
                            )
                        )
                    )
                )
            )

        // Act & Assert: Expect the analysis to fail with the specific exception.
        assertFailsWith<DuplicateLabelException> {
            labelAnalysis.analyze(ast)
        }
    }

    @Test
    fun `test undeclared label throws UndeclaredLabelException`() {
        // Arrange: A program with a goto that targets a non-existent label.
        val ast: ASTNode =
            SimpleProgram(
                functionDefinition =
                Function(
                    name = "main",
                    body =
                    listOf(
                        S(GotoStatement("missing_label")),
                        S(ReturnStatement(IntExpression(0)))
                    )
                )
            )

        // Act & Assert: Expect the analysis to fail with the specific exception.
        assertFailsWith<UndeclaredLabelException> {
            labelAnalysis.analyze(ast)
        }
    }

    @Test
    fun `test nested labels are found correctly`() {
        // Arrange: A program where labels are nested inside an if statement.
        val ast: ASTNode =
            SimpleProgram(
                functionDefinition =
                Function(
                    name = "main",
                    body =
                    listOf(
                        S(
                            IfStatement(
                                condition = IntExpression(1),
                                then = (
                                    LabeledStatement(
                                        label = "then_branch",
                                        statement = GotoStatement("end")
                                    )
                                    ),
                                _else = (
                                    LabeledStatement(
                                        label = "else_branch",
                                        statement = GotoStatement("end")
                                    )
                                    )
                            )
                        ),
                        S(
                            LabeledStatement(
                                label = "end",
                                statement = ReturnStatement(IntExpression(0))
                            )
                        )
                    )
                )
            )

        // Act & Assert: This should complete successfully.
        labelAnalysis.analyze(ast)
        assertTrue(true, "Analysis of nested labels should complete successfully.")
    }
}
