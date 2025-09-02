package assembly

sealed class Function : AsmConstruct()

data class AsmFunction(
    val name: String,
    var body: List<Instruction>,
    var stackSize: Int = 0
) : Function()
