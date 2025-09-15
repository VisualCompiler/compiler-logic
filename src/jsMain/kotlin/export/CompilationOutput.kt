package export

import compiler.CompilerStage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
sealed class CompilationOutput {
    abstract val stage: String
    abstract val errors: Array<CompilationError>
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
@SerialName("LexerOutput")
data class LexerOutput(
    override val stage: String = CompilerStage.LEXER.name.lowercase(),
    val tokens: String? = null,
    override val errors: Array<CompilationError>,
    val sourceLocation: SourceLocationInfo? = null
) : CompilationOutput()

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
@SerialName("ParserOutput")
data class ParserOutput(
    override val stage: String = CompilerStage.PARSER.name.lowercase(),
    val ast: String? = null,
    override val errors: Array<CompilationError>,
    val sourceLocation: SourceLocationInfo? = null
) : CompilationOutput()

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
@SerialName("TackyOutput")
data class TackyOutput(
    override val stage: String = CompilerStage.TACKY.name.lowercase(),
    val tacky: String? = null,
    val tackyPretty: String? = null,
    val precomputedCFGs: String = "",
    val precomputedAssembly: String = "",
    val optimizations: Array<String?> = arrayOf("CONSTANT_FOLDING", "DEAD_STORE_ELIMINATION"),
    val functionNames: Array<String?> = emptyArray(),
    override val errors: Array<CompilationError>,
    val sourceLocation: SourceLocationInfo? = null
) : CompilationOutput()

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
@SerialName("AssemblyOutput")
data class AssemblyOutput(
    override val stage: String = CompilerStage.ASSEMBLY.name.lowercase(),
    val assembly: String? = null,
    val rawAssembly: String? = null,
    val precomputedAssembly: String = "",
    val selectedOptimizations: Array<String> = emptyArray(),
    override val errors: Array<CompilationError>,
    val sourceLocation: SourceLocationInfo? = null
) : CompilationOutput()

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class SourceLocationInfo(
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
    val totalLines: Int
)

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class CompilationError(
    val stage: String = "undefined",
    val message: String,
    val line: Int,
    val column: Int
)

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class CompilationResult(
    val outputs: Array<CompilationOutput>,
    val overallSuccess: Boolean,
    val overallErrors: Array<CompilationError>
) {
    fun toJsonString(): String =
        Json {
            classDiscriminator = "stageType"
            encodeDefaults = true
        }.encodeToString(this)
}
