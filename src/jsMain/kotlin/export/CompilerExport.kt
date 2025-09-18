package export

import assembly.AsmProgram
import assembly.CodeEmitter
import compiler.CompilerStage
import compiler.CompilerWorkflow
import exceptions.CompilationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import lexer.Lexer
import lexer.Token
import optimizations.Block
import optimizations.ControlFlowGraph
import optimizations.EXIT
import optimizations.OptimizationManager
import optimizations.OptimizationType
import optimizations.START
import tacky.TackyProgram

@Serializable
data class CFGNode(
    val id: String,
    val label: String,
    val type: String
)

@Serializable
data class CFGEdge(
    val from: String,
    val to: String
)

@Serializable
data class CFGExport(
    val functionName: String,
    val nodes: List<CFGNode>,
    val edges: List<CFGEdge>
)

@Serializable
data class CFGEntry(
    val functionName: String,
    val appliedOptimizations: List<String>,
    val cfg: String
)

@Serializable
data class AssemblyEntry(
    val optimizations: List<String>,
    val asmCode: String
)

@OptIn(ExperimentalJsExport::class)
@JsExport
class CompilerExport {
    private fun calculateSourceLocationInfo(code: String): SourceLocationInfo {
        val lines = code.split('\n')
        val totalLines = lines.size
        val lastLine = lines.lastOrNull() ?: ""
        val endColumn = lastLine.length + 1

        return SourceLocationInfo(
            startLine = 1,
            startColumn = 1,
            endLine = totalLines,
            endColumn = endColumn,
            totalLines = totalLines
        )
    }

    fun exportCompilationResults(code: String): String {
        val outputs = mutableListOf<CompilationOutput>()
        val overallErrors = mutableListOf<CompilationError>()
        val codeEmitter = CodeEmitter()
        val sourceLocationInfo = calculateSourceLocationInfo(code)

        try {
            val tokens = CompilerWorkflow.take(code)
            Lexer(code)
            outputs.add(
                LexerOutput(
                    tokens = tokens.toJsonString(),
                    errors = emptyArray(),
                    sourceLocation = sourceLocationInfo
                )
            )
            val ast = CompilerWorkflow.take(tokens)
            outputs.add(
                ParserOutput(
                    errors = emptyArray(),
                    ast = Json.encodeToString(ast.accept(ASTExport())),
                    sourceLocation = sourceLocationInfo
                )
            )
            val tacky = CompilerWorkflow.take(ast)
            val tackyProgram = tacky as TackyProgram
            outputs.add(
                TackyOutput(
                    tackyPretty = tackyProgram.toPseudoCode(),
                    functionNames = tackyProgram.functions.map { it.name }.toTypedArray(),
                    precomputedCFGs = precomputeAllCFGs(tackyProgram),
                    precomputedAssembly = precomputeAllAssembly(tackyProgram),
                    errors = emptyArray(),
                    tacky = Json.encodeToString(tackyProgram),
                    sourceLocation = sourceLocationInfo
                )
            )
            val optimizedTacky =
                CompilerWorkflow.take(
                    tacky,
                    optimizations =
                    listOf(
                        OptimizationType.CONSTANT_FOLDING,
                        OptimizationType.DEAD_STORE_ELIMINATION,
                        OptimizationType.COPY_PROPAGATION,
                        OptimizationType.UNREACHABLE_CODE_ELIMINATION
                    )
                )
            val asm = CompilerWorkflow.take(optimizedTacky)
            val finalAssemblyString = codeEmitter.emit(asm as AsmProgram)
            val rawAssembly = codeEmitter.emitRaw(asm)
            outputs.add(
                AssemblyOutput(
                    errors = emptyArray(),
                    assembly = finalAssemblyString,
                    rawAssembly = rawAssembly,
                    sourceLocation = sourceLocationInfo
                )
            )
        } catch (e: CompilationException) {
            val stage =
                when {
                    outputs.isEmpty() -> CompilerStage.LEXER
                    outputs.size == 1 -> CompilerStage.PARSER
                    outputs.size == 2 -> CompilerStage.TACKY
                    else -> CompilerStage.ASSEMBLY
                }
            val error =
                CompilationError(
                    stage = stage.name.lowercase(),
                    message = e.message ?: "Unknown ${stage.name.lowercase()} error",
                    line = e.line ?: -1,
                    column = e.column ?: -1
                )
            overallErrors.add(error)
            when (stage) {
                CompilerStage.LEXER -> outputs.add(LexerOutput(errors = arrayOf(error), sourceLocation = sourceLocationInfo))
                CompilerStage.PARSER -> outputs.add(ParserOutput(errors = arrayOf(error), sourceLocation = sourceLocationInfo))
                CompilerStage.TACKY -> outputs.add(TackyOutput(errors = arrayOf(error), sourceLocation = sourceLocationInfo))
                CompilerStage.ASSEMBLY -> outputs.add(AssemblyOutput(errors = arrayOf(error), sourceLocation = sourceLocationInfo))
            }
        } catch (e: Exception) {
            // Fallback for any unexpected runtime errors
            val error =
                CompilationError(
                    message = e.message ?: "Unknown error",
                    line = -1,
                    column = -1
                )
            overallErrors.add(error)
            // ensure we return four stages
            while (outputs.size < 3) {
                outputs.add(ParserOutput(errors = emptyArray(), sourceLocation = sourceLocationInfo))
            }
            outputs.add(AssemblyOutput(errors = arrayOf(error), sourceLocation = sourceLocationInfo))
        }

        val result =
            CompilationResult(
                outputs = outputs.toTypedArray(),
                overallSuccess = overallErrors.isEmpty(),
                overallErrors = overallErrors.toTypedArray()
            )

        return result.toJsonString()
    }

    private fun precomputeAllCFGs(program: TackyProgram): String {
        val allOptLists = generateOptimizationCombinations()
        val cfgs =
            program.functions.filter { it.body.isNotEmpty() }.flatMap { fn ->
                allOptLists.map { optList ->
                    try {
                        val cfg = ControlFlowGraph().construct(fn.name, fn.body)
                        val types = optList.mapNotNull(optTypeMap::get)
                        val optimized = OptimizationManager.applyOptimizations(cfg, types)
                        CFGEntry(fn.name, optList, exportControlFlowGraph(optimized))
                    } catch (_: Exception) {
                        CFGEntry(fn.name, optList.sorted(), createEmptyCFGJson(fn.name))
                    }
                }
            }
        return Json.encodeToString(cfgs)
    }

    private fun precomputeAllAssembly(program: TackyProgram): String {
        val allOptLists = generateOptimizationCombinations()
        val assemblies = mutableListOf<AssemblyEntry>()

        for (optList in allOptLists) {
            val optimizedTacky =
                CompilerWorkflow.take(
                    program,
                    optimizations = optList.mapNotNull(optTypeMap::get)
                )
            val asm = CompilerWorkflow.take(optimizedTacky)
            val finalAssemblyString = CodeEmitter().emit(asm as AsmProgram)

            // Create one entry per optimization set with the full program assembly
            assemblies.add(AssemblyEntry(optList, finalAssemblyString))
        }

        val result = Json.encodeToString(assemblies)
        return result
    }

    private val optTypeMap =
        mapOf(
            "CONSTANT_FOLDING" to OptimizationType.CONSTANT_FOLDING,
            "DEAD_STORE_ELIMINATION" to OptimizationType.DEAD_STORE_ELIMINATION,
            "COPY_PROPAGATION" to OptimizationType.COPY_PROPAGATION,
            "UNREACHABLE_CODE_ELIMINATION" to OptimizationType.UNREACHABLE_CODE_ELIMINATION
        )

    private fun generateOptimizationCombinations(): List<List<String>> {
        val opts = optTypeMap.keys.sorted()
        return (0 until (1 shl opts.size)).map { mask ->
            opts.filterIndexed { i, _ -> mask and (1 shl i) != 0 }
        }
    }

    private fun createEmptyCFGJson(fn: String): String =
        Json.encodeToString(
            CFGExport(
                functionName = fn,
                nodes = listOf(CFGNode("entry", "Entry", "entry"), CFGNode("exit", "Exit", "exit")),
                edges = listOf(CFGEdge("entry", "exit"))
            )
        )

    private fun exportControlFlowGraph(cfg: ControlFlowGraph): String {
        val nodes = mutableListOf(CFGNode("entry", "Entry", "entry"))
        nodes +=
            cfg.blocks.mapIndexed { i, block ->
                val id = "block_$i"
                val label = block.instructions.joinToString(";\n") { it.toPseudoCode(0) }.ifEmpty { "Empty Block" }
                CFGNode(id, label, "block")
            }
        nodes += CFGNode("exit", "Exit", "exit")

        val edges =
            cfg.edges.map { edge ->
                val fromId =
                    when (edge.from) {
                        is START -> "entry"
                        is EXIT -> "exit"
                        is Block -> {
                            // Find block by ID instead of object equality
                            val index = cfg.blocks.indexOfFirst { it.id == edge.from.id }
                            if (index >= 0) "block_$index" else "unknown_block"
                        }
                    }
                val toId =
                    when (edge.to) {
                        is START -> "entry"
                        is EXIT -> "exit"
                        is Block -> {
                            // Find block by ID instead of object equality
                            val index = cfg.blocks.indexOfFirst { it.id == edge.to.id }
                            if (index >= 0) "block_$index" else "unknown_block"
                        }
                    }
                CFGEdge(fromId, toId)
            }

        return Json.encodeToString(CFGExport(cfg.functionName ?: "unknown", nodes, edges))
    }

    private fun Any.toId(cfg: ControlFlowGraph): String =
        when (this) {
            is START -> "entry"
            is EXIT -> "exit"
            is Block -> {
                val index = cfg.blocks.indexOf(this)
                if (index >= 0) "block_$index" else "unknown_block"
            }
            else -> "unknown"
        }

    fun getCFGForFunction(
        precomputed: String?,
        fn: String,
        enabledOpts: Array<String>
    ): String {
        if (precomputed == null) return createEmptyCFGJson(fn)
        val sortedOpts = enabledOpts.sorted()
        return try {
            val entries = Json.decodeFromString<List<CFGEntry>>(precomputed)
            val cfgString = entries.find { it.functionName == fn && it.appliedOptimizations == sortedOpts }?.cfg
            cfgString ?: createEmptyCFGJson(fn)
        } catch (_: Exception) {
            createEmptyCFGJson(fn)
        }
    }
}

fun List<Token>.toJsonString(): String {
    val jsonTokens =
        this.map { token ->
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(token.type.toString()),
                    "lexeme" to JsonPrimitive(token.lexeme),
                    "location" to
                        JsonObject(
                            mapOf(
                                "startLine" to JsonPrimitive(token.startLine),
                                "startCol" to JsonPrimitive(token.startColumn),
                                "endLine" to JsonPrimitive(token.endLine),
                                "endCol" to JsonPrimitive(token.endColumn)
                            )
                        )
                )
            )
        }

    return Json.encodeToString(JsonArray(jsonTokens))
}
