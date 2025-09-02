package compiler.symanticAnalysis

import exceptions.DuplicateVariableDeclaration
import exceptions.NestedFunctionException
import exceptions.UndeclaredVariableException
import parser.ASTNode
import parser.AssignmentExpression
import parser.BinaryExpression
import parser.Block
import parser.BlockItem
import parser.BreakStatement
import parser.CompoundStatement
import parser.ConditionalExpression
import parser.ContinueStatement
import parser.D
import parser.Declaration
import parser.DoWhileStatement
import parser.Expression
import parser.ExpressionStatement
import parser.ForInit
import parser.ForStatement
import parser.FunDecl
import parser.FunctionCall
import parser.FunctionDeclaration
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
import parser.VarDecl
import parser.VariableDeclaration
import parser.VariableExpression
import parser.Visitor
import parser.WhileStatement

data class SymbolInfo(
    val uniqueName: String,
    val hasLinkage: Boolean
)

class IdentifierResolution : Visitor<ASTNode> {
    private var tempCounter = 0

    private fun newTemporary(name: String): String = "$name.${tempCounter++}"

    // private val variableMap = mutableMapOf<String, VariableMapEntry>()

    private val scopeStack = mutableListOf<MutableMap<String, SymbolInfo>>()

    fun analyze(program: SimpleProgram): SimpleProgram {
        tempCounter = 0
        scopeStack.clear()
        enterScope()
        val result = program.accept(this) as SimpleProgram
        leaveScope()
        if (scopeStack.isNotEmpty()) {
            throw IllegalStateException("Scope stack was not empty after analysis.")
        }
        return result
    }

    private fun declare(
        name: String,
        hasLinkage: Boolean
    ): String {
        val currentScope = scopeStack.last()
        val existing = currentScope[name]

        if (existing != null) {
            // A redeclaration in the same scope is only okay if both have linkage.
            if (!existing.hasLinkage || !hasLinkage) {
                throw DuplicateVariableDeclaration()
            }
            // If both have linkage (e.g., two function declarations), it's okay.
            return existing.uniqueName
        }
        val uniqueName = if (hasLinkage) name else newTemporary(name)
        currentScope[name] = SymbolInfo(uniqueName, hasLinkage)
        return uniqueName
    }

    private fun enterScope() = scopeStack.add(mutableMapOf())

    private fun leaveScope() = scopeStack.removeAt(scopeStack.lastIndex)

    private fun resolve(name: String): SymbolInfo {
        scopeStack.asReversed().forEach { scope ->
            if (scope.containsKey(name)) {
                return scope.getValue(name)
            }
        }
        throw UndeclaredVariableException()
    }

    override fun visit(node: SimpleProgram): ASTNode {
        val newDecls = node.functionDeclaration.map { it.accept(this) as FunctionDeclaration }
        return SimpleProgram(newDecls)
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
        enterScope()

        val newInit = node.init.accept(this) as ForInit

        val newCond = node.condition?.accept(this) as Expression?
        val newPost = node.post?.accept(this) as Expression?
        val newBody = node.body.accept(this) as Statement

        leaveScope()

        return ForStatement(newInit, newCond, newPost, newBody)
    }

    override fun visit(node: InitDeclaration): ASTNode {
        val newDecl = node.varDeclaration.accept(this) as VariableDeclaration
        return InitDeclaration(newDecl)
    }

    override fun visit(node: InitExpression): ASTNode {
        val newExp = node.expression?.accept(this) as Expression?
        return InitExpression(newExp)
    }

    override fun visit(node: FunctionDeclaration): ASTNode {
        // Check if we are inside another function's scope
        if (scopeStack.size > 1) {
            // We're inside another function - check if this is a prototype or definition
            if (node.body != null) {
                // Function definition with body - not allowed inside other functions
                throw NestedFunctionException()
            } else {
                // Function prototype (no body) - allowed inside other functions
                // Just declare the function name and return it
                declare(node.name, hasLinkage = true)
                return FunctionDeclaration(node.name, node.params, null)
            }
        } else {
            // We're at global scope - all function declarations are allowed
            declare(node.name, hasLinkage = true)

            enterScope()

            val newParams =
                node.params.map { paramName ->
                    declare(paramName, hasLinkage = false)
                }
            val newBody = node.body?.accept(this) as Block?

            leaveScope()

            return FunctionDeclaration(node.name, newParams, newBody)
        }
    }

    override fun visit(node: VariableExpression): ASTNode {
        val symbol = resolve(node.name)
        return VariableExpression(symbol.uniqueName)
    }

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

    override fun visit(node: Declaration): ASTNode = throw IllegalStateException("Should not visit Declaration directly.")

    override fun visit(node: VariableDeclaration): ASTNode {
        val newInit = node.init?.accept(this) as Expression?

        val uniqueName = declare(node.name, hasLinkage = false)

        return VariableDeclaration(uniqueName, newInit)
    }

    override fun visit(node: S): ASTNode {
        val statement = node.statement.accept(this) as Statement
        return S(statement)
    }

    override fun visit(node: D): ASTNode {
        val declaration = node.declaration.accept(this)
        return when (declaration) {
            is VarDecl -> D(declaration)
            is FunDecl -> D(declaration)
            else -> throw IllegalStateException("Unexpected declaration type: ${declaration::class.simpleName}")
        }
    }

    override fun visit(node: VarDecl): ASTNode {
        val newVarDeclData = node.varDecl.accept(this) as VariableDeclaration
        return VarDecl(newVarDeclData)
    }

    override fun visit(node: FunDecl): ASTNode {
        val funDecl = node.funDecl.accept(this) as FunctionDeclaration

        if (funDecl.body != null) {
            throw NestedFunctionException()
        } else {
            return FunDecl(funDecl)
        }
    }

    override fun visit(node: Block): ASTNode {
        enterScope()
        val newItems = node.block.map { it.accept(this) as BlockItem }
        leaveScope()
        return Block(newItems)
    }

    override fun visit(node: CompoundStatement): ASTNode {
        val newBlock = node.block.accept(this) as Block
        return CompoundStatement(newBlock)
    }

    override fun visit(node: FunctionCall): ASTNode {
        val symbol = resolve(node.name)
        val newArgs = node.arguments.map { it.accept(this) as Expression }
        // The unique name for a function is just its original name.
        return FunctionCall(symbol.uniqueName, newArgs)
    }
}
