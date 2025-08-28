package compiler.parser

import exceptions.DuplicateLabelException
import exceptions.UndeclaredLabelException
import parser.ASTNode
import parser.AssignmentExpression
import parser.BinaryExpression
import parser.ConditionalExpression
import parser.D
import parser.Declaration
import parser.ExpressionStatement
import parser.Function
import parser.GotoStatement
import parser.IfStatement
import parser.IntExpression
import parser.LabeledStatement
import parser.NullStatement
import parser.ReturnStatement
import parser.S
import parser.SimpleProgram
import parser.UnaryExpression
import parser.VariableExpression
import parser.Visitor

class LabelCollector : Visitor<Unit> {
    val definedLabels: MutableSet<String> = mutableSetOf<String>()

    override fun visit(node: LabeledStatement) {
        if (!definedLabels.add(node.label)) {
            throw DuplicateLabelException(node.label)
        }
        node.statement.accept(this)
    }

    override fun visit(node: AssignmentExpression) {
    }

    override fun visit(node: Declaration) {
    }

    override fun visit(node: SimpleProgram) {
        node.functionDefinition.accept(this)
    }

    override fun visit(node: Function) {
        node.body.forEach { it.accept(this) }
    }

    override fun visit(node: VariableExpression) {
    }

    override fun visit(node: UnaryExpression) {
    }

    override fun visit(node: BinaryExpression) {
    }

    override fun visit(node: IntExpression) {
    }

    override fun visit(node: IfStatement) {
        node.then.accept(this)
        node._else?.accept(this)
    }

    override fun visit(node: ConditionalExpression) {
    }

    override fun visit(node: S) {
        node.statement.accept(this)
    }

    override fun visit(node: D) {}

    override fun visit(node: ReturnStatement) {}

    override fun visit(node: ExpressionStatement) {}

    override fun visit(node: NullStatement) {}

    override fun visit(node: GotoStatement) {}
}

private class GotoValidator(
    private val definedLabels: Set<String>
) : Visitor<Unit> {
    override fun visit(node: GotoStatement) {
        if (node.label !in definedLabels) {
            throw UndeclaredLabelException(node.label)
        }
    }

    // --- Pass-through methods ---
    override fun visit(node: SimpleProgram) {
        node.functionDefinition.accept(this)
    }

    override fun visit(node: Function) {
        node.body.forEach { it.accept(this) }
    }

    override fun visit(node: VariableExpression) {
    }

    override fun visit(node: UnaryExpression) {
    }

    override fun visit(node: BinaryExpression) {
    }

    override fun visit(node: IntExpression) {
    }

    override fun visit(node: IfStatement) {
        node.then.accept(this)
        node._else?.accept(this)
    }

    override fun visit(node: ConditionalExpression) {
    }

    override fun visit(node: LabeledStatement) {
        node.statement.accept(this)
    }

    override fun visit(node: AssignmentExpression) {
    }

    override fun visit(node: Declaration) {
    }

    override fun visit(node: S) {
        node.statement.accept(this)
    }

    override fun visit(node: D) {}

    override fun visit(node: ReturnStatement) {}

    override fun visit(node: ExpressionStatement) {}

    override fun visit(node: NullStatement) {}
}

class LabelAnalysis {
    fun analyze(ast: ASTNode) {
        console.log("[LabelAnalysis] Starting analysis on AST: $ast")

        val collector = LabelCollector()
        ast.accept(collector)

        val validator = GotoValidator(collector.definedLabels)
        ast.accept(validator)
    }
}
