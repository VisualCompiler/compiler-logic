package assembly

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Program : AsmConstruct()

@Serializable
@SerialName("AsmProgram")
data class AsmProgram(
    val functions: List<AsmFunction>
) : Program()
