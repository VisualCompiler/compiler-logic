package compiler.parser

import exceptions.InvalidStatementException
import parser.AssignmentExpression
import parser.BinaryExpression
import parser.BreakStatement
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

class LoopLabeling : Visitor<Unit> {
    var currentLabel: String? = null
    private var labelCounter = 0

    private fun newLabel(): String = "loop.${labelCounter++}"

    override fun visit(node: SimpleProgram) {
        currentLabel = null
        node.functionDefinition.accept(this)
    }

    override fun visit(node: ReturnStatement) {
        node.expression.accept(this)
    }

    override fun visit(node: ExpressionStatement) {
        node.expression.accept(this)
    }

    override fun visit(node: NullStatement) {
    }

    override fun visit(node: BreakStatement) {
        if (currentLabel == null) {
            throw InvalidStatementException("Break statement outside of loop")
        }
        node.label = currentLabel!!
    }

    override fun visit(node: ContinueStatement) {
        if (currentLabel == null) {
            throw InvalidStatementException("Continue statement outside of loop")
        }
        node.label = currentLabel!!
    }

    override fun visit(node: WhileStatement) {
        currentLabel = newLabel()
        node.label = currentLabel!!
        node.body.accept(this)
        currentLabel = null
        node.condition.accept(this)
    }

    override fun visit(node: DoWhileStatement) {
        currentLabel = newLabel()
        node.label = currentLabel!!
        node.body.accept(this)
        currentLabel = null
        node.condition.accept(this)
    }

    override fun visit(node: ForStatement) {
        currentLabel = newLabel()
        node.label = currentLabel!!
        node.body.accept(this)
        currentLabel = null
        node.post?.accept(this)
        node.condition?.accept(this)
        node.init.accept(this)
    }

    override fun visit(node: InitDeclaration) {
        node.declaration.accept(this)
    }

    override fun visit(node: InitExpression) {
        node.expression?.accept(this)
    }

    override fun visit(node: Function) {
        node.body.forEach { it.accept(this) }
    }

    override fun visit(node: VariableExpression) {
    }

    override fun visit(node: UnaryExpression) {
        node.expression.accept(this)
    }

    override fun visit(node: BinaryExpression) {
        node.left.accept(this)
        node.right.accept(this)
    }

    override fun visit(node: IntExpression) {
    }

    override fun visit(node: IfStatement) {
        node.then.accept(this)
        node._else?.accept(this)
        node.condition.accept(this)
    }

    override fun visit(node: ConditionalExpression) {
        node.codition.accept(this)
        node.thenExpression.accept(this)
        node.elseExpression.accept(this)
    }

    override fun visit(node: GotoStatement) {
    }

    override fun visit(node: LabeledStatement) {
        node.statement.accept(this)
    }

    override fun visit(node: AssignmentExpression) {
        node.lvalue.accept(this)
        node.rvalue.accept(this)
    }

    override fun visit(node: Declaration) {
        node.init?.accept(this)
    }

    override fun visit(node: S) {
        node.statement.accept(this)
    }

    override fun visit(node: D) {
        node.declaration.accept(this)
    }
}
