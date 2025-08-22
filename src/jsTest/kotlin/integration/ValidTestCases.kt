package integration

import assembly.AsmProgram
import lexer.Token
import parser.ASTNode
import tacky.TackyProgram

data class ValidTestCase(
    val code: String,
    val expectedTokenList: List<Token>? = null,
    val expectedAst: ASTNode? = null,
    val expectedTacky: TackyProgram? = null,
    val expectedAssembly: AsmProgram? = null
)

object ValidTestCases {
    val testCases: List<ValidTestCase> =
        listOf(
            // Basic arithmetic operations
            ValidTestCase(
                code = "int main(void) { return 5 + 3; }"
            ),
            ValidTestCase(
                code = "int main(void) { return 10 - 4; }"
            ),
            ValidTestCase(
                code = "int main(void) { return 6 * 7; }"
            ),
            ValidTestCase(
                code = "int main(void) { return 20 / 4; }"
            ),
            ValidTestCase(
                code = "int main(void) { return 17 % 5; }"
            ),
            // Unary operations
            ValidTestCase(
                code = "int main(void) { return ~(-2); }"
            ),
            ValidTestCase(
                code = "int main(void) { return -42; }"
            ),
            // Mixed
            ValidTestCase(
                code = "int main(void) { return (5 + 3) * 2; }"
            ),
            ValidTestCase(
                code = "int main(void) { return 5 + 3 * 2; }"
            ),
            ValidTestCase(
                code = "int main(void) { return -5 + 3; }"
            )
        )
}
