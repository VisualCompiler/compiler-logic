package assembly

sealed class Function : AsmConstruct()

data class AsmFunction(
    val name: String,
    val body: List<Instruction>
) : Function()
