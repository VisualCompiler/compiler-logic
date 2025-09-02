package export

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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

class ASTExport : Visitor<String> {
    private fun buildJsonString(
        type: String,
        label: String,
        children: Map<String, JsonPrimitive>
    ): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(type),
                    "label" to JsonPrimitive(label),
                    "children" to JsonObject(children)
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: SimpleProgram): String {
        val decls = JsonArray(node.functionDeclaration.map { Json.decodeFromString(it.accept(this)) })

        val children = JsonObject(mapOf("declarations" to decls))
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("Program"),
                    "label" to JsonPrimitive("Top-level Declarations"),
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
                    "declaration" to JsonPrimitive(node.varDeclaration.accept(this))
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

    override fun visit(node: FunctionDeclaration): String {
        val childrenMap =
            mutableMapOf(
                "name" to JsonPrimitive(node.name),
                "params" to JsonArray(node.params.map { JsonPrimitive(it) })
            )
        // A function declaration might not have a body (prototype)
        node.body?.let {
            childrenMap["body"] = Json.decodeFromString(it.accept(this))
        }

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("FunctionDeclaration"),
                    "label" to JsonPrimitive("name, params, body?"),
                    "children" to JsonObject(childrenMap)
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: VarDecl): String {
        val children = JsonObject(mapOf("variableDeclaration" to Json.decodeFromString(node.varDecl.accept(this))))
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("VarDecl"),
                    "label" to JsonPrimitive("Variable Declaration"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: FunDecl): String {
        val children = JsonObject(mapOf("functionDeclaration" to Json.decodeFromString(node.funDecl.accept(this))))
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("FunDecl"),
                    "label" to JsonPrimitive("Function Declaration"),
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

    override fun visit(node: Declaration): String =
        when (node) {
            is FunDecl -> node.accept(this)
            is VarDecl -> node.accept(this)
        }

    override fun visit(node: VariableDeclaration): String {
        val childrenMap = mutableMapOf("name" to JsonPrimitive(node.name))
        node.init?.let {
            childrenMap["initializer"] = Json.decodeFromString(it.accept(this))
        }
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("VariableDeclaration"),
                    "label" to JsonPrimitive("name, init?"),
                    "children" to JsonObject(childrenMap)
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: S): String {
        val children = JsonObject(mapOf("statement" to Json.decodeFromString(node.statement.accept(this))))
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("S"),
                    "label" to JsonPrimitive("Statement Wrapper"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: D): String {
        val children = JsonObject(mapOf("declaration" to Json.decodeFromString(node.declaration.accept(this))))
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("D"),
                    "label" to JsonPrimitive("Declaration Wrapper"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: Block): String {
        val items = JsonArray(node.block.map { Json.decodeFromString(it.accept(this)) })
        val children = JsonObject(mapOf("items" to items))
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("Block"),
                    "label" to JsonPrimitive("Block Items"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: CompoundStatement): String {
        val children = JsonObject(mapOf("block" to Json.decodeFromString(node.block.accept(this))))
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("CompoundStatement"),
                    "label" to JsonPrimitive("Block"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: FunctionCall): String {
        val children =
            JsonObject(
                mapOf(
                    "name" to JsonPrimitive(node.name),
                    "arguments" to JsonArray(node.arguments.map { Json.decodeFromString(it.accept(this)) })
                )
            )
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("FunctionCall"),
                    "label" to JsonPrimitive("name, args"),
                    "children" to children
                )
            )
        return Json.encodeToString(jsonNode)
    }
}
