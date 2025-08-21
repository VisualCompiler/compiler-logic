package tacky

import lexer.TokenType
import parser.BinaryExpression
import parser.Identifier
import parser.IntExpression
import parser.ReturnStatement
import parser.SimpleFunction
import parser.SimpleProgram
import parser.UnaryExpression
import parser.Visitor

class TackyGenVisitor : Visitor<Any> {
    private var tempCounter = 0

    private fun newTemporary(): TackyVar = TackyVar("tmp.${tempCounter++}")

    private fun convertUnaryOp(tokenType: TokenType): TackyUnaryOP {
        if (tokenType == TokenType.TILDE) {
            return TackyUnaryOP.COMPLEMENT
        } else if (tokenType == TokenType.NEGATION) {
            return TackyUnaryOP.NEGATE
        } else {
            throw IllegalArgumentException("Not a valid unary operator: $tokenType")
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
        } else {
            throw IllegalArgumentException("Not a valid Binary operator: $tokenType")
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
        val functionName = node.name.value

        val bodyResult = node.body.accept(this) as TackyResult

        val instructionList = bodyResult.instructions

        return TackyFunction(functionName, instructionList)
    }

    override fun visit(node: Identifier): TackyResult = throw NotImplementedError("Identifiers not yet supported in TACKY generation.")

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

    override fun visit(node: IntExpression): Any =
        TackyResult(
            instructions = emptyList(),
            resultVal = TackyConstant(node.value)
        )
}
