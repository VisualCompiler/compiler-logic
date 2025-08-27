package export

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import parser.AssignmentExpression
import parser.BinaryExpression
import parser.D
import parser.Declaration
import parser.ExpressionStatement
import parser.Function
import parser.IntExpression
import parser.NullStatement
import parser.ReturnStatement
import parser.S
import parser.SimpleProgram
import parser.UnaryExpression
import parser.VariableExpression
import parser.Visitor

class ASTExport : Visitor<String> {
    override fun visit(node: SimpleProgram): String {
        val children =
            JsonObject(
                mapOf(
                    "functionDefinition" to JsonPrimitive(node.functionDefinition.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("function definition"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: ReturnStatement): String {
        val children =
            JsonObject(
                mapOf(
                    "expression" to JsonPrimitive(node.expression.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("return expression"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: ExpressionStatement): String {
        val children =
            JsonObject(
                mapOf(
                    "expression" to JsonPrimitive(node.expression.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("expression statement"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: NullStatement): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("null statement")
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: Function): String {
        val children =
            JsonObject(
                mapOf(
                    "name" to JsonPrimitive(node.name),
                    "body" to JsonArray(node.body.map { Json.decodeFromString(it.accept(this)) })
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("'main', body'"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: VariableExpression): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive(node.name)
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: UnaryExpression): String {
        val children =
            JsonObject(
                mapOf(
                    "operator" to JsonPrimitive(node.operator.toString()),
                    "expression" to JsonPrimitive(node.expression.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("operator, expression"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: BinaryExpression): String {
        val children =
            JsonObject(
                mapOf(
                    "left" to JsonPrimitive(node.left.accept(this)),
                    "operator" to JsonPrimitive(node.operator.toString()),
                    "right" to JsonPrimitive(node.right.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("operator, left, right"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: IntExpression): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive(node.value)
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: AssignmentExpression): String {
        val children =
            JsonObject(
                mapOf(
                    "lvalue" to JsonPrimitive(node.lvalue.accept(this)),
                    "rvalue" to JsonPrimitive(node.rvalue.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("lvalue, rvalue"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: Declaration): String {
        val childrenMap =
            mutableMapOf<String, kotlinx.serialization.json.JsonElement>(
                "name" to JsonPrimitive(node.name)
            )
        if (node.init != null) {
            childrenMap["init"] = JsonPrimitive(node.init.accept(this))
        }
        val children = JsonObject(childrenMap)

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("declaration"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: S): String {
        val children =
            JsonObject(
                mapOf(
                    "statement" to JsonPrimitive(node.statement.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("statement"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: D): String {
        val children =
            JsonObject(
                mapOf(
                    "declaration" to JsonPrimitive(node.declaration.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("declaration"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }
}
// <TackyProgram(function=TackyFunction(name=main, body=[TackyBinary(operator=EQUAL, src1=TackyConstant(value=1), src2=TackyConstant(value=0), dest=TackyVar(name=tmp.1)), JumpIfNotZero(condition=TackyVar(name=tmp.1), target=TackyLabel(name=.L_or_true_0)), TackyBinary(operator=GREATER, src1=TackyConstant(value=5), src2=TackyConstant(value=2), dest=TackyVar(name=tmp.3)), JumpIfZero(condition=TackyVar(name=tmp.3), target=TackyLabel(name=.L_and_false_2)), TackyBinary(operator=LESS_EQUAL, src1=TackyConstant(value=10), src2=TackyConstant(value=20), dest=TackyVar(name=tmp.4)), JumpIfZero(condition=TackyVar(name=tmp.4), target=TackyLabel(name=.L_and_false_2)), TackyCopy(src=TackyConstant(value=1), dest=TackyVar(name=tmp.2)), TackyJump(target=TackyLabel(name=.L_and_end_3)), TackyLabel(name=.L_and_false_2), TackyCopy(src=TackyConstant(value=0), dest=TackyVar(name=tmp.2)), TackyLabel(name=.L_and_end_3), JumpIfNotZero(condition=TackyVar(name=tmp.2), target=TackyLabel(name=.L_or_true_0)), TackyCopy(src=TackyConstant(value=0), dest=TackyVar(name=tmp.0)), TackyJump(target=TackyLabel(name=.L_or_end_1)), TackyLabel(name=.L_or_true_0), TackyCopy(src=TackyConstant(value=1), dest=TackyVar(name=tmp.0)), TackyLabel(name=.L_or_end_1), TackyRet(value=TackyVar(name=tmp.0))]))>, actual
// <TackyProgram(function=TackyFunction(name=main, body=[TackyBinary(operator=EQUAL, src1=TackyConstant(value=1), src2=TackyConstant(value=0), dest=TackyVar(name=tmp.1)), JumpIfNotZero(condition=TackyVar(name=tmp.1), target=TackyLabel(name=.L_or_true_0)), TackyBinary(operator=GREATER, src1=TackyConstant(value=5), src2=TackyConstant(value=2), dest=TackyVar(name=tmp.3)), JumpIfZero(condition=TackyVar(name=tmp.3), target=TackyLabel(name=.L_and_false_2)), TackyBinary(operator=LESS_EQUAL, src1=TackyConstant(value=10), src2=TackyConstant(value=20), dest=TackyVar(name=tmp.4)), JumpIfZero(condition=TackyVar(name=tmp.4), target=TackyLabel(name=.L_and_false_2)), TackyCopy(src=TackyConstant(value=1), dest=TackyVar(name=tmp.2)), TackyJump(target=TackyLabel(name=.L_and_end_3)), TackyLabel(name=.L_and_false_2), TackyCopy(src=TackyConstant(value=0), dest=TackyVar(name=tmp.2)), TackyLabel(name=.L_and_end_3), JumpIfNotZero(condition=TackyVar(name=tmp.2), target=TackyLabel(name=.L_or_true_0)), TackyCopy(src=TackyConstant(value=0), dest=TackyVar(name=tmp.0)), TackyJump(target=TackyLabel(name=.L_or_end_1)), TackyLabel(name=.L_or_true_0), TackyCopy(src=TackyConstant(value=1), dest=TackyVar(name=tmp.0)), TackyLabel(name=.L_or_end_1), TackyRet(value=TackyVar(name=tmp.0)), TackyRet(value=TackyConstant(value=0))]))>.
