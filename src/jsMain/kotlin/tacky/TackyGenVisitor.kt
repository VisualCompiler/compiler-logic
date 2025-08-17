package org.example.tacky

import lexer.TokenType
import org.example.parser.BinaryExpression
import org.example.parser.Identifier
import org.example.parser.IntExpression
import org.example.parser.ReturnStatement
import org.example.parser.SimpleFunction
import org.example.parser.SimpleProgram
import org.example.parser.UnaryExpression
import org.example.parser.Visitor

class TackyGenVisitor : Visitor<TackyResult> {
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

    override fun visit(node: SimpleProgram): TackyResult = node.functionDefinition.accept(this)

    override fun visit(node: ReturnStatement): TackyResult {
        val expressionResult = node.expression.accept(this)
        val returnInstruction = TackyRet(expressionResult.resultVal!!)
        val allInstruction = expressionResult.instructions + returnInstruction
        return TackyResult(allInstruction, null)
    }

    override fun visit(node: SimpleFunction): TackyResult = node.body.accept(this)

    override fun visit(node: Identifier): TackyResult = throw NotImplementedError("Identifiers not yet supported in TACKY generation.")

    override fun visit(node: UnaryExpression): TackyResult {
        val innerExp = node.expression.accept(this)
        val src = innerExp.resultVal!!

        val dst = newTemporary()
        val tackyUnOp = convertUnaryOp(node.operator.type)
        val unaryInstruction = TackyUnary(operator = tackyUnOp, src = src, dest = dst)
        val allInstruction = innerExp.instructions + unaryInstruction
        return TackyResult(allInstruction, dst)
    }

    override fun visit(node: BinaryExpression): TackyResult {
        TODO("Not yet implemented")
    }

    override fun visit(node: IntExpression): TackyResult =
        TackyResult(
            instructions = emptyList(),
            resultVal = TackyConstant(node.value)
        )
}
