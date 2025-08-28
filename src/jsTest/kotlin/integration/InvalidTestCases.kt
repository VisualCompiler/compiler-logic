package integration

import compiler.CompilerStage
import exceptions.DuplicateLabelException
import exceptions.DuplicateVariableDeclaration
import exceptions.InvalidLValueException
import exceptions.LexicalException
import exceptions.UndeclaredLabelException
import exceptions.UndeclaredVariableException
import exceptions.UnexpectedTokenException
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
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { ret }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return 5 + 3 }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return (5 + 3; } ;",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return --9; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            // Expression errors
            InvalidTestCase(
                code = "int main(void) { return +; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return * 5; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return 5 / ; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return 5 % ; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int 5;",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            // Invalid Relational/Logical Expressions ---
            InvalidTestCase(
                code = "int main(void) { return 5 <; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return > 10; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return 1 &&; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return || 0; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { a = 3; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UndeclaredVariableException::class
            ),
            InvalidTestCase(
                code = "int main(void) { int a; int a; }",
                failingStage = CompilerStage.PARSER,
                expectedException = DuplicateVariableDeclaration::class
            ),
            InvalidTestCase(
                code = "int main(void) { 2 = 3; }",
                failingStage = CompilerStage.PARSER,
                expectedException = InvalidLValueException::class
            ),
            InvalidTestCase(
                code = "int main(void) { if (1) return; else }", // 'else' without a statement
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { if 1 return 1; }", // Missing parentheses around condition
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { else return 1; }", // 'else' without a preceding 'if'
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            // Syntax Errors for Conditional Operator (? :)
            InvalidTestCase(
                code = "int main(void) { return 1 ? 2; }", // Missing the ':' part
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            InvalidTestCase(
                code = "int main(void) { return 1 : 2; }", // Missing the '?' part
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            // Syntax Errors for GOTO and LABELS
            InvalidTestCase(
                code = "int main(void) { goto ; }", // 'goto' without a label identifier
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            ),
            // Semantic Errors (caught after parsing)
            InvalidTestCase(
                code = "int main(void) { int a; int a; return a; }", // Duplicate variable
                failingStage = CompilerStage.PARSER, // Thrown by VariableResolution, caught in the Parser stage
                expectedException = DuplicateVariableDeclaration::class
            ),
            InvalidTestCase(
                code = "int main(void) { return a; }", // Undeclared variable
                failingStage = CompilerStage.PARSER,
                expectedException = UndeclaredVariableException::class
            ),
            InvalidTestCase(
                code = "int main(void) { 1 = 2; return 0; }", // Invalid L-value
                failingStage = CompilerStage.PARSER,
                expectedException = InvalidLValueException::class
            ),
            InvalidTestCase(
                code = "int main(void) { my_label: return 0; my_label: return 1; }", // Duplicate label
                failingStage = CompilerStage.PARSER,
                expectedException = DuplicateLabelException::class
            ),
            InvalidTestCase(
                code = "int main(void) { goto missing_label; }", // Undeclared label
                failingStage = CompilerStage.PARSER,
                expectedException = UndeclaredLabelException::class
            )
        )
}
