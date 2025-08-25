import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class ErrorType {
    LEXICAL,
    SYNTAX,
    CODE_GENERATION,
    RUNTIME,
    GENERAL
}

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class CompilationStage {
    LEXER,
    PARSER,
    TACKY,
    CODE_GENERATOR
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
sealed class CompilationOutput {
    abstract val stage: CompilationStage
    abstract val errors: Array<CompilationError>
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
@SerialName("LexerOutput")
data class LexerOutput(
    override val stage: CompilationStage = CompilationStage.LEXER,
    val tokens: String? = null,
    override val errors: Array<CompilationError>
) : CompilationOutput()

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
@SerialName("ParserOutput")
data class ParserOutput(
    override val stage: CompilationStage = CompilationStage.PARSER,
    val ast: String? = null,
    override val errors: Array<CompilationError>
) : CompilationOutput()

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
@SerialName("TackyOutput")
data class TackyOutput(
    override val stage: CompilationStage = CompilationStage.TACKY,
    val tackyJson: String? = null,
    val tackyPretty: String? = null,
    override val errors: Array<CompilationError>
) : CompilationOutput()

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
@SerialName("CodeGeneratorOutput")
data class CodeGeneratorOutput(
    override val stage: CompilationStage = CompilationStage.CODE_GENERATOR,
    val assembly: String? = null,
    override val errors: Array<CompilationError>
) : CompilationOutput()

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class CompilationError(
    val type: ErrorType,
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
