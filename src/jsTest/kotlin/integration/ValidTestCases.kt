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
            ),
            ValidTestCase(code = "int main(void) { return 5 < 10; }"), // Should return 1 (true)
            ValidTestCase(code = "int main(void) { return 10 < 5; }"), // Should return 0 (false)
            ValidTestCase(code = "int main(void) { return 8 > 3; }"), // Should return 1
            ValidTestCase(code = "int main(void) { return 3 > 8; }"), // Should return 0
            ValidTestCase(code = "int main(void) { return 4 <= 4; }"), // Should return 1
            ValidTestCase(code = "int main(void) { return 5 <= 4; }"), // Should return 0
            ValidTestCase(code = "int main(void) { return 7 >= 7; }"), // Should return 1
            ValidTestCase(code = "int main(void) { return 6 >= 7; }"), // Should return 0
            ValidTestCase(code = "int main(void) { return 9 == 9; }"), // Should return 1
            ValidTestCase(code = "int main(void) { return 9 == 8; }"), // Should return 0
            ValidTestCase(code = "int main(void) { return 1 != 2; }"), // Should return 1
            ValidTestCase(code = "int main(void) { return 2 != 2; }"), // Should return 0
            // Logical Operator Tests ---
            ValidTestCase(code = "int main(void) { return !0; }"), // Should return 1
            ValidTestCase(code = "int main(void) { return !5; }"), // Should return 0
            ValidTestCase(code = "int main(void) { return !(5 < 2); }"), // Should return !0 -> 1
            // Short-Circuiting Tests ---
            ValidTestCase(code = "int main(void) { return 1 && 1; }"), // Should return 1
            ValidTestCase(code = "int main(void) { return 1 && 0; }"), // Should return 0
            ValidTestCase(code = "int main(void) { return 0 && 1; }"), // Should short-circuit and return 0
            ValidTestCase(code = "int main(void) { return 1 || 0; }"), // Should short-circuit and return 1
            ValidTestCase(code = "int main(void) { return 0 || 1; }"), // Should return 1
            ValidTestCase(code = "int main(void) { return 0 || 0; }"), // Should return 0
            // NEW: Complex Combination Tests ---
            ValidTestCase(code = "int main(void) { return 5 > 3 && 1 < 10; }"), // 1 && 1 -> 1
            ValidTestCase(code = "int main(void) { return (1 == 2) || (3 != 4); }") // 0 || 1 -> 1
        )
}
