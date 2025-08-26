package integration

import exceptions.LexicalException
import exceptions.UnexpectedTokenSyntaxException
import kotlin.reflect.KClass

data class InvalidTestCase(
    val code: String,
    val failingStage: CompilerStage,
    val expectedException: KClass<out Exception>
)

object InvalidTestCases {
    val testCases: List<InvalidTestCase> =
        listOf(
            // Lexical errors
            InvalidTestCase(
                code = "#",
                failingStage = CompilerStage.LEXER,
                expectedException = LexicalException::class
            ),
            // Parser errors
            InvalidTestCase(
                code = "int main() { return x; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            InvalidTestCase(
                code = "int main(void) { ret }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return 5 + 3 }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return (5 + 3; } ;",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return --9; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            // Expression errors
            InvalidTestCase(
                code = "int main(void) { return +; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return * 5; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return 5 / ; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return 5 % ; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            InvalidTestCase(
                code = "int 5;",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            // Invalid Relational/Logical Expressions ---
            InvalidTestCase(
                code = "int main(void) { return 5 <; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return > 10; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return 1 &&; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return || 0; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenSyntaxException::class
            )
        )
}
