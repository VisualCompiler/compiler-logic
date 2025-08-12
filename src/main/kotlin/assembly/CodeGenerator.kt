package assembly

import org.example.parser.BinaryExpression
import org.example.parser.Expression
import org.example.parser.FunctionDefinition
import org.example.parser.IntExpression
import org.example.parser.Program
import org.example.parser.ReturnStatement
import org.example.parser.SimpleFunction
import org.example.parser.SimpleProgram
import org.example.parser.Statement
import org.example.parser.UnaryExpression

class CodeGenerator {
    fun generateAsm(program: Program): SimpleAsmProgram {
        when (program) {
            is SimpleProgram -> {
                val asmFunction = generateFunction(program.functionDefinition)
                return SimpleAsmProgram(
                    function = asmFunction,
                    line = program.line,
                    column = program.column
                )
            }
        }
    }

    private fun generateFunction(func: FunctionDefinition): AsmFunction {
        when (func) {
            is SimpleFunction -> {
                val bodyInstructions = generateInstructions(func.body)
                return AsmFunction(
                    name = func.name.value,
                    body = bodyInstructions,
                    line = func.line,
                    column = func.column
                )
            }
        }
    }

    private fun generateInstructions(body: Statement): List<Instruction> {
        when (body) {
            is ReturnStatement -> return generateReturn(body.expression)
        }
    }

    private fun generateReturn(expression: Expression): List<Instruction> {
        when (expression) {
            is IntExpression -> {
                val imm =
                    Imm(
                        value = expression.value,
                        line = expression.line,
                        column = expression.column
                    )
                val retReg =
                    Register(
                        name = "EAX",
                        line = expression.line,
                        column = expression.column
                    )
                return listOf(
                    Mov(
                        src = imm,
                        dest = retReg,
                        line = expression.line,
                        column = expression.column
                    ),
                    Ret(
                        line = expression.line,
                        column = expression.column
                    )
                )
            }
            is BinaryExpression -> TODO()
            is UnaryExpression -> TODO()
        }
    }
}
