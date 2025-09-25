package semanticAnalysis

import exceptions.ArgumentCountException
import exceptions.IncompatibleFuncDeclarationException
import exceptions.NotAFunctionException
import exceptions.NotAVariableException
import exceptions.ReDeclarationFunctionException
import parser.AssignmentExpression
import parser.BinaryExpression
import parser.Block
import parser.BreakStatement
import parser.CompoundStatement
import parser.ConditionalExpression
import parser.ContinueStatement
import parser.D
import parser.DoWhileStatement
import parser.ExpressionStatement
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
import parser.UnaryExpression
import parser.VarDecl
import parser.VariableDeclaration
import parser.VariableExpression
import parser.Visitor
import parser.WhileStatement

class TypeChecker : Visitor<Unit> {
    fun analyze(program: SimpleProgram) {
        SymbolTable.clear() // Ensure the table is fresh for each compilation.
        program.accept(this)
    }

    override fun visit(node: SimpleProgram) {
        node.functionDeclaration.forEach { it.accept(this) }
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
    }

    override fun visit(node: ContinueStatement) {
    }

    override fun visit(node: WhileStatement) {
    }

    override fun visit(node: DoWhileStatement) {
    }

    override fun visit(node: ForStatement) {
    }

    override fun visit(node: InitDeclaration) {
    }

    override fun visit(node: InitExpression) {
    }

    override fun visit(node: FunctionDeclaration) {
        val funType = FunType(node.params.size)
        val hasBody = node.body != null
        var isAlreadyDefined = false

        val existingSymbol = SymbolTable.get(node.name)
        if (existingSymbol != null) {
            if (existingSymbol.type != funType) {
                throw IncompatibleFuncDeclarationException(node.name)
            }
            isAlreadyDefined = existingSymbol.isDefined
            if (isAlreadyDefined && hasBody) {
                throw ReDeclarationFunctionException(node.name)
            }
        }

        val newSymbol = Symbol(type = funType, isDefined = isAlreadyDefined || hasBody)
        SymbolTable.add(node.name, newSymbol)

        if (hasBody) {
            node.params.forEach { paramName ->
                SymbolTable.add(paramName, Symbol(IntType, isDefined = true))
            }
            node.body.accept(this)
        }
    }

    override fun visit(node: VariableExpression) {
        val symbol =
            SymbolTable.get(node.name)
                ?: throw IllegalStateException(node.name)

        if (symbol.type !is IntType) {
            throw NotAVariableException(node.name)
        }
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
        node.condition.accept(this)
        node.then.accept(this)
        node._else?.accept(this)
    }

    override fun visit(node: ConditionalExpression) {
        node.codition.accept(this)
        node.thenExpression.accept(this)
        node.elseExpression.accept(this)
    }

    override fun visit(node: GotoStatement) {
    }

    override fun visit(node: LabeledStatement) {
    }

    override fun visit(node: AssignmentExpression) {
        node.lvalue.accept(this)
        node.rvalue.accept(this)
    }

    override fun visit(node: VariableDeclaration) {
        SymbolTable.add(node.name, Symbol(IntType, isDefined = true))
        node.init?.accept(this)
    }

    override fun visit(node: VarDecl) {
        node.varDecl.accept(this)
    }

    override fun visit(node: FunDecl) {
        node.funDecl.accept(this)
    }

    override fun visit(node: S) {
        node.statement.accept(this)
    }

    override fun visit(node: D) {
        node.declaration.accept(this)
    }

    override fun visit(node: Block) {
        node.items.forEach { it.accept(this) }
    }

    override fun visit(node: CompoundStatement) {
        node.block.accept(this)
    }

    override fun visit(node: FunctionCall) {
        val symbol =
            SymbolTable.get(node.name)
                ?: throw exceptions.IllegalStateException(node.name)

        when (val type = symbol.type) {
            is IntType -> throw NotAFunctionException(node.name)
            is FunType -> {
                if (type.paramCount != node.arguments.size) {
                    throw ArgumentCountException(
                        name = node.name,
                        expected = type.paramCount,
                        actual = node.arguments.size
                    )
                }
            }
        }

        // Recursively type check all arguments.
        node.arguments.forEach { it.accept(this) }
    }
}
