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

class TackyGenVisitor : Visitor<TackyConstruct?> {
    private var tempCounter = 0
    private var labelCounter = 0

    private fun newTemporary(): TackyVar = TackyVar("tmp.${tempCounter++}")

    private fun newLabel(base: String): TackyLabel = TackyLabel(".L_${base}_${labelCounter++}")

    private val currentInstructions = mutableListOf<TackyInstruction>()

    private fun convertUnaryOp(tokenType: TokenType): TackyUnaryOP {
        if (tokenType == TokenType.TILDE) {
            return TackyUnaryOP.COMPLEMENT
        } else if (tokenType == TokenType.NEGATION) {
            return TackyUnaryOP.NEGATE
        } else if (tokenType == TokenType.NOT) {
            return TackyUnaryOP.NOT
        } else {
            throw TackyException(tokenType.toString())
        }
    }

    private fun convertBinaryOp(tokenType: TokenType): TackyBinaryOP {
        if (tokenType == TokenType.PLUS) {
            return TackyBinaryOP.ADD
        } else if (tokenType == TokenType.NEGATION) {
            return TackyBinaryOP.SUBTRACT
        } else if (tokenType == TokenType.MULTIPLY) {
            return TackyBinaryOP.MULTIPLY
        } else if (tokenType == TokenType.DIVIDE) {
            return TackyBinaryOP.DIVIDE
        } else if (tokenType == TokenType.REMAINDER) {
            return TackyBinaryOP.REMAINDER
        } else if (tokenType == TokenType.EQUAL_TO) {
            return TackyBinaryOP.EQUAL
        } else if (tokenType == TokenType.GREATER) {
            return TackyBinaryOP.GREATER
        } else if (tokenType == TokenType.LESS) {
            return TackyBinaryOP.LESS
        } else if (tokenType == TokenType.GREATER_EQUAL) {
            return TackyBinaryOP.GREATER_EQUAL
        } else if (tokenType == TokenType.LESS_EQUAL) {
            return TackyBinaryOP.LESS_EQUAL
        } else if (tokenType == TokenType.NOT_EQUAL) {
            return TackyBinaryOP.NOT_EQUAL
        } else {
            throw TackyException(tokenType.toString())
        }
    }

    override fun visit(node: SimpleProgram): TackyConstruct {
        // Reset counter for test assertions
        tempCounter = 0
        labelCounter = 0
        val tackyFunction = node.functionDefinition.accept(this) as TackyFunction
        return TackyProgram(tackyFunction)
    }

    override fun visit(node: ReturnStatement): TackyConstruct {
        val value = node.expression.accept(this) as TackyVal
        val instr = TackyRet(value)
        currentInstructions += instr
        return instr
    }

    override fun visit(node: ExpressionStatement): TackyConstruct {
        val result = node.expression.accept(this) as TackyVal
        return result
    }

    override fun visit(node: NullStatement): TackyConstruct? = null

    override fun visit(node: BreakStatement): TackyConstruct? {
        val breakLabel = TackyLabel("break_${node.label}")
        currentInstructions += TackyJump(breakLabel)
        return null
    }

    override fun visit(node: ContinueStatement): TackyConstruct? {
        val continueLabel = TackyLabel("continue_${node.label}")
        currentInstructions += TackyJump(continueLabel)
        return null
    }

    override fun visit(node: WhileStatement): TackyConstruct? {
        val continueLabel = TackyLabel("continue_${node.label}")
        val breakLabel = TackyLabel("break_${node.label}")
        currentInstructions += continueLabel
        val condition = node.condition.accept(this) as TackyVal
        currentInstructions += JumpIfZero(condition, breakLabel)
        node.body.accept(this)
        currentInstructions += TackyJump(continueLabel)
        currentInstructions += breakLabel
        return null
    }

    override fun visit(node: DoWhileStatement): TackyConstruct? {
        val startLabel = TackyLabel("start_${node.label}")
        val continueLabel = TackyLabel("continue_${node.label}")
        val breakLabel = TackyLabel("break_${node.label}")
        currentInstructions += startLabel
        node.body.accept(this)
        currentInstructions += continueLabel
        val condition = node.condition.accept(this) as TackyVal
        currentInstructions += JumpIfNotZero(condition, startLabel)
        currentInstructions += breakLabel
        return null
    }

    override fun visit(node: ForStatement): TackyConstruct? {
        val startLabel = TackyLabel("start_${node.label}")
        val continueLabel = TackyLabel("continue_${node.label}")
        val breakLabel = TackyLabel("break_${node.label}")
        node.init.accept(this)
        currentInstructions += startLabel
        if (node.condition != null) {
            val condition = node.condition.accept(this) as TackyVal
            currentInstructions += JumpIfZero(condition, breakLabel)
        }
        node.body.accept(this)
        currentInstructions += continueLabel
        node.post?.accept(this)
        currentInstructions += TackyJump(startLabel)
        currentInstructions += breakLabel
        return null
    }

    override fun visit(node: InitDeclaration): TackyConstruct? {
        node.declaration.accept(this)
        return null
    }

    override fun visit(node: InitExpression): TackyConstruct? {
        node.expression?.accept(this)
        return null
    }

    override fun visit(node: Function): TackyConstruct {
        val functionName = node.name

        currentInstructions.clear()
        node.body.accept(this)
        // Return 0 to guarantee successful termination
        currentInstructions.add(TackyRet(TackyConstant(0)))
        val instructionList = currentInstructions.toList()

        return TackyFunction(functionName, instructionList)
    }

    override fun visit(node: VariableExpression): TackyConstruct = TackyVar(node.name)

    override fun visit(node: UnaryExpression): TackyConstruct {
        val src = node.expression.accept(this) as TackyVal
        val dst = newTemporary()
        val op = convertUnaryOp(node.operator.type)
        currentInstructions += TackyUnary(op, src, dst)
        return dst
    }

    override fun visit(node: BinaryExpression): TackyConstruct {
        when (node.operator.type) {
            TokenType.AND -> {
                val falseLabel = newLabel("and_false")
                val endLabel = newLabel("and_end")
                val resultVar = newTemporary()

                val left = node.left.accept(this) as TackyVal
                currentInstructions += JumpIfZero(left, falseLabel)
                val right = node.right.accept(this) as TackyVal
                currentInstructions += JumpIfZero(right, falseLabel)
                currentInstructions += TackyCopy(TackyConstant(1), resultVar)
                currentInstructions += TackyJump(endLabel)
                currentInstructions += falseLabel
                currentInstructions += TackyCopy(TackyConstant(0), resultVar)
                currentInstructions += endLabel

                return resultVar
            }
            TokenType.OR -> {
                val trueLabel = newLabel("or_true")
                val endLabel = newLabel("or_end")
                val resultVar = newTemporary()

                val left = node.left.accept(this) as TackyVal
                currentInstructions += JumpIfNotZero(left, trueLabel)
                val right = node.right.accept(this) as TackyVal
                currentInstructions += JumpIfNotZero(right, trueLabel)
                currentInstructions += TackyCopy(TackyConstant(0), resultVar)
                currentInstructions += TackyJump(endLabel)
                currentInstructions += trueLabel
                currentInstructions += TackyCopy(TackyConstant(1), resultVar)
                currentInstructions += endLabel

                return resultVar
            }
            else -> {
                val src1 = node.left.accept(this) as TackyVal
                val src2 = node.right.accept(this) as TackyVal
                val op = convertBinaryOp(node.operator.type)

                val dst = newTemporary()
                currentInstructions += TackyBinary(operator = op, src1 = src1, src2 = src2, dest = dst)
                return dst
            }
        }
    }

    override fun visit(node: IntExpression): TackyConstruct = TackyConstant(node.value)

    override fun visit(node: IfStatement): TackyConstruct? {
        val endLabel = newLabel("end")

        val condition = node.condition.accept(this) as TackyVal
        if (node._else == null) {
            currentInstructions += JumpIfZero(condition, endLabel)
            node.then.accept(this)
            currentInstructions += endLabel
        } else {
            val elseLabel = newLabel("else_label")
            currentInstructions += JumpIfZero(condition, elseLabel)
            node.then.accept(this)
            currentInstructions += TackyJump(endLabel)
            currentInstructions += elseLabel
            node._else.accept(this)
            currentInstructions += endLabel
        }
        return null
    }

    override fun visit(node: ConditionalExpression): TackyConstruct? {
        val resultVar = newTemporary()

        val elseLabel = newLabel("cond_else")
        val endLabel = newLabel("cond_end")

        val conditionResult = node.codition.accept(this) as TackyVal
        currentInstructions += JumpIfZero(conditionResult, elseLabel)

        val thenResult = node.thenExpression.accept(this) as TackyVal
        currentInstructions += TackyCopy(thenResult, resultVar)
        currentInstructions += TackyJump(endLabel)
        currentInstructions += elseLabel
        val elseResult = node.elseExpression.accept(this) as TackyVal
        currentInstructions += TackyCopy(elseResult, resultVar)
        currentInstructions += endLabel

        return resultVar
    }

    override fun visit(node: GotoStatement): TackyConstruct? {
        currentInstructions += TackyJump(TackyLabel(node.label))
        return null
    }

    override fun visit(node: LabeledStatement): TackyConstruct? {
        // val label = newLabel(node.label)
        currentInstructions += TackyLabel(node.label)
        node.statement.accept(this)
        return null
    }

    override fun visit(node: AssignmentExpression): TackyConstruct {
        val rvalue = node.rvalue.accept(this) as TackyVal
        val dest = TackyVar(node.lvalue.name)
        currentInstructions += TackyCopy(rvalue, dest)
        return dest
    }

    override fun visit(node: Declaration): TackyConstruct? {
        if (node.init != null) {
            val initVal = node.init.accept(this) as TackyVal
            currentInstructions += TackyCopy(initVal, TackyVar(node.name))
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
        node.block.forEach { it.accept(this) }
        return null
    }

    override fun visit(node: CompoundStatement): TackyConstruct? {
        node.block.accept(this)
        return null
    }
}
