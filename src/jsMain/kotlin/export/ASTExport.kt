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

fun createJsonNode(
    type: String,
    label: String,
    children: JsonObject,
    edgeLabels: Boolean = false,
    location: parser.SourceLocation? = null,
    id: String? = null
): String {
    val nodeMap = mutableMapOf<String, kotlinx.serialization.json.JsonElement>(
        "type" to JsonPrimitive(type),
        "label" to JsonPrimitive(label),
        "children" to children,
        "edgeLabels" to JsonPrimitive(edgeLabels)
    )

    location?.let {
        nodeMap["location"] = JsonObject(
            mapOf(
                "startLine" to JsonPrimitive(it.startLine),
                "startCol" to JsonPrimitive(it.startCol),
                "endLine" to JsonPrimitive(it.endLine),
                "endCol" to JsonPrimitive(it.endCol)
            )
        )
    }

    id?.let {
        nodeMap["id"] = JsonPrimitive(it)
    }

    val jsonNode = JsonObject(nodeMap)
    return Json.encodeToString(jsonNode)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class NodeType {
    Program,
    Statement,
    Function,
    Expression,
    ASTNode,
    Block,
    Declaration
}

class ASTExport : Visitor<String> {
    override fun visit(node: SimpleProgram): String {
        val decls = JsonArray(node.functionDeclaration.map { JsonPrimitive(it.accept(this)) })
        return createJsonNode(NodeType.Program.name, "Program", JsonObject(mapOf("declarations" to decls)), location = node.location, id = node.id)
    }

    override fun visit(node: ReturnStatement): String {
        val children = JsonObject(mapOf("expression" to JsonPrimitive(node.expression.accept(this))))
        return createJsonNode(NodeType.Statement.name, "ReturnStatement", children, location = node.location, id = node.id)
    }

    override fun visit(node: ExpressionStatement): String {
        val children = JsonObject(mapOf("expression" to JsonPrimitive(node.expression.accept(this))))
        return createJsonNode(NodeType.Statement.name, "ExpressionStatement", children, location = node.location, id = node.id)
    }

    override fun visit(node: NullStatement): String = createJsonNode(NodeType.Statement.name, "NullStatement", JsonObject(emptyMap()), location = node.location, id = node.id)

    override fun visit(node: BreakStatement): String = createJsonNode(NodeType.Statement.name, "BreakStatement", JsonObject(emptyMap()), location = node.location, id = node.id)

    override fun visit(node: ContinueStatement): String = createJsonNode(NodeType.Statement.name, "continue", JsonObject(emptyMap()), location = node.location, id = node.id)

    override fun visit(node: WhileStatement): String {
        val children =
            JsonObject(
                mapOf(
                    "cond" to JsonPrimitive(node.condition.accept(this)),
                    "body" to JsonPrimitive(node.body.accept(this))
                )
            )
        return createJsonNode(NodeType.Statement.name, "WhileLoop", children, edgeLabels = true, location = node.location, id = node.id)
    }

    override fun visit(node: DoWhileStatement): String {
        val children =
            JsonObject(
                mapOf(
                    "body" to JsonPrimitive(node.body.accept(this)),
                    "cond" to JsonPrimitive(node.condition.accept(this))
                )
            )
        return createJsonNode(NodeType.Statement.name, "DoWhileLoop", children, edgeLabels = true, location = node.location, id = node.id)
    }

    override fun visit(node: ForStatement): String {
        val childrenMap =
            mutableMapOf<String, kotlinx.serialization.json.JsonElement>(
                "init" to JsonPrimitive(node.init.accept(this))
            )
        node.condition?.let { childrenMap["cond"] = JsonPrimitive(it.accept(this)) }
        node.post?.let { childrenMap["post"] = JsonPrimitive(it.accept(this)) }
        childrenMap["body"] = JsonPrimitive(node.body.accept(this))

        return createJsonNode(NodeType.Statement.name, "ForLoop", JsonObject(childrenMap), edgeLabels = true, location = node.location, id = node.id)
    }

    override fun visit(node: InitDeclaration): String {
        val children = JsonObject(mapOf("declaration" to JsonPrimitive(node.varDeclaration.accept(this))))
        return createJsonNode(NodeType.ASTNode.name, "Declaration", children, location = node.location, id = node.id)
    }

    override fun visit(node: InitExpression): String {
        val childrenMap = mutableMapOf<String, kotlinx.serialization.json.JsonElement>()
        node.expression?.let { childrenMap["expression"] = JsonPrimitive(it.accept(this)) }
        return createJsonNode(NodeType.Expression.name, "Expression", JsonObject(childrenMap), location = node.location, id = node.id)
    }

    override fun visit(node: FunctionDeclaration): String {
        val childrenMap = mutableMapOf("name" to JsonPrimitive(node.name))
        node.body?.let { childrenMap["body"] = JsonPrimitive(it.accept(this)) }
        return createJsonNode(NodeType.Function.name, "Function(${node.name})", JsonObject(childrenMap), edgeLabels = true, location = node.location, id = node.id)
    }

    override fun visit(node: VarDecl): String {
        val children = JsonObject(mapOf("variableDeclaration" to JsonPrimitive(node.varDecl.accept(this))))
        return createJsonNode(NodeType.Declaration.name, "VarDeclaration", children, location = node.location, id = node.id)
    }

    override fun visit(node: FunDecl): String {
        val children = JsonObject(mapOf("functionDeclaration" to JsonPrimitive(node.funDecl.accept(this))))
        return createJsonNode(NodeType.Declaration.name, "FuncDeclaration", children, location = node.location, id = node.id)
    }

    override fun visit(node: VariableExpression): String =
        createJsonNode(NodeType.Expression.name, "Variable(${node.name})", JsonObject(emptyMap()), location = node.location, id = node.id)

    override fun visit(node: UnaryExpression): String {
        val children =
            JsonObject(
                mapOf(
                    "operator" to JsonPrimitive(node.operator.toString()),
                    "expression" to JsonPrimitive(node.expression.accept(this))
                )
            )
        return createJsonNode(NodeType.Expression.name, "UnaryExpression(${node.operator.type})", children, location = node.location, id = node.id)
    }

    override fun visit(node: BinaryExpression): String {
        val children =
            JsonObject(
                mapOf(
                    "left" to JsonPrimitive(node.left.accept(this)),
                    "right" to JsonPrimitive(node.right.accept(this))
                )
            )
        return createJsonNode(NodeType.Expression.name, "BinaryExpression(${node.operator.type})", children, edgeLabels = true, location = node.location, id = node.id)
    }

    override fun visit(node: IntExpression): String = createJsonNode(NodeType.Expression.name, "Int(${node.value})", JsonObject(emptyMap()), location = node.location, id = node.id)

    override fun visit(node: IfStatement): String {
        val childrenMap =
            mutableMapOf(
                "cond" to JsonPrimitive(node.condition.accept(this)),
                "then" to JsonPrimitive(node.then.accept(this))
            )
        node._else?.let { childrenMap["else"] = JsonPrimitive(it.accept(this)) }
        return createJsonNode(NodeType.Statement.name, "IfStatement", JsonObject(childrenMap), edgeLabels = true, location = node.location, id = node.id)
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
        return createJsonNode(NodeType.Expression.name, "ConditionalExpression", children, edgeLabels = true, location = node.location, id = node.id)
    }

    override fun visit(node: GotoStatement): String {
        val children = JsonObject(mapOf("targetLabel" to JsonPrimitive(node.label)))
        return createJsonNode(NodeType.Statement.name, "Goto(${node.label})", children, edgeLabels = true, location = node.location, id = node.id)
    }

    override fun visit(node: LabeledStatement): String {
        val children =
            JsonObject(
                mapOf(
                    "label" to JsonPrimitive(node.label),
                    "statement" to JsonPrimitive(node.statement.accept(this))
                )
            )
        return createJsonNode(NodeType.Statement.name, "LabeledStatement(${node.label})", children, edgeLabels = true, location = node.location, id = node.id)
    }

    override fun visit(node: AssignmentExpression): String {
        val children =
            JsonObject(
                mapOf(
                    "lvalue" to JsonPrimitive(node.lvalue.accept(this)),
                    "rvalue" to JsonPrimitive(node.rvalue.accept(this))
                )
            )
        return createJsonNode(NodeType.Expression.name, "Assignment", children, edgeLabels = true, location = node.location, id = node.id)
    }

    override fun visit(node: VariableDeclaration): String {
        val childrenMap = mutableMapOf("name" to JsonPrimitive(node.name))
        node.init?.let { childrenMap["initializer"] = JsonPrimitive(it.accept(this)) }
        return createJsonNode(NodeType.Declaration.name, "Declaration(${node.name})", JsonObject(childrenMap), location = node.location, id = node.id)
    }

    override fun visit(node: S): String = node.statement.accept(this)

    override fun visit(node: D): String = when (node.declaration) {
        is VarDecl -> node.declaration.accept(this)
        is FunDecl -> node.declaration.accept(this)
        is VariableDeclaration -> node.declaration.accept(this)
    }

    override fun visit(node: Block): String {
        val blockItems = node.items.map { it.accept(this) }
        val children = JsonObject(mapOf("block" to JsonArray(blockItems.map { JsonPrimitive(it) })))
        return createJsonNode(NodeType.Block.name, "Block", children, location = node.location, id = node.id)
    }

    override fun visit(node: CompoundStatement): String = node.block.accept(this)

    override fun visit(node: FunctionCall): String {
        val children =
            JsonObject(
                mapOf(
                    "name" to JsonPrimitive(node.name),
                    "arguments" to JsonArray(node.arguments.map { JsonPrimitive(it.accept(this)) })
                )
            )
        return createJsonNode(NodeType.Function.name, "FuncCall(${node.name})", children, location = node.location, id = node.id)
    }
}
