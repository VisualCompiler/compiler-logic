package assembly

import kotlinx.serialization.Serializable

@Serializable
sealed class AsmConstruct {
    protected fun indent(level: Int): String = "  ".repeat(level)
}
