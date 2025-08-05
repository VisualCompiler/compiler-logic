package org.example.parser

import lexer.Token
import lexer.TokenType
import org.example.Exceptions.SyntaxError

class Parser {
    fun parseTokens(incomingTokens: List<Token>): ASTNode {
        val tokens = incomingTokens.toMutableList()
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

        return Identifier(token = token)
    }

    private fun parseStatement(tokens: MutableList<Token>): Statement {
        expect(TokenType.KEYWORD_RETURN, tokens)
        val expression = parseExpression(tokens)
        expect(TokenType.SEMICOLON, tokens)

        return ReturnStatement(expression = expression)
    }

    private fun parseExpression(tokens: MutableList<Token>): Expression {
        return parseAddition(tokens)
    }

    private fun parseAddition(tokens: MutableList<Token>): Expression {
        var left_operand = parseTerm(tokens)

        while (check(TokenType.PLUS, tokens) || check(TokenType.MINUS, tokens)) {
            var operator = tokens.removeFirst()
            var right_operand = parseTerm(tokens)
            left_operand = BinaryExpression(left_operand, operator, right_operand)
        }
        return left_operand
    }

    private fun parseTerm(tokens: MutableList<Token>): Expression {
        var left_operand = parsePrimary(tokens)

        while (check(TokenType.MULTIPLY, tokens) || check(TokenType.DIVIDE, tokens)) {
            var operator = tokens.removeFirst()
            var right_operand = parsePrimary(tokens)
            left_operand = BinaryExpression(left_operand, operator, right_operand)
        }
        return left_operand
    }

    private fun parsePrimary(tokens: MutableList<Token>): Expression {
        if (check(TokenType.INT_LITERAL, tokens)) {
            return IntExpression(tokens.removeFirst())
        }

        if (check(TokenType.LEFT_PAREN, tokens)) {
            expect(TokenType.LEFT_PAREN, tokens)
            val expression = parseExpression(tokens)
            expect(TokenType.RIGHT_PAREN, tokens)
            return expression
        }

        val unexpectedToken = tokens.first()
        throw SyntaxError(
            line = unexpectedToken.line,
            column = unexpectedToken.column,
            message = "Unexpected token: ${unexpectedToken.type}"
        )
    }

    private fun check(type: TokenType, tokens: MutableList<Token>): Boolean {
        if (tokens.isEmpty()) return false
        return tokens.first().type == type
    }
}
