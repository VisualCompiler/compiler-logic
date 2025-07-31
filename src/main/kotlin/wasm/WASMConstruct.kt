package org.example.wasm

sealed class WASMConstruct {
    abstract val line: Int
    abstract val column: Int
    abstract fun toWat(indent: Int = 0): String
    protected fun indent(level: Int): String = "  ".repeat(level)
}
