package compiler.symanticAnalysis

sealed class Type

object IntType : Type()

data class FunType(
    val paramCount: Int
) : Type()

data class Symbol(
    val type: Type,
    val isDefined: Boolean
)

object SymbolTable {
    val table = mutableMapOf<String, Symbol>()

    fun clear() {
        table.clear()
    }

    fun add(
        name: String,
        symbol: Symbol
    ) {
        table[name] = symbol
    }

    fun get(name: String): Symbol? = table[name]
}
