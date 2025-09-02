package integration

import compiler.CompilerStage
import exceptions.DuplicateVariableDeclaration
import exceptions.IncompatibleFuncDeclarationException
import exceptions.InvalidLValueException
import exceptions.InvalidStatementException
import exceptions.LexicalException
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
                code = "int main(void) { return x; }",
                failingStage = CompilerStage.PARSER,
                expectedException = UndeclaredVariableException::class
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
                code = "int main(void) { return || 0;",
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
                failingStage = CompilerStage.PARSER,
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
                code = "int main(void) { break; }",
                failingStage = CompilerStage.PARSER,
                expectedException = InvalidStatementException::class
            ),
            InvalidTestCase(
                code = "int main(void) { continue; }",
                failingStage = CompilerStage.PARSER,
                expectedException = InvalidStatementException::class
            ),
            InvalidTestCase(
                code = "int main(void) { int a = 10; { int a = 5; int a = 10; } }",
                failingStage = CompilerStage.PARSER,
                expectedException = DuplicateVariableDeclaration::class
            ),
            // Function redeclaration with different parameter count (illegal in C)
            InvalidTestCase(
                code = """
                    int func(int a, int b);
                    int func(int a);
                    int main(void) {
                        return func(1, 2);
                    }
                """.trimIndent(),
                failingStage = CompilerStage.PARSER,
                expectedException = IncompatibleFuncDeclarationException::class
            ),
            // Nested function
            InvalidTestCase(
                code = """
                    int main(void) {
                        {
                            int nested(int x) {
                                return x + 1;
                            }
                        }
                        return 0;
                    }
                """.trimIndent(),
                failingStage = CompilerStage.PARSER,
                expectedException = UnexpectedTokenException::class
            )
        )
}
