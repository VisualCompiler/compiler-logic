package tacky

import exceptions.TackyException
import lexer.TokenType
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

class TackyGenVisitor : Visitor<TackyConstruct?> {
    private var tempCounter = 0
    private var labelCounter = 0

    private fun newTemporary(): TackyVar = TackyVar("tmp.${tempCounter++}")

    private fun newLabel(
        base: String,
        sourceId: String = ""
    ): TackyLabel = TackyLabel(".L_${base}_${labelCounter++}", sourceId)

    private val currentInstructions = mutableListOf<TackyInstruction>()

    fun reset() {
        tempCounter = 0
        labelCounter = 0
        currentInstructions.clear()
    }

    private fun convertUnaryOp(tokenType: TokenType): TackyUnaryOP =
        if (tokenType == TokenType.TILDE) {
            TackyUnaryOP.COMPLEMENT
        } else if (tokenType == TokenType.NEGATION) {
            TackyUnaryOP.NEGATE
        } else if (tokenType == TokenType.NOT) {
            TackyUnaryOP.NOT
        } else {
            throw TackyException(tokenType.toString())
        }

    private fun convertBinaryOp(tokenType: TokenType): TackyBinaryOP {
        when (tokenType) {
            TokenType.PLUS -> {
                return TackyBinaryOP.ADD
            }
            TokenType.NEGATION -> {
                return TackyBinaryOP.SUBTRACT
            }
            TokenType.MULTIPLY -> {
                return TackyBinaryOP.MULTIPLY
            }
            TokenType.DIVIDE -> {
                return TackyBinaryOP.DIVIDE
            }
            TokenType.REMAINDER -> {
                return TackyBinaryOP.REMAINDER
            }
            TokenType.EQUAL_TO -> {
                return TackyBinaryOP.EQUAL
            }
            TokenType.GREATER -> {
                return TackyBinaryOP.GREATER
            }
            TokenType.LESS -> {
                return TackyBinaryOP.LESS
            }
            TokenType.GREATER_EQUAL -> {
                return TackyBinaryOP.GREATER_EQUAL
            }
            TokenType.LESS_EQUAL -> {
                return TackyBinaryOP.LESS_EQUAL
            }
            TokenType.NOT_EQUAL -> {
                return TackyBinaryOP.NOT_EQUAL
            }
            else -> {
                throw TackyException(tokenType.toString())
            }
        }
    }

    override fun visit(node: SimpleProgram): TackyConstruct {
        // Reset counter
        reset()
        val tackyFunction = node.functionDeclaration.filter { it.body != null }.map { it.accept(this) as TackyFunction }
        return TackyProgram(tackyFunction)
    }

    override fun visit(node: ReturnStatement): TackyConstruct {
        val value = node.expression.accept(this) as TackyVal
        val instr = TackyRet(value, node.id)
        currentInstructions += instr
        return instr
    }

    override fun visit(node: ExpressionStatement): TackyConstruct {
        val result = node.expression.accept(this) as TackyVal
        return result
    }

    override fun visit(node: NullStatement): TackyConstruct? = null

    override fun visit(node: BreakStatement): TackyConstruct? {
        val breakLabel = TackyLabel("break_${node.label}", node.id)
        currentInstructions += TackyJump(breakLabel, node.id)
        return null
    }

    override fun visit(node: ContinueStatement): TackyConstruct? {
        val continueLabel = TackyLabel("continue_${node.label}", node.id)
        currentInstructions += TackyJump(continueLabel, node.id)
        return null
    }

    override fun visit(node: WhileStatement): TackyConstruct? {
        val continueLabel = TackyLabel("continue_${node.label}", node.id)
        val breakLabel = TackyLabel("break_${node.label}", node.id)
        currentInstructions += continueLabel
        val condition = node.condition.accept(this) as TackyVal
        currentInstructions += JumpIfZero(condition, breakLabel, node.id)
        node.body.accept(this)
        currentInstructions += TackyJump(continueLabel, node.id)
        currentInstructions += breakLabel
        return null
    }

    override fun visit(node: DoWhileStatement): TackyConstruct? {
        val startLabel = TackyLabel("start_${node.label}", node.id)
        val continueLabel = TackyLabel("continue_${node.label}", node.id)
        val breakLabel = TackyLabel("break_${node.label}", node.id)
        currentInstructions += startLabel
        node.body.accept(this)
        currentInstructions += continueLabel
        val condition = node.condition.accept(this) as TackyVal
        currentInstructions += JumpIfNotZero(condition, startLabel, node.id)
        currentInstructions += breakLabel
        return null
    }

    override fun visit(node: ForStatement): TackyConstruct? {
        val startLabel = TackyLabel("start_${node.label}", node.id)
        val continueLabel = TackyLabel("continue_${node.label}", node.id)
        val breakLabel = TackyLabel("break_${node.label}", node.id)
        node.init.accept(this)
        currentInstructions += startLabel
        if (node.condition != null) {
            val condition = node.condition.accept(this) as TackyVal
            currentInstructions += JumpIfZero(condition, breakLabel, node.id)
        }
        node.body.accept(this)
        currentInstructions += continueLabel
        node.post?.accept(this)
        currentInstructions += TackyJump(startLabel, node.id)
        currentInstructions += breakLabel
        return null
    }

    override fun visit(node: InitDeclaration): TackyConstruct? {
        node.varDeclaration.accept(this)
        return null
    }

    override fun visit(node: InitExpression): TackyConstruct? {
        node.expression?.accept(this)
        return null
    }

    override fun visit(node: FunctionDeclaration): TackyConstruct {
        val functionName = node.name
        val functionParams = node.params

        currentInstructions.clear()
        node.body?.accept(this)

        if (currentInstructions.lastOrNull() !is TackyRet) {
            currentInstructions += TackyRet(TackyConstant(0), node.id)
        }
        return TackyFunction(functionName, functionParams, currentInstructions.toList(), node.id)
    }

    override fun visit(node: VarDecl): TackyConstruct? {
        node.varDecl.accept(this)
        return null
    }

    override fun visit(node: FunDecl): TackyConstruct? {
        node.funDecl.accept(this)
        return null
    }

    override fun visit(node: VariableExpression): TackyConstruct = TackyVar(node.name)

    override fun visit(node: UnaryExpression): TackyConstruct {
        val src = node.expression.accept(this) as TackyVal
        val dst = newTemporary()
        val op = convertUnaryOp(node.operator.type)
        currentInstructions += TackyUnary(op, src, dst, node.id)
        return dst
    }

    override fun visit(node: BinaryExpression): TackyConstruct {
        when (node.operator.type) {
            TokenType.AND -> {
                val falseLabel = newLabel("and_false", node.id)
                val endLabel = newLabel("and_end", node.id)
                val resultVar = newTemporary()

                val left = node.left.accept(this) as TackyVal
                currentInstructions += JumpIfZero(left, falseLabel, node.id)
                val right = node.right.accept(this) as TackyVal
                currentInstructions += JumpIfZero(right, falseLabel, node.id)
                currentInstructions += TackyCopy(TackyConstant(1), resultVar, node.id)
                currentInstructions += TackyJump(endLabel, node.id)
                currentInstructions += falseLabel
                currentInstructions += TackyCopy(TackyConstant(0), resultVar, node.id)
                currentInstructions += endLabel

                return resultVar
            }
            TokenType.OR -> {
                val trueLabel = newLabel("or_true", node.id)
                val endLabel = newLabel("or_end", node.id)
                val resultVar = newTemporary()

                val left = node.left.accept(this) as TackyVal
                currentInstructions += JumpIfNotZero(left, trueLabel, node.id)
                val right = node.right.accept(this) as TackyVal
                currentInstructions += JumpIfNotZero(right, trueLabel, node.id)
                currentInstructions += TackyCopy(TackyConstant(0), resultVar, node.id)
                currentInstructions += TackyJump(endLabel, node.id)
                currentInstructions += trueLabel
                currentInstructions += TackyCopy(TackyConstant(1), resultVar, node.id)
                currentInstructions += endLabel

                return resultVar
            }
            else -> {
                val src1 = node.left.accept(this) as TackyVal
                val src2 = node.right.accept(this) as TackyVal
                val op = convertBinaryOp(node.operator.type)

                val dst = newTemporary()
                currentInstructions += TackyBinary(operator = op, src1 = src1, src2 = src2, dest = dst, sourceId = node.id)
                return dst
            }
        }
    }

    override fun visit(node: IntExpression): TackyConstruct = TackyConstant(node.value)

    override fun visit(node: IfStatement): TackyConstruct? {
        val endLabel = newLabel("end", node.id)

        val condition = node.condition.accept(this) as TackyVal
        if (node._else == null) {
            currentInstructions += JumpIfZero(condition, endLabel, node.id)
            node.then.accept(this)
            currentInstructions += endLabel
        } else {
            val elseLabel = newLabel("else_label", node.id)
            currentInstructions += JumpIfZero(condition, elseLabel, node.id)
            node.then.accept(this)
            currentInstructions += TackyJump(endLabel, node.id)
            currentInstructions += elseLabel
            node._else.accept(this)
            currentInstructions += endLabel
        }
        return null
    }

    override fun visit(node: ConditionalExpression): TackyConstruct? {
        val resultVar = newTemporary()

        val elseLabel = newLabel("cond_else", node.id)
        val endLabel = newLabel("cond_end", node.id)

        val conditionResult = node.condition.accept(this) as TackyVal
        currentInstructions += JumpIfZero(conditionResult, elseLabel, node.id)

        val thenResult = node.thenExpression.accept(this) as TackyVal
        currentInstructions += TackyCopy(thenResult, resultVar, node.id)
        currentInstructions += TackyJump(endLabel, node.id)
        currentInstructions += elseLabel
        val elseResult = node.elseExpression.accept(this) as TackyVal
        currentInstructions += TackyCopy(elseResult, resultVar, node.id)
        currentInstructions += endLabel

        return resultVar
    }

    override fun visit(node: GotoStatement): TackyConstruct? {
        currentInstructions += TackyJump(TackyLabel(node.label, node.id), node.id)
        return null
    }

    override fun visit(node: LabeledStatement): TackyConstruct? {
        val label = node.label
        currentInstructions += TackyLabel(node.label, node.id)
        node.statement.accept(this)
        return null
    }

    override fun visit(node: AssignmentExpression): TackyConstruct {
        val rvalue = node.rvalue.accept(this) as TackyVal
        val dest = TackyVar(node.lvalue.name)
        currentInstructions += TackyCopy(rvalue, dest, node.id)
        return dest
    }

    override fun visit(node: VariableDeclaration): TackyConstruct? {
        if (node.init != null) {
            val initVal = node.init.accept(this) as TackyVal
            // The `node.name` is already the unique name from IdentifierResolution
            currentInstructions += TackyCopy(initVal, TackyVar(node.name), node.id)
        }
        return null
    }

    override fun visit(node: S): TackyConstruct? {
        node.statement.accept(this)
        return null
    }

    override fun visit(node: D): TackyConstruct? {
        node.declaration.accept(this)
        return null
    }

    override fun visit(node: Block): TackyConstruct? {
        node.items.forEach { it.accept(this) }
        return null
    }

    override fun visit(node: CompoundStatement): TackyConstruct? {
        node.block.accept(this)
        return null
    }

    override fun visit(node: FunctionCall): TackyConstruct? {
        val args = node.arguments.map { it.accept(this) as TackyVal }
        val dest = newTemporary()
        currentInstructions += TackyFunCall(node.name, args, dest, node.id)
        return dest
    }
}
