package parser

interface Visitor<T> {
    fun visit(node: SimpleProgram): T

    fun visit(node: ReturnStatement): T

    fun visit(node: ExpressionStatement): T

    fun visit(node: NullStatement): T

    fun visit(node: BreakStatement): T

    fun visit(node: ContinueStatement): T

    fun visit(node: WhileStatement): T

    fun visit(node: DoWhileStatement): T

    fun visit(node: ForStatement): T

    fun visit(node: InitDeclaration): T

    fun visit(node: InitExpression): T

    fun visit(node: VariableExpression): T

    fun visit(node: UnaryExpression): T

    fun visit(node: BinaryExpression): T

    fun visit(node: IntExpression): T

    fun visit(node: IfStatement): T

    fun visit(node: ConditionalExpression): T

    fun visit(node: GotoStatement): T

    fun visit(node: LabeledStatement): T

    fun visit(node: AssignmentExpression): T

    fun visit(node: VariableDeclaration): T

    fun visit(node: FunctionDeclaration): T

    fun visit(node: VarDecl): T

    fun visit(node: FunDecl): T

    fun visit(node: S): T

    fun visit(node: D): T

    fun visit(node: Block): T

    fun visit(node: CompoundStatement): T

    fun visit(node: FunctionCall): T
}
