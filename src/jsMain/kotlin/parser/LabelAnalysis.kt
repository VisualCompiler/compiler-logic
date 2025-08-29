package compiler.parser

import exceptions.DuplicateLabelException
import exceptions.UndeclaredLabelException
import parser.ASTNode
import parser.AssignmentExpression
import parser.BinaryExpression
import parser.Block
import parser.BreakStatement
import parser.CompoundStatement
import parser.ConditionalExpression
import parser.ContinueStatement
import parser.D
import parser.Declaration
import parser.DoWhileStatement
import parser.ExpressionStatement
import parser.ForStatement
import parser.Function
import parser.GotoStatement
import parser.IfStatement
import parser.InitDeclaration
import parser.InitExpression
import parser.IntExpression
import parser.LabeledStatement
import parser.NullStatement
import parser.ReturnStatement
import parser.S
import parser.SimpleProgram
import parser.UnaryExpression
import parser.VariableExpression
import parser.Visitor
import parser.WhileStatement

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
        node.body.accept(this)
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

    override fun visit(node: BreakStatement) {
        // No labels to collect
    }

    override fun visit(node: ContinueStatement) {
        // No labels to collect
    }

    override fun visit(node: WhileStatement) {
        node.body.accept(this)
        node.condition.accept(this)
    }

    override fun visit(node: DoWhileStatement) {
        node.body.accept(this)
        node.condition.accept(this)
    }

    override fun visit(node: ForStatement) {
        node.init.accept(this)
        node.condition?.accept(this)
        node.post?.accept(this)
        node.body.accept(this)
    }

    override fun visit(node: InitDeclaration) {
        node.declaration.accept(this)
    }

    override fun visit(node: InitExpression) {
        node.expression?.accept(this)
    }

    override fun visit(node: GotoStatement) {}

    override fun visit(node: Block) {
        node.block.forEach { it.accept(this) }
    }

    override fun visit(node: CompoundStatement) {
        node.block.accept(this)
    }
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
        node.body.accept(this)
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

    override fun visit(node: BreakStatement) {
        // No goto statements to validate
    }

    override fun visit(node: ContinueStatement) {
        // No goto statements to validate
    }

    override fun visit(node: WhileStatement) {
        node.body.accept(this)
        node.condition.accept(this)
    }

    override fun visit(node: DoWhileStatement) {
        node.body.accept(this)
        node.condition.accept(this)
    }

    override fun visit(node: ForStatement) {
        node.init.accept(this)
        node.condition?.accept(this)
        node.post?.accept(this)
        node.body.accept(this)
    }

    override fun visit(node: InitDeclaration) {
        node.declaration.accept(this)
    }

    override fun visit(node: InitExpression) {
        node.expression?.accept(this)
    }

    override fun visit(node: Block) {
        node.block.forEach { it.accept(this) }
    }

    override fun visit(node: CompoundStatement) {
        node.block.accept(this)
    }
}

class LabelAnalysis {
    fun analyze(ast: ASTNode) {
        val collector = LabelCollector()
        ast.accept(collector)

        val validator = GotoValidator(collector.definedLabels)
        ast.accept(validator)
    }
}
