package assembly

sealed class Program : AsmConstruct()

data class AsmProgram(
    val functions: List<AsmFunction>
) : Program()
