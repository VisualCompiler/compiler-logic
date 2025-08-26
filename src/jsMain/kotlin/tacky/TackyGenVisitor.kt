package tacky

import exceptions.TackyException
import lexer.TokenType
import parser.BinaryExpression
import parser.IntExpression
import parser.ReturnStatement
import parser.SimpleFunction
import parser.SimpleProgram
import parser.UnaryExpression
import parser.Visitor

class TackyGenVisitor : Visitor<Any> {
    private var tempCounter = 0
    private var labelCounter = 0

    private fun newTemporary(): TackyVar = TackyVar("tmp.${tempCounter++}")

    private fun newLabel(base: String): TackyLabel = TackyLabel(".L_${base}_${labelCounter++}")

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

    override fun visit(node: SimpleProgram): Any {
        val tackyFunction = node.functionDefinition.accept(this) as TackyFunction
        // Wrap the TackyFunction in a TackyProgram and return it. This is the final result.
        return TackyProgram(tackyFunction)
    }

    override fun visit(node: ReturnStatement): Any {
        val expressionResult = node.expression.accept(this) as TackyResult
        val returnInstruction = TackyRet(expressionResult.resultVal!!)
        val allInstruction = expressionResult.instructions + returnInstruction
        return TackyResult(allInstruction, null)
    }

    override fun visit(node: SimpleFunction): Any {
        val functionName = node.name

        val bodyResult = node.body.accept(this) as TackyResult

        val instructionList = bodyResult.instructions

        return TackyFunction(functionName, instructionList)
    }

    override fun visit(node: UnaryExpression): Any {
        val innerExp = node.expression.accept(this) as TackyResult
        val src = innerExp.resultVal!!

        val dst = newTemporary()
        val tackyUnOp = convertUnaryOp(node.operator.type)
        val unaryInstruction = TackyUnary(operator = tackyUnOp, src = src, dest = dst)
        val allInstruction = innerExp.instructions + unaryInstruction
        return TackyResult(allInstruction, dst)
    }

    override fun visit(node: BinaryExpression): Any {
        when (node.operator.type) {
            TokenType.AND -> {
                val falseLabel = newLabel("and_false")
                val endLabel = newLabel("and_end")
                val resultVar = newTemporary()

                val left = node.left.accept(this) as TackyResult
                val right = node.right.accept((this))as TackyResult

                val instructions = mutableListOf<TackyInstruction>()
                instructions.addAll(left.instructions)
                instructions.add(JumpIfZero(left.resultVal!!, falseLabel))
                instructions.addAll(right.instructions)
                instructions.add(JumpIfZero(right.resultVal!!, falseLabel))
                instructions.add(TackyCopy(TackyConstant(1), resultVar))
                instructions.add(TackyJump(endLabel))
                instructions.add(falseLabel)
                instructions.add(TackyCopy(TackyConstant(0), resultVar))
                instructions.add(endLabel)

                return TackyResult(instructions, resultVar)
            }
            TokenType.OR -> {
                val trueLabel = newLabel("or_true")
                val endLabel = newLabel("or_end")
                val resultVar = newTemporary()

                val left = node.left.accept(this) as TackyResult
                val right = node.right.accept(this) as TackyResult

                val instructions = mutableListOf<TackyInstruction>()
                instructions.addAll(left.instructions)
                instructions.add(JumpIfNotZero(left.resultVal!!, trueLabel))
                instructions.addAll(right.instructions)
                instructions.add(JumpIfNotZero(right.resultVal!!, trueLabel))
                instructions.add(TackyCopy(TackyConstant(0), resultVar))
                instructions.add(TackyJump(endLabel))
                instructions.add(trueLabel)
                instructions.add(TackyCopy(TackyConstant(1), resultVar))
                instructions.add(endLabel)

                return TackyResult(instructions, resultVar)
            }
            else -> {
                val leftExp = node.left.accept(this) as TackyResult
                val src1 = leftExp.resultVal!!
                val rightExp = node.right.accept(this) as TackyResult
                val src2 = rightExp.resultVal!!
                val op = convertBinaryOp(node.operator.type)

                val dst = newTemporary()
                val binaryInstruction = TackyBinary(operator = op, src1 = src1, src2 = src2, dest = dst)
                val allInstruction = leftExp.instructions + rightExp.instructions + binaryInstruction
                return TackyResult(allInstruction, dst)
            }
        }
    }

    override fun visit(node: IntExpression): Any =
        TackyResult(
            instructions = emptyList(),
            resultVal = TackyConstant(node.value)
        )
}
