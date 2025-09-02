package parser

import exceptions.DuplicateLabelException
import exceptions.UndeclaredLabelException
import symanticAnalysis.LabelCollector
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LabelAnalysisTest {
    private val labelAnalysis = LabelCollector.LabelAnalysis()

    @Test
    fun `test valid labels and gotos complete successfully`() {
        // Arrange: A program with a forward jump and a backward jump.
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
                )
            )

        // Act & Assert: This should complete without throwing an exception.
        labelAnalysis.analyze(ast)
        assertTrue(true, "Analysis of valid labels and gotos should complete successfully.")
    }

    @Test
    fun `test duplicate label throws DuplicateLabelException`() {
        // Arrange: A program where the same label is defined twice.
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
                functionDeclaration =
                listOf(
                    FunctionDeclaration(
                        name = "main",
                        params = emptyList(),
                        body =
                        Block(
                            block =
                            listOf(
                                S(GotoStatement("missing_label")),
                                S(ReturnStatement(IntExpression(0)))
                            )
                        )
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
                listOf(
                    FunctionDeclaration(
                        name = "main",
                        params = emptyList(),
                        body =
                        Block(
                            listOf(
                                S(
                                    IfStatement(
                                        condition = IntExpression(1),
                                        then =
                                        LabeledStatement(
                                            label = "nested_label",
                                            statement = ReturnStatement(IntExpression(1))
                                        ),
                                        _else = null
                                    )
                                ),
                                S(GotoStatement("nested_label"))
                            )
                        )
                    )
                )
            )

        // Act & Assert: This should complete successfully without throwing an exception.
        labelAnalysis.analyze(ast)
        assertTrue(true, "Analysis of nested labels should complete successfully.")
    }
}
