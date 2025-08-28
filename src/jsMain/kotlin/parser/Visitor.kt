package parser

interface Visitor<T> {
    fun visit(node: SimpleProgram): T

    fun visit(node: ReturnStatement): T

    fun visit(node: ExpressionStatement): T

    fun visit(node: NullStatement): T

    fun visit(node: Function): T

    fun visit(node: VariableExpression): T

    fun visit(node: UnaryExpression): T

    fun visit(node: BinaryExpression): T

    fun visit(node: IntExpression): T

    fun visit(node: IfStatement): T

    fun visit(node: ConditionalExpression): T

    fun visit(node: GotoStatement): T

    fun visit(node: LabeledStatement): T

    fun visit(node: AssignmentExpression): T

    fun visit(node: Declaration): T

    fun visit(node: S): T

    fun visit(node: D): T
}
