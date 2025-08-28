package compiler.parser

import exceptions.DuplicateVariableDeclaration
import exceptions.UndeclaredVariableException
import parser.ASTNode
import parser.AssignmentExpression
import parser.BinaryExpression
import parser.BlockItem
import parser.BreakStatement
import parser.ConditionalExpression
import parser.ContinueStatement
import parser.D
import parser.Declaration
import parser.DoWhileStatement
import parser.Expression
import parser.ExpressionStatement
import parser.ForInit
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
import parser.Statement
import parser.UnaryExpression
import parser.VariableExpression
import parser.Visitor
import parser.WhileStatement

class VariableResolution : Visitor<ASTNode> {
    private var tempCounter = 0

    private fun newTemporary(name: String): String = "$name.${tempCounter++}"

    private val variableMap = mutableMapOf<String, String>()

    override fun visit(node: SimpleProgram): ASTNode {
        // Reset state for each program to avoid leaking declarations across compilations
        variableMap.clear()
        tempCounter = 0
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

    override fun visit(node: BreakStatement): ASTNode = node

    override fun visit(node: ContinueStatement): ASTNode = node

    override fun visit(node: WhileStatement): ASTNode {
        val cond = node.condition.accept(this) as Expression
        val newBody = node.body.accept(this) as Statement
        return WhileStatement(cond, newBody, node.label)
    }

    override fun visit(node: DoWhileStatement): ASTNode {
        val cond = node.condition.accept(this) as Expression
        val newBody = node.body.accept(this) as Statement
        return DoWhileStatement(cond, newBody, node.label)
    }

    override fun visit(node: ForStatement): ASTNode {
        // Create a new inner scope for the for-loop header variables
        val saved = variableMap.toMutableMap()
        val newInit = node.init.accept(this) as ForInit
        val newCond = node.condition?.accept(this) as Expression?
        val newPost = node.post?.accept(this) as Expression?
        val newBody = node.body.accept(this) as Statement
        // Restore the outer scope so header declarations are not visible outside
        variableMap.clear()
        variableMap.putAll(saved)
        return ForStatement(newInit, newCond, newPost, newBody, node.label)
    }

    override fun visit(node: InitDeclaration): ASTNode {
        val newDecl = node.declaration.accept(this) as Declaration
        return InitDeclaration(newDecl)
    }

    override fun visit(node: InitExpression): ASTNode {
        val newExp = node.expression?.accept(this) as Expression?
        return InitExpression(newExp)
    }

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

    override fun visit(node: IfStatement): ASTNode {
        val condition = node.condition.accept(this) as Expression
        val thenStatement = node.then.accept(this) as Statement
        val elseStatement = node._else?.accept(this) as Statement?
        return IfStatement(condition, thenStatement, elseStatement)
    }

    override fun visit(node: ConditionalExpression): ASTNode {
        val condition = node.codition.accept(this) as Expression
        val thenExpression = node.thenExpression.accept(this) as Expression
        val elseExpression = node.elseExpression.accept(this) as Expression
        return ConditionalExpression(condition, thenExpression, elseExpression)
    }

    override fun visit(node: GotoStatement): ASTNode = node

    override fun visit(node: LabeledStatement): ASTNode {
        val statement = node.statement.accept(this) as Statement
        return LabeledStatement(node.label, statement)
    }

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
