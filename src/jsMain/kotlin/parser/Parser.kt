package parser

import exceptions.UnexpectedEndOfFileException
import exceptions.UnexpectedTokenSyntaxException
import lexer.Token
import lexer.TokenType

class Parser {
    private val precedenceMap =
        mapOf(
            TokenType.PLUS to 45,
            TokenType.NEGATION to 45,
            TokenType.MULTIPLY to 50,
            TokenType.DIVIDE to 50,
            TokenType.REMAINDER to 50,
            TokenType.GREATER to 35,
            TokenType.GREATER_EQUAL to 35,
            TokenType.LESS to 35,
            TokenType.LESS_EQUAL to 35,
            TokenType.EQUAL_TO to 30,
            TokenType.NOT_EQUAL to 30,
            TokenType.AND to 10,
            TokenType.OR to 5
        )

    fun parseTokens(tokens: List<Token>): ASTNode {
        val tokenSet = tokens.toMutableList()
        val ast = parseProgram(tokenSet)

        val lastToken = tokenSet.removeFirst()
        if (lastToken.type != TokenType.EOF || !tokenSet.isEmpty()) {
            throw UnexpectedEndOfFileException(
                line = lastToken.line,
                column = lastToken.column
            )
        }
        return ast
    }

    private fun parseProgram(tokens: MutableList<Token>): SimpleProgram {
        val function = parseFunction(tokens)

        return SimpleProgram(
            functionDefinition = function
        )
    }

    private fun parseFunction(tokens: MutableList<Token>): FunctionDefinition {
        val first = tokens.firstOrNull()
        expect(TokenType.KEYWORD_INT, tokens)
        val name = parseIdentifier(tokens)
        expect(TokenType.LEFT_PAREN, tokens)
        expect(TokenType.KEYWORD_VOID, tokens)
        expect(TokenType.RIGHT_PAREN, tokens)
        expect(TokenType.LEFT_BRACK, tokens)
        val body = parseStatement(tokens)
        expect(TokenType.RIGHT_BRACK, tokens)

        return SimpleFunction(
            name = name,
            body = body
        )
    }

    private fun expect(
        expected: TokenType,
        tokens: MutableList<Token>
    ): Token {
        val token = tokens.removeFirst()

        if (token.type != expected) {
            throw UnexpectedTokenSyntaxException(
                expected = expected.toString(),
                actual = token.type.toString(),
                line = token.line,
                column = token.column
            )
        }

        return token
    }

    private fun parseIdentifier(tokens: MutableList<Token>): String {
        val token = tokens.removeFirst()
        if (token.type != TokenType.IDENTIFIER) {
            throw UnexpectedTokenSyntaxException(
                expected = TokenType.IDENTIFIER.toString(),
                actual = token.type.toString(),
                line = token.line,
                column = token.column
            )
        }

        return token.lexeme
    }

    private fun parseStatement(tokens: MutableList<Token>): Statement {
        val first = tokens.firstOrNull()
        expect(TokenType.KEYWORD_RETURN, tokens)
        val expression = parseExpression(45, tokens)
        expect(TokenType.SEMICOLON, tokens)

        return ReturnStatement(
            expression = expression
        )
    }

    private fun parseExpression(
        minPrec: Int = 45,
        tokens: MutableList<Token>
    ): Expression {
        var left = parseFactor(tokens)

        while (tokens.isNotEmpty()) {
            val nextToken = tokens.first()
            val prec = precedenceMap[nextToken.type] ?: break

            if (prec < minPrec) {
                break
            }

            val operator = tokens.removeFirst()
            val right = parseExpression(prec + 5, tokens)

            left =
                BinaryExpression(
                    left = left,
                    operator = operator,
                    right = right
                )
        }
        return left
    }

    private fun parseFactor(tokens: MutableList<Token>): Expression {
        var nextToken = tokens.first()
        if (nextToken.type == TokenType.INT_LITERAL) {
            nextToken = tokens.removeFirst()
            return IntExpression(value = nextToken.lexeme.toInt())
        } else if (nextToken.type == TokenType.TILDE || nextToken.type == TokenType.NEGATION || nextToken.type == TokenType.NOT) {
            val operator = tokens.removeFirst()
            val factor = parseFactor(tokens)
            return UnaryExpression(operator = operator, expression = factor)
        } else if (nextToken.type == TokenType.LEFT_PAREN) {
            expect(TokenType.LEFT_PAREN, tokens)
            val expression = parseExpression(45, tokens)
            expect(TokenType.RIGHT_PAREN, tokens)
            return expression
        } else {
            val nToken = tokens.removeFirst()
            throw UnexpectedTokenSyntaxException(
                expected = "literal, unary operator, or '('",
                actual = nToken.type.toString(),
                line = nToken.line,
                column = nToken.column
            )
        }
    }
}
