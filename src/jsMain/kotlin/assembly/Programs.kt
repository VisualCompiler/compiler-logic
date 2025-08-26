package assembly

sealed class Program : AsmConstruct()

data class AsmProgram(
    val function: AsmFunction
) : Program()
