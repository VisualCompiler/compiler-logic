package org.example.wasm

sealed class Module : WASMConstruct()

data class SimpleModule(
    val function: Function,
) : Module()
