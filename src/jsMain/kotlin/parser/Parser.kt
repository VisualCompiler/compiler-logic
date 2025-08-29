package parser

import exceptions.InvalidLValueException
import exceptions.UnexpectedEndOfFileException
import exceptions.UnexpectedTokenException
import lexer.Token
import lexer.TokenType

class Parser {
    private val precedenceMap =
        mapOf(
            TokenType.ASSIGN to 1,
            TokenType.QUESTION_MARK to 3,
            TokenType.OR to 5,
            TokenType.AND to 10,
            TokenType.EQUAL_TO to 30,
            TokenType.NOT_EQUAL to 30,
            TokenType.LESS to 35,
            TokenType.LESS_EQUAL to 35,
            TokenType.GREATER to 35,
            TokenType.GREATER_EQUAL to 35,
            TokenType.PLUS to 45,
            TokenType.NEGATION to 45,
            TokenType.MULTIPLY to 50,
            TokenType.DIVIDE to 50,
            TokenType.REMAINDER to 50
        )

    fun parseTokens(tokens: List<Token>): ASTNode {
        val tokenSet = tokens.toMutableList()
        val ast = parseProgram(tokenSet)

        // After a full program is parsed, we must be at EOF
        expect(TokenType.EOF, tokenSet)
        if (!tokenSet.isEmpty()) {
            throw UnexpectedEndOfFileException()
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
        expect(TokenType.KEYWORD_INT, tokens)
        val name = parseIdentifier(tokens)
        expect(TokenType.LEFT_PAREN, tokens)
        expect(TokenType.KEYWORD_VOID, tokens)
        expect(TokenType.RIGHT_PAREN, tokens)
        expect(TokenType.LEFT_BRACK, tokens)
        val body = mutableListOf<BlockItem>()
        while (!tokens.isEmpty() && tokens.first().type != TokenType.RIGHT_BRACK) {
            val next = parseBlockItem(tokens)
            body.add(next)
        }
        expect(TokenType.RIGHT_BRACK, tokens)

        return Function(
            name = name,
            body = body
        )
    }

    private fun parseBlockItem(tokens: MutableList<Token>): BlockItem =
        if (tokens.firstOrNull()?.type == TokenType.KEYWORD_INT) {
            D(parseDeclaration(tokens))
        } else {
            S(parseStatement(tokens))
        }

    private fun parseDeclaration(tokens: MutableList<Token>): Declaration {
        expect(TokenType.KEYWORD_INT, tokens)
        val name = parseIdentifier(tokens)
        var exp: Expression? = null
        if (!tokens.isEmpty() && tokens.first().type == TokenType.ASSIGN) {
            tokens.removeFirst()
            exp = parseExpression(tokens = tokens)
        }
        expect(TokenType.SEMICOLON, tokens)

        return Declaration(
            name = name,
            init = exp
        )
    }

    private fun expect(
        expected: TokenType,
        tokens: MutableList<Token>
    ): Token {
        if (tokens.isEmpty()) {
            throw UnexpectedEndOfFileException()
        }
        val token = tokens.removeFirst()

        if (token.type != expected) {
            throw UnexpectedTokenException(
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
            throw UnexpectedTokenException(
                expected = TokenType.IDENTIFIER.toString(),
                actual = token.type.toString(),
                line = token.line,
                column = token.column
            )
        }

        return token.lexeme
    }

    private fun parseStatement(tokens: MutableList<Token>): Statement {
        val firstToken = tokens.firstOrNull() ?: throw UnexpectedEndOfFileException()
        val secondToken = if (tokens.size > 1) tokens[1] else null
        when (firstToken.type) {
            TokenType.IF -> {
                tokens.removeFirst()
                expect(TokenType.LEFT_PAREN, tokens)
                val condition = parseExpression(tokens = tokens)
                expect(TokenType.RIGHT_PAREN, tokens)
                val thenStatement = parseStatement(tokens)
                var elseStatement: Statement? = null
                if (tokens.firstOrNull()?.type == TokenType.ELSE) {
                    tokens.removeFirst()
                    elseStatement = parseStatement(tokens)
                }
                return IfStatement(condition, thenStatement, elseStatement)
            }
            TokenType.KEYWORD_RETURN -> {
                tokens.removeFirst()
                val expression = parseExpression(tokens = tokens)
                expect(TokenType.SEMICOLON, tokens)
                return ReturnStatement(
                    expression = expression
                )
            }
            TokenType.GOTO -> {
                tokens.removeFirst()
                val label = parseIdentifier(tokens)
                expect(TokenType.SEMICOLON, tokens)
                return GotoStatement(label)
            }
            TokenType.IDENTIFIER -> {
                // Handle labeled statements: IDENTIFIER followed by COLON
                if (secondToken?.type == TokenType.COLON) {
                    val labelName = parseIdentifier(tokens)
                    expect(TokenType.COLON, tokens)
                    val statement = parseStatement(tokens)
                    return LabeledStatement(labelName, statement)
                } else {
                    // Not a label: parse as expression statement by delegating to default branch
                    val expression = parseOptionalExpression(tokens = tokens, followedByType = TokenType.SEMICOLON)
                    return if (expression != null) ExpressionStatement(expression) else NullStatement()
                }
            }
            TokenType.KEYWORD_BREAK -> {
                tokens.removeFirst()
                expect(TokenType.SEMICOLON, tokens)
                return BreakStatement()
            }
            TokenType.KEYWORD_CONTINUE -> {
                tokens.removeFirst()
                expect(TokenType.SEMICOLON, tokens)
                return ContinueStatement()
            }
            TokenType.KEYWORD_WHILE -> {
                tokens.removeFirst()
                expect(TokenType.LEFT_PAREN, tokens)
                val condition = parseExpression(tokens = tokens)
                expect(TokenType.RIGHT_PAREN, tokens)
                val body = parseStatement(tokens)
                return WhileStatement(condition, body)
            }
            TokenType.KEYWORD_DO -> {
                tokens.removeFirst()
                val body = parseStatement(tokens)
                expect(TokenType.KEYWORD_WHILE, tokens)
                expect(TokenType.LEFT_PAREN, tokens)
                val condition = parseExpression(tokens = tokens)
                expect(TokenType.RIGHT_PAREN, tokens)
                expect(TokenType.SEMICOLON, tokens)
                return DoWhileStatement(condition, body)
            }
            TokenType.KEYWORD_FOR -> {
                tokens.removeFirst()
                expect(TokenType.LEFT_PAREN, tokens)
                val init = parseForInit(tokens)
                val condition = parseOptionalExpression(tokens = tokens, followedByType = TokenType.SEMICOLON)
                val post = parseOptionalExpression(tokens = tokens, followedByType = TokenType.RIGHT_PAREN)
                val body = parseStatement(tokens)
                return ForStatement(
                    init = init,
                    condition = condition,
                    post = post,
                    body = body
                )
            }
            else -> {
                val expression = parseOptionalExpression(tokens = tokens, followedByType = TokenType.SEMICOLON)
                return if (expression != null) ExpressionStatement(expression) else NullStatement()
            }
        }
    }

    private fun parseForInit(tokens: MutableList<Token>): ForInit {
        if (tokens.firstOrNull()?.type == TokenType.KEYWORD_INT) {
            val declaration = parseDeclaration(tokens)
            return InitDeclaration(declaration)
        }
        val expression = parseOptionalExpression(tokens = tokens, followedByType = TokenType.SEMICOLON)
        return InitExpression(expression)
    }

    private fun parseExpression(
        minPrec: Int = 0,
        tokens: MutableList<Token>
    ): Expression {
        var left = parseFactor(tokens)

        while (tokens.isNotEmpty()) {
            val nextType = tokens.first().type
            val prec = precedenceMap[nextType] ?: break
            if (prec < minPrec) break

            val op = tokens.removeFirst()

            left =
                when (nextType) {
                    TokenType.ASSIGN -> {
                        if (left !is VariableExpression) {
                            throw InvalidLValueException()
                        }
                        val right = parseExpression(prec, tokens)
                        AssignmentExpression(left, right)
                    }
                    TokenType.QUESTION_MARK -> {
                        val thenExpression = parseExpression(prec, tokens)
                        expect(TokenType.COLON, tokens)
                        val elseExpression = parseExpression(prec, tokens)
                        return ConditionalExpression(left, thenExpression, elseExpression)
                    }
                    else -> {
                        val right = parseExpression(prec + 1, tokens)
                        BinaryExpression(
                            left = left,
                            operator = op,
                            right = right
                        )
                    }
                }
        }
        return left
    }

    private fun parseOptionalExpression(
        minPrec: Int = 0,
        tokens: MutableList<Token>,
        followedByType: TokenType
    ): Expression? {
        if (tokens.first().type == followedByType) {
            expect(followedByType, tokens)
            return null
        }
        val expression = parseExpression(minPrec, tokens)
        expect(followedByType, tokens)
        return expression
    }

    private fun parseFactor(tokens: MutableList<Token>): Expression {
        var nextToken = tokens.first()
        when (nextToken.type) {
            TokenType.INT_LITERAL -> {
                nextToken = tokens.removeFirst()
                return IntExpression(value = nextToken.lexeme.toInt())
            }
            TokenType.IDENTIFIER -> {
                nextToken = tokens.removeFirst()
                return VariableExpression(name = nextToken.lexeme)
            }
            TokenType.TILDE, TokenType.NEGATION, TokenType.NOT -> {
                val operator = tokens.removeFirst()
                val factor = parseFactor(tokens)
                return UnaryExpression(operator = operator, expression = factor)
            }
            TokenType.LEFT_PAREN -> {
                expect(TokenType.LEFT_PAREN, tokens)
                val expression = parseExpression(tokens = tokens)
                expect(TokenType.RIGHT_PAREN, tokens)
                return expression
            }
            else -> {
                val nToken = tokens.removeFirst()
                throw UnexpectedTokenException(
                    expected =
                    "${TokenType.INT_LITERAL}, ${TokenType.IDENTIFIER}, unary operator, ${TokenType.LEFT_PAREN}",
                    actual = nToken.type.toString(),
                    line = nToken.line,
                    column = nToken.column
                )
            }
        }
    }
}
