package export

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import parser.AssignmentExpression
import parser.BinaryExpression
import parser.BreakStatement
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

    override fun visit(node: BreakStatement): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("break")
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: ContinueStatement): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("continue")
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: WhileStatement): String {
        val children =
            JsonObject(
                mapOf(
                    "condition" to JsonPrimitive(node.condition.accept(this)),
                    "body" to JsonArray(Json.decodeFromString(node.body.accept(this)))
                )
            )
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("while"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: DoWhileStatement): String {
        val children =
            JsonObject(
                mapOf(
                    "body" to JsonArray(Json.decodeFromString(node.body.accept(this))),
                    "condition" to JsonPrimitive(node.condition.accept(this))
                )
            )
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("do while"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: ForStatement): String {
        val childrenMap =
            mutableMapOf<String, kotlinx.serialization.json.JsonElement>(
                "init" to JsonPrimitive(node.init.accept(this))
            )
        if (node.condition != null) {
            childrenMap["condition"] = JsonPrimitive(node.condition.accept(this))
        }
        if (node.post != null) {
            childrenMap["post"] = JsonPrimitive(node.post.accept(this))
        }
        childrenMap["body"] = JsonPrimitive(node.body.accept(this))
        val children = JsonObject(childrenMap)
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("for"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: InitDeclaration): String {
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
                    "label" to JsonPrimitive("init declaration"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: InitExpression): String {
        val childrenMap = mutableMapOf<String, kotlinx.serialization.json.JsonElement>()
        if (node.expression != null) {
            childrenMap["expression"] = JsonPrimitive(node.expression.accept(this))
        }
        val children = JsonObject(childrenMap)
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(this::class.simpleName),
                    "label" to JsonPrimitive("init expression"),
                    "children" to children
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

    override fun visit(node: IfStatement): String {
        val childrenMap =
            mutableMapOf(
                "condition" to JsonPrimitive(node.condition.accept(this)),
                "then" to JsonPrimitive(node.then.accept(this))
            )
        // Handle the optional 'else' branch
        node._else?.let {
            childrenMap["else"] = JsonPrimitive(it.accept(this))
        }

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("IfStatement"),
                    "label" to JsonPrimitive("if-then-else"),
                    "children" to JsonObject(childrenMap)
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: ConditionalExpression): String {
        val children =
            JsonObject(
                mapOf(
                    "condition" to JsonPrimitive(node.codition.accept(this)),
                    "thenExpression" to JsonPrimitive(node.thenExpression.accept(this)),
                    "elseExpression" to JsonPrimitive(node.elseExpression.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("ConditionalExpression"),
                    "label" to JsonPrimitive("cond ? then : else"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: GotoStatement): String {
        val children =
            JsonObject(
                mapOf(
                    "targetLabel" to JsonPrimitive(node.label)
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("GotoStatement"),
                    "label" to JsonPrimitive("goto"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: LabeledStatement): String {
        val children =
            JsonObject(
                mapOf(
                    "label" to JsonPrimitive(node.label),
                    "statement" to JsonPrimitive(node.statement.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("LabeledStatement"),
                    "label" to JsonPrimitive("label: statement"),
                    "children" to children
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
