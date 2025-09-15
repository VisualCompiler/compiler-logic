package assembly

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Function : AsmConstruct()

@Serializable
@SerialName("AsmFunction")
data class AsmFunction(
    val name: String,
    var body: List<Instruction>,
    var stackSize: Int = 0
) : Function()
