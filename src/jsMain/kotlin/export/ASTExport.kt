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

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class NodeType {
    Program,
    Statement,
    Function,
    Expression,
    ASTNode,
    Block,
    Constant
}

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
                    "type" to JsonPrimitive(NodeType.Program.name),
                    "label" to JsonPrimitive("Program"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(false)
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
                    "type" to JsonPrimitive(NodeType.Statement.name),
                    "label" to JsonPrimitive("ReturnStatement"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(false)
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
                    "type" to JsonPrimitive(NodeType.Statement.name),
                    "label" to JsonPrimitive("ExpressionStatement"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(false)
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: NullStatement): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Statement.name),
                    "label" to JsonPrimitive("NullStatement")
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: BreakStatement): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Statement.name),
                    "label" to JsonPrimitive("BreakStatement")
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: ContinueStatement): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Statement.name),
                    "label" to JsonPrimitive("continue")
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: WhileStatement): String {
        val children =
            JsonObject(
                mapOf(
                    "cond" to JsonPrimitive(node.condition.accept(this)),
                    "body" to JsonPrimitive(node.body.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Statement.name),
                    "label" to JsonPrimitive("WhileLoop"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(true)
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: DoWhileStatement): String {
        val children =
            JsonObject(
                mapOf(
                    "body" to JsonPrimitive(node.body.accept(this)),
                    "cond" to JsonPrimitive(node.condition.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Statement.name),
                    "label" to JsonPrimitive("DoWhileLoop"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(true)
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
            childrenMap["cond"] = JsonPrimitive(node.condition.accept(this))
        }
        if (node.post != null) {
            childrenMap["post"] = JsonPrimitive(node.post.accept(this))
        }
        childrenMap["body"] = JsonPrimitive(node.body.accept(this))
        val children = JsonObject(childrenMap)

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Statement.name),
                    "label" to JsonPrimitive("ForLoop"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(true)
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

        val edgeLabels =
            JsonObject(
                mapOf(
                    "declaration" to JsonPrimitive("init declaration")
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.ASTNode.name),
                    "label" to JsonPrimitive("Declaration"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(false)
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
                    "type" to JsonPrimitive(NodeType.Expression.name),
                    "label" to JsonPrimitive("Expression"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(false)
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: Function): String {
        val children =
            JsonObject(
                mapOf(
                    "name" to JsonPrimitive(node.name),
                    "body" to JsonPrimitive(node.body.accept(this))
                )
            )

        JsonObject(
            mapOf(
                "body" to JsonPrimitive("body")
            )
        )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Function.name),
                    "label" to JsonPrimitive("Function(${node.name})"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(true)
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: VariableExpression): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Expression.name),
                    "label" to JsonPrimitive("Variable(${node.name})")
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
                    "type" to JsonPrimitive(NodeType.Expression.name),
                    "label" to JsonPrimitive("UnaryExpression(${node.operator.type})"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(false)
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: BinaryExpression): String {
        val children =
            JsonObject(
                mapOf(
                    "left" to JsonPrimitive(node.left.accept(this)),
                    "right" to JsonPrimitive(node.right.accept(this))
                )
            )

        JsonObject(
            mapOf(
                "left" to JsonPrimitive("left"),
                "right" to JsonPrimitive("right")
            )
        )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Expression.name),
                    "label" to JsonPrimitive("BinaryExpression(${node.operator.type})"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(true)
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: IntExpression): String {
        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Expression.name),
                    "label" to JsonPrimitive("Int(${node.value})")
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: IfStatement): String {
        val childrenMap =
            mutableMapOf(
                "cond" to JsonPrimitive(node.condition.accept(this)),
                "then" to JsonPrimitive(node.then.accept(this))
            )
        // Handle the optional 'else' branch
        node._else?.let {
            childrenMap["else"] = JsonPrimitive(it.accept(this))
        }

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Statement.name),
                    "label" to JsonPrimitive("IfStatement"),
                    "children" to JsonObject(childrenMap),
                    "edgeLabels" to JsonPrimitive(true)
                )
            )
        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: ConditionalExpression): String {
        val children =
            JsonObject(
                mapOf(
                    "cond" to JsonPrimitive(node.codition.accept(this)),
                    "then" to JsonPrimitive(node.thenExpression.accept(this)),
                    "else" to JsonPrimitive(node.elseExpression.accept(this))
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Expression.name),
                    "label" to JsonPrimitive("ConditionalExpression"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(true)
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
                    "type" to JsonPrimitive(NodeType.Statement.name),
                    "label" to JsonPrimitive("Goto(${node.label})"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(true)
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
                    "type" to JsonPrimitive(NodeType.Statement.name),
                    "label" to JsonPrimitive("LabeledStatement(${node.label})"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(true)
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

        JsonObject(
            mapOf(
                "lvalue" to JsonPrimitive("target"),
                "rvalue" to JsonPrimitive("value")
            )
        )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Expression.name),
                    "label" to JsonPrimitive("Assignment"),
                    "children" to children,
                    "edgeLabels" to JsonPrimitive(true)
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
                    "type" to JsonPrimitive(NodeType.ASTNode.name),
                    "label" to JsonPrimitive("Declaration"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: S): String = node.statement.accept(this)

    override fun visit(node: D): String = node.declaration.accept(this)

    override fun visit(node: Block): String {
        val blockItems = node.block.map { it.accept(this) }
        val children =
            JsonObject(
                mapOf(
                    "block" to JsonArray(blockItems.map { JsonPrimitive(it) })
                )
            )

        val jsonNode =
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(NodeType.Block.name),
                    "label" to JsonPrimitive("Block"),
                    "children" to children
                )
            )

        return Json.encodeToString(jsonNode)
    }

    override fun visit(node: CompoundStatement): String = node.block.accept(this)
}
