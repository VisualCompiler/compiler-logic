package parser

interface Visitor<T> {
    fun visit(node: SimpleProgram): T

    fun visit(node: ReturnStatement): T

    fun visit(node: SimpleFunction): T

    fun visit(node: UnaryExpression): T

    fun visit(node: BinaryExpression): T

    fun visit(node: IntExpression): T
}
