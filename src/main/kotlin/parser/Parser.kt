
package org.example.parser

import lexer.Token
import lexer.TokenType
import org.example.Exceptions.SyntaxError

class Parser {
    fun parseTokens(tokens: List<Token>): ASTNode {
        val tokenSet = tokens.toMutableList()
        val ast = parseProgram(tokenSet)

        val lastToken = tokenSet.removeFirst()
        if (lastToken.type != TokenType.EOF || !tokenSet.isEmpty()) {
            throw SyntaxError(
                line = lastToken.line,
                column = lastToken.column,
                message = "Expected end of file"
            )
        }
        return ast
    }

    private fun parseProgram(tokens: MutableList<Token>): SimpleProgram {
        val function = parseFunction(tokens)

        return SimpleProgram(
            functionDefinition = function,
            line = function.line,
            column = function.column
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
            body = body,
            line = first?.line!!,
            column = first.column
        )
    }

    private fun expect(
        expected: TokenType,
        tokens: MutableList<Token>
    ): Token {
        val token = tokens.removeFirst()

        if (token.type != expected) {
            throw SyntaxError(
                line = token.line,
                column = token.column,
                message = "Expected token: $expected, got ${token.type}"
            )
        }

        return token
    }

    private fun parseIdentifier(tokens: MutableList<Token>): Identifier {
        val token = tokens.removeFirst()
        if (token.type != TokenType.IDENTIFIER) {
            throw SyntaxError(
                line = token.line,
                column = token.column,
                message = "Expected token: ${TokenType.IDENTIFIER}, got ${token.type}"
            )
        }

        return Identifier(
            value = token.lexeme,
            line = token.line,
            column = token.column
        )
    }

    private fun parseStatement(tokens: MutableList<Token>): Statement {
        val first = tokens.firstOrNull()
        expect(TokenType.KEYWORD_RETURN, tokens)
        val expression = parseExpression(tokens)
        expect(TokenType.SEMICOLON, tokens)

        return ReturnStatement(
            expression = expression,
            line = first?.line!!,
            column = first.column
        )
    }

    private fun parseExpression(tokens: MutableList<Token>): Expression {
        val token = tokens.removeFirst()

        // check if its Unary Expression
        if (token.type == TokenType.TILDE || token.type == TokenType.NEGATION) {
            val operator = token
            val expression = parseExpression(tokens)

            return UnaryExpression(operator = operator, expression = expression, line = operator.line, column = operator.column)
        } else if (token.type == TokenType.LEFT_PAREN) {
            expect(TokenType.LEFT_PAREN, tokens)
            val expression = parseExpression(tokens)
            expect(TokenType.RIGHT_PAREN, tokens)
            return expression
        } else {
            if (token.type != TokenType.INT_LITERAL) {
                throw SyntaxError(
                    line = token.line,
                    column = token.column,
                    message = "Expected an integer, unary operator, or parenthesis, but got ${token.type}"
                )
            }
            return IntExpression(
                value = token.lexeme.toInt(),
                line = token.line,
                column = token.column
            )
        }
    }
}
