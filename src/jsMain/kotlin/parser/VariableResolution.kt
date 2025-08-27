package compiler.parser

import exceptions.DuplicateVariableDeclaration
import exceptions.UndeclaredVariableException
import parser.ASTNode
import parser.AssignmentExpression
import parser.BinaryExpression
import parser.BlockItem
import parser.D
import parser.Declaration
import parser.Expression
import parser.ExpressionStatement
import parser.Function
import parser.IntExpression
import parser.NullStatement
import parser.ReturnStatement
import parser.S
import parser.SimpleProgram
import parser.Statement
import parser.UnaryExpression
import parser.VariableExpression
import parser.Visitor

class VariableResolution : Visitor<ASTNode> {
    private var tempCounter = 0

    private fun newTemporary(name: String): String = "$name.${tempCounter++}"

    private val variableMap = mutableMapOf<String, String>()

    override fun visit(node: SimpleProgram): ASTNode {
        val function = node.functionDefinition.accept(this) as Function
        return SimpleProgram(function)
    }

    override fun visit(node: ReturnStatement): ASTNode {
        val exp = node.expression.accept(this) as Expression
        return ReturnStatement(exp)
    }

    override fun visit(node: ExpressionStatement): ASTNode {
        val exp = node.expression.accept(this) as Expression
        return ExpressionStatement(exp)
    }

    override fun visit(node: NullStatement): ASTNode = node

    override fun visit(node: Function): ASTNode {
        val body = node.body.map { it.accept(this) as BlockItem }
        return Function(node.name, body)
    }

    override fun visit(node: VariableExpression): ASTNode =
        VariableExpression(variableMap[node.name] ?: throw UndeclaredVariableException())

    override fun visit(node: UnaryExpression): ASTNode {
        val exp = node.expression.accept(this) as Expression
        return UnaryExpression(node.operator, exp)
    }

    override fun visit(node: BinaryExpression): ASTNode {
        val left = node.left.accept(this) as Expression
        val right = node.right.accept(this) as Expression
        return BinaryExpression(left, node.operator, right)
    }

    override fun visit(node: IntExpression): ASTNode = node

    override fun visit(node: AssignmentExpression): ASTNode {
        val lvalue = node.lvalue.accept(this) as VariableExpression
        val rvalue = node.rvalue.accept(this) as Expression
        return AssignmentExpression(lvalue, rvalue)
    }

    override fun visit(node: Declaration): ASTNode {
        if (node.name in variableMap) {
            throw DuplicateVariableDeclaration()
        }
        val uniqueName = newTemporary(node.name)
        variableMap[node.name] = uniqueName
        val init = node.init?.accept(this)
        return Declaration(uniqueName, init as Expression?)
    }

    override fun visit(node: S): ASTNode {
        val statement = node.statement.accept(this) as Statement
        return S(statement)
    }

    override fun visit(node: D): ASTNode {
        val declaration = node.declaration.accept(this) as Declaration
        return D(declaration)
    }
}
