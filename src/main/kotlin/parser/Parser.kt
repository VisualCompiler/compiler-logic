package org.example.parser

import lexer.Token
import lexer.TokenType
import org.example.Exceptions.SyntaxError

class Parser {
    fun parseTokens(tokens: List<Token>): ASTNode {
        val tokens = tokens.toMutableList()
        val ast = parseProgram(tokens)

        val lastToken = tokens.removeFirst()
        if (lastToken.type != TokenType.EOF) {
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

        return SimpleProgram(functionDefinition = function)
    }

    private fun parseFunction(tokens: MutableList<Token>): FunctionDefinition {
        expect(TokenType.KEYWORD_INT, tokens)
        val name = parseIdentifier(tokens)
        expect(TokenType.LEFT_PAREN, tokens)
        expect(TokenType.KEYWORD_VOID, tokens)
        expect(TokenType.RIGHT_PAREN, tokens)
        expect(TokenType.LEFT_BRACK, tokens)
        val body = parseStatement(tokens)
        expect(TokenType.RIGHT_BRACK, tokens)

        return SimpleFunction(name = name, body = body)
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

        return Identifier(value = token.lexeme)
    }

    private fun parseStatement(tokens: MutableList<Token>): Statement {
        expect(TokenType.KEYWORD_RETURN, tokens)
        val expression = parseExpression(tokens)
        expect(TokenType.SEMICOLON, tokens)

        return ReturnStatement(expression = expression)
    }

    private fun parseExpression(tokens: MutableList<Token>): Expression {
        val token = tokens.removeFirst()
        if (token.type != TokenType.INT_LITERAL) {
            throw SyntaxError(
                line = token.line,
                column = token.column,
                message = "Expected token: ${TokenType.INT_LITERAL}, got ${token.type}"
            )
        }
        return IntExpression(value = token.lexeme.toInt())
    }
}
