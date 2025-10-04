package export

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import parser.ASTVisitor
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
import parser.WhileStatement

fun createJsonNode(
    type: String,
    label: String,
    children: JsonObject,
    edgeLabels: Boolean = false,
    location: parser.SourceLocation? = null,
    id: String? = null
): JsonObject {
    val nodeMap =
        mutableMapOf<String, JsonElement>(
            "type" to JsonPrimitive(type),
            "label" to JsonPrimitive(label),
            "children" to children,
            "edgeLabels" to JsonPrimitive(edgeLabels)
        )

    location?.let {
        nodeMap["location"] =
            JsonObject(
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

    return JsonObject(nodeMap)
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

class ASTExport : ASTVisitor<JsonObject> {
    override fun visit(node: SimpleProgram): JsonObject {
        val decls = JsonArray(node.functionDeclaration.map { it.accept(this) })
        return createJsonNode(NodeType.Program.name, "Program", JsonObject(mapOf("declarations" to decls)), false, node.location, node.id)
    }

    override fun visit(node: ReturnStatement): JsonObject {
        val children = JsonObject(mapOf("expression" to node.expression.accept(this)))
        return createJsonNode(NodeType.Statement.name, "ReturnStatement", children, false, node.location, node.id)
    }

    override fun visit(node: ExpressionStatement): JsonObject {
        val children = JsonObject(mapOf("expression" to node.expression.accept(this)))
        return createJsonNode(NodeType.Statement.name, "ExpressionStatement", children, false, node.location, node.id)
    }

    override fun visit(node: NullStatement): JsonObject =
        createJsonNode(NodeType.Statement.name, "NullStatement", JsonObject(emptyMap()), false, node.location, node.id)

    override fun visit(node: BreakStatement): JsonObject =
        createJsonNode(NodeType.Statement.name, "BreakStatement", JsonObject(emptyMap()), false, node.location, node.id)

    override fun visit(node: ContinueStatement): JsonObject =
        createJsonNode(NodeType.Statement.name, "continue", JsonObject(emptyMap()), false, node.location, node.id)

    override fun visit(node: WhileStatement): JsonObject {
        val children =
            JsonObject(
                mapOf(
                    "cond" to node.condition.accept(this),
                    "body" to node.body.accept(this)
                )
            )
        return createJsonNode(NodeType.Statement.name, "WhileLoop", children, true, node.location, node.id)
    }

    override fun visit(node: DoWhileStatement): JsonObject {
        val children =
            JsonObject(
                mapOf(
                    "body" to node.body.accept(this),
                    "cond" to node.condition.accept(this)
                )
            )
        return createJsonNode(NodeType.Statement.name, "DoWhileLoop", children, true, node.location, node.id)
    }

    override fun visit(node: ForStatement): JsonObject {
        val childrenMap =
            mutableMapOf<String, JsonElement>(
                "init" to node.init.accept(this)
            )
        node.condition?.let { childrenMap["cond"] = it.accept(this) }
        node.post?.let { childrenMap["post"] = it.accept(this) }
        childrenMap["body"] = node.body.accept(this)

        return createJsonNode(NodeType.Statement.name, "ForLoop", JsonObject(childrenMap), true, node.location, node.id)
    }

    override fun visit(node: InitDeclaration): JsonObject {
        val children = JsonObject(mapOf("declaration" to node.varDeclaration.accept(this)))
        return createJsonNode(NodeType.ASTNode.name, "Declaration", children, false, node.location, node.id)
    }

    override fun visit(node: InitExpression): JsonObject {
        val childrenMap = mutableMapOf<String, JsonElement>()
        node.expression?.let { childrenMap["expression"] = it.accept(this) }
        return createJsonNode(NodeType.Expression.name, "Expression", JsonObject(childrenMap), false, node.location, node.id)
    }

    override fun visit(node: FunctionDeclaration): JsonObject {
        val childrenMap = mutableMapOf<String, JsonElement>("name" to JsonPrimitive(node.name))
        node.body?.let { childrenMap["body"] = it.accept(this) }
        return createJsonNode(NodeType.Function.name, "Function(${node.name})", JsonObject(childrenMap), true, node.location, node.id)
    }

    override fun visit(node: VarDecl): JsonObject {
        val children = JsonObject(mapOf("variableDeclaration" to node.varDecl.accept(this)))
        return createJsonNode(NodeType.Declaration.name, "VarDeclaration", children, false, node.location, node.id)
    }

    override fun visit(node: FunDecl): JsonObject {
        val children = JsonObject(mapOf("functionDeclaration" to node.funDecl.accept(this)))
        return createJsonNode(NodeType.Declaration.name, "FuncDeclaration", children, false, node.location, node.id)
    }

    override fun visit(node: VariableExpression): JsonObject =
        createJsonNode(NodeType.Expression.name, "Variable(${node.name})", JsonObject(emptyMap()), false, node.location, node.id)

    override fun visit(node: UnaryExpression): JsonObject {
        val children =
            JsonObject(
                mapOf(
                    "operator" to JsonPrimitive(node.operator.toString()),
                    "expression" to node.expression.accept(this)
                )
            )
        return createJsonNode(
            NodeType.Expression.name,
            "UnaryExpression(${node.operator.type})",
            children,
            location = node.location,
            id = node.id
        )
    }

    override fun visit(node: BinaryExpression): JsonObject {
        val children =
            JsonObject(
                mapOf(
                    "left" to node.left.accept(this),
                    "right" to node.right.accept(this)
                )
            )
        return createJsonNode(
            NodeType.Expression.name,
            "BinaryExpression(${node.operator.type})",
            children,
            edgeLabels = true,
            location = node.location,
            id = node.id
        )
    }

    override fun visit(node: IntExpression): JsonObject =
        createJsonNode(NodeType.Expression.name, "Int(${node.value})", JsonObject(emptyMap()), false, node.location, node.id)

    override fun visit(node: IfStatement): JsonObject {
        val childrenMap =
            mutableMapOf(
                "cond" to node.condition.accept(this),
                "then" to node.then.accept(this)
            )
        node._else?.let { childrenMap["else"] = it.accept(this) }
        return createJsonNode(NodeType.Statement.name, "IfStatement", JsonObject(childrenMap), true, node.location, node.id)
    }

    override fun visit(node: ConditionalExpression): JsonObject {
        val children =
            JsonObject(
                mapOf(
                    "cond" to node.condition.accept(this),
                    "then" to node.thenExpression.accept(this),
                    "else" to node.elseExpression.accept(this)
                )
            )
        return createJsonNode(NodeType.Expression.name, "ConditionalExpression", children, true, node.location, node.id)
    }

    override fun visit(node: GotoStatement): JsonObject {
        val children = JsonObject(mapOf("targetLabel" to JsonPrimitive(node.label)))
        return createJsonNode(NodeType.Statement.name, "Goto(${node.label})", children, true, node.location, node.id)
    }

    override fun visit(node: LabeledStatement): JsonObject {
        val children =
            JsonObject(
                mapOf(
                    "label" to JsonPrimitive(node.label),
                    "statement" to node.statement.accept(this)
                )
            )
        return createJsonNode(NodeType.Statement.name, "LabeledStatement(${node.label})", children, true, node.location, node.id)
    }

    override fun visit(node: AssignmentExpression): JsonObject {
        val children =
            JsonObject(
                mapOf(
                    "lvalue" to node.lvalue.accept(this),
                    "rvalue" to node.rvalue.accept(this)
                )
            )
        return createJsonNode(NodeType.Expression.name, "Assignment", children, true, node.location, node.id)
    }

    override fun visit(node: VariableDeclaration): JsonObject {
        val childrenMap = mutableMapOf<String, JsonElement>("name" to JsonPrimitive(node.name))
        node.init?.let { childrenMap["initializer"] = it.accept(this) }
        return createJsonNode(
            NodeType.Declaration.name,
            "Declaration(${node.name})",
            JsonObject(childrenMap),
            false,
            node.location,
            node.id
        )
    }

    override fun visit(node: S): JsonObject = node.statement.accept(this)

    override fun visit(node: D): JsonObject =
        when (node.declaration) {
            is VarDecl -> node.declaration.accept(this)
            is FunDecl -> node.declaration.accept(this)
            is VariableDeclaration -> node.declaration.accept(this)
        }

    override fun visit(node: Block): JsonObject {
        val blockItems = node.items.map { it.accept(this) }
        val children = JsonObject(mapOf("block" to JsonArray(blockItems)))
        return createJsonNode(NodeType.Block.name, "Block", children, false, node.location, node.id)
    }

    override fun visit(node: CompoundStatement): JsonObject = node.block.accept(this)

    override fun visit(node: FunctionCall): JsonObject {
        val children =
            JsonObject(
                mapOf(
                    "name" to JsonPrimitive(node.name),
                    "arguments" to JsonArray(node.arguments.map { it.accept(this) })
                )
            )
        return createJsonNode(NodeType.Function.name, "FuncCall(${node.name})", children, false, node.location, node.id)
    }
}
