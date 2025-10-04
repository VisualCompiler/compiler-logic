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
        val declarations = mutableListOf<FunctionDeclaration>()
        val startLine = 0
        val startColumn = 0
        while (tokens.isNotEmpty() && tokens.first().type != TokenType.EOF) {
            declarations.add(parseFunctionDeclaration(tokens))
        }
        val endLine = declarations.last().location.endLine
        val endColumn = declarations.last().location.endCol

        return SimpleProgram(
            functionDeclaration = declarations,
            location = SourceLocation(startLine, startColumn, endLine, endColumn)
        )
    }

    private fun parseFunctionDeclaration(tokens: MutableList<Token>): FunctionDeclaration {
        val func = expect(TokenType.KEYWORD_INT, tokens)
        val name = parseIdentifier(tokens)
        expect(TokenType.LEFT_PAREN, tokens)
        val params = mutableListOf<String>()
        if (tokens.firstOrNull()?.type == TokenType.KEYWORD_VOID) {
            tokens.removeFirst() // consume 'void'
        } else if (tokens.firstOrNull()?.type == TokenType.KEYWORD_INT) {
            // get params
            do {
                expect(TokenType.KEYWORD_INT, tokens)
                params.add(parseIdentifier(tokens))
            } while (tokens.firstOrNull()?.type == TokenType.COMMA && tokens.removeFirst().type == TokenType.COMMA)
        }
        // If neither void nor int, assume no parameters (empty parameter list)
        val endParan = expect(TokenType.RIGHT_PAREN, tokens)
        val body: Block?
        val endLine: Int
        val endColumn: Int
        if (tokens.firstOrNull()?.type == TokenType.LEFT_BRACK) {
            body = parseBlock(tokens)
            endLine = body.location.endLine
            endColumn = body.location.endCol
        } else {
            expect(TokenType.SEMICOLON, tokens)
            body = null
            endLine = endParan.endLine
            endColumn = endParan.endColumn
        }

        return FunctionDeclaration(name, params, body, SourceLocation(func.startLine, func.startColumn, endLine, endColumn))
    }

    private fun parseFunctionDeclarationFromBody(
        tokens: MutableList<Token>,
        name: String,
        location: SourceLocation
    ): FunctionDeclaration {
        expect(TokenType.LEFT_PAREN, tokens)
        val params = mutableListOf<String>()
        if (tokens.firstOrNull()?.type == TokenType.KEYWORD_VOID) {
            tokens.removeFirst() // consume 'void'
        } else if (tokens.firstOrNull()?.type == TokenType.KEYWORD_INT) {
            // get params
            do {
                expect(TokenType.KEYWORD_INT, tokens)
                params.add(parseIdentifier(tokens))
            } while (tokens.firstOrNull()?.type == TokenType.COMMA && tokens.removeFirst().type == TokenType.COMMA)
        }
        // If neither void nor int, assume no parameters (empty parameter list)
        val end = expect(TokenType.RIGHT_PAREN, tokens)
        val body: Block?
        val finalLocation: SourceLocation
        if (tokens.firstOrNull()?.type == TokenType.LEFT_BRACK) {
            // Parse function body, we will throw exception in semantic pass
            body = parseBlock(tokens)
            finalLocation = SourceLocation(location.startLine, location.startCol, body.location.endLine, body.location.endCol)
        } else {
            expect(TokenType.SEMICOLON, tokens)
            body = null
            finalLocation = SourceLocation(location.startLine, location.startCol, end.endLine, end.endColumn)
        }

        return FunctionDeclaration(name, params, body, finalLocation)
    }

    private fun parseBlock(tokens: MutableList<Token>): Block {
        val body = mutableListOf<BlockItem>()
        val start = expect(TokenType.LEFT_BRACK, tokens)
        while (tokens.firstOrNull()?.type != TokenType.RIGHT_BRACK) {
            body.add(parseBlockItem(tokens))
        }
        val end = expect(TokenType.RIGHT_BRACK, tokens)
        return Block(body, SourceLocation(start.startLine, start.startColumn, end.endLine, end.endColumn))
    }

    private fun parseBlockItem(tokens: MutableList<Token>): BlockItem =
        if (tokens.firstOrNull()?.type == TokenType.KEYWORD_INT) {
            val lookaheadTokens = tokens.toMutableList()
            val start = expect(TokenType.KEYWORD_INT, lookaheadTokens)
            val name = parseIdentifier(lookaheadTokens)

            if (lookaheadTokens.firstOrNull()?.type == TokenType.LEFT_PAREN) {
                expect(TokenType.KEYWORD_INT, tokens) // consume the int keyword
                val actualName = parseIdentifier(tokens)
                D(
                    FunDecl(
                        parseFunctionDeclarationFromBody(
                            tokens,
                            actualName,
                            SourceLocation(start.startLine, start.startColumn, start.endLine, start.endColumn)
                        )
                    )
                )
            } else {
                val end = expect(TokenType.KEYWORD_INT, tokens)
                val actualName = parseIdentifier(tokens)
                // check if end is right later
                val location = SourceLocation(start.startLine, start.startColumn, end.endLine, end.endColumn)
                D(VarDecl(parseVariableDeclaration(tokens, actualName, location)))
            }
        } else {
            S(parseStatement(tokens))
        }

    private fun parseVariableDeclaration(
        tokens: MutableList<Token>,
        name: String,
        location: SourceLocation
    ): VariableDeclaration {
        var init: Expression? = null
        if (tokens.firstOrNull()?.type == TokenType.ASSIGN) {
            tokens.removeFirst() // consume '='
            init = parseExpression(0, tokens)
        }
        val end = expect(TokenType.SEMICOLON, tokens)
        val finalLocation = SourceLocation(location.startLine, location.startCol, end.endLine, end.endColumn)
        return VariableDeclaration(name, init, finalLocation)
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
                line = token.startLine,
                column = token.startColumn
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
                line = token.startLine,
                column = token.startColumn
            )
        }

        return token.lexeme
    }

    private fun parseStatement(tokens: MutableList<Token>): Statement {
        val firstToken = tokens.firstOrNull() ?: throw UnexpectedEndOfFileException()
        val secondToken = if (tokens.size > 1) tokens[1] else null
        when (firstToken.type) {
            TokenType.IF -> {
                val ifToken = expect(TokenType.IF, tokens)
                expect(TokenType.LEFT_PAREN, tokens)
                val condition = parseExpression(tokens = tokens)
                expect(TokenType.RIGHT_PAREN, tokens)
                val thenStatement = parseStatement(tokens)
                var elseStatement: Statement? = null
                var endLine = thenStatement.location.endLine
                var endCol = thenStatement.location.endCol
                if (tokens.firstOrNull()?.type == TokenType.ELSE) {
                    tokens.removeFirst()
                    elseStatement = parseStatement(tokens)
                    endLine = elseStatement.location.endLine
                    endCol = elseStatement.location.endCol
                }
                return IfStatement(
                    condition,
                    thenStatement,
                    elseStatement,
                    SourceLocation(ifToken.startLine, ifToken.startColumn, endLine, endCol)
                )
            }
            TokenType.KEYWORD_RETURN -> {
                val returnToken = expect(TokenType.KEYWORD_RETURN, tokens)
                val expression = parseExpression(tokens = tokens)
                val semicolonToken = expect(TokenType.SEMICOLON, tokens)
                return ReturnStatement(
                    expression = expression,
                    SourceLocation(returnToken.startLine, returnToken.startColumn, semicolonToken.endLine, semicolonToken.endColumn)
                )
            }
            TokenType.GOTO -> {
                val gotoToken = expect(TokenType.GOTO, tokens)
                val label = parseIdentifier(tokens)
                val semicolonToken = expect(TokenType.SEMICOLON, tokens)
                return GotoStatement(
                    label,
                    SourceLocation(gotoToken.startLine, gotoToken.startColumn, semicolonToken.endLine, semicolonToken.endColumn)
                )
            }
            TokenType.IDENTIFIER -> {
                // Handle labeled statements: IDENTIFIER followed by COLON
                if (secondToken?.type == TokenType.COLON) {
                    val labelToken = expect(TokenType.IDENTIFIER, tokens)
                    val labelName = labelToken.lexeme
                    expect(TokenType.COLON, tokens)
                    val statement = parseStatement(tokens)
                    return LabeledStatement(
                        labelName,
                        statement,
                        SourceLocation(labelToken.startLine, labelToken.startColumn, statement.location.endLine, statement.location.endCol)
                    )
                } else {
                    // Not a label, parse as expression statement by delegating to default branch
                    val expression = parseOptionalExpression(tokens = tokens, followedByType = TokenType.SEMICOLON)
                    return if (expression !=
                        null
                    ) {
                        ExpressionStatement(expression, expression.location)
                    } else {
                        NullStatement(SourceLocation(0, 0, 0, 0))
                    }
                }
            }
            TokenType.KEYWORD_BREAK -> {
                val breakToken = expect(TokenType.KEYWORD_BREAK, tokens)
                val semicolonToken = expect(TokenType.SEMICOLON, tokens)
                return BreakStatement(
                    "",
                    SourceLocation(breakToken.startLine, breakToken.startColumn, semicolonToken.endLine, semicolonToken.endColumn)
                )
            }
            TokenType.KEYWORD_CONTINUE -> {
                val continueToken = expect(TokenType.KEYWORD_CONTINUE, tokens)
                val semicolonToken = expect(TokenType.SEMICOLON, tokens)
                return ContinueStatement(
                    "",
                    SourceLocation(continueToken.startLine, continueToken.startColumn, semicolonToken.endLine, semicolonToken.endColumn)
                )
            }
            TokenType.KEYWORD_WHILE -> {
                val whileToken = expect(TokenType.KEYWORD_WHILE, tokens)
                expect(TokenType.LEFT_PAREN, tokens)
                val condition = parseExpression(tokens = tokens)
                expect(TokenType.RIGHT_PAREN, tokens)
                val body = parseStatement(tokens)
                return WhileStatement(
                    condition,
                    body,
                    "",
                    SourceLocation(whileToken.startLine, whileToken.startColumn, body.location.endLine, body.location.endCol)
                )
            }
            TokenType.KEYWORD_DO -> {
                val doToken = expect(TokenType.KEYWORD_DO, tokens)
                val body = parseStatement(tokens)
                expect(TokenType.KEYWORD_WHILE, tokens)
                expect(TokenType.LEFT_PAREN, tokens)
                val condition = parseExpression(tokens = tokens)
                expect(TokenType.RIGHT_PAREN, tokens)
                val semicolonToken = expect(TokenType.SEMICOLON, tokens)
                return DoWhileStatement(
                    condition,
                    body,
                    "",
                    SourceLocation(doToken.startLine, doToken.startColumn, semicolonToken.endLine, semicolonToken.endColumn)
                )
            }
            TokenType.KEYWORD_FOR -> {
                val forToken = expect(TokenType.KEYWORD_FOR, tokens)
                expect(TokenType.LEFT_PAREN, tokens)
                val init = parseForInit(tokens)
                val condition = parseOptionalExpression(tokens = tokens, followedByType = TokenType.SEMICOLON)
                val post = parseOptionalExpression(tokens = tokens, followedByType = TokenType.RIGHT_PAREN)
                val body = parseStatement(tokens)
                return ForStatement(
                    init = init,
                    condition = condition,
                    post = post,
                    body = body,
                    label = "",
                    SourceLocation(forToken.startLine, forToken.startColumn, body.location.endLine, body.location.endCol)
                )
            }
            TokenType.LEFT_BRACK -> {
                val body = parseBlock(tokens)
                return CompoundStatement(body, body.location)
            }
            else -> {
                val expression = parseOptionalExpression(tokens = tokens, followedByType = TokenType.SEMICOLON)
                return if (expression !=
                    null
                ) {
                    ExpressionStatement(expression, expression.location)
                } else {
                    NullStatement(SourceLocation(0, 0, 0, 0))
                }
            }
        }
    }

    private fun parseForInit(tokens: MutableList<Token>): ForInit {
        if (tokens.firstOrNull()?.type == TokenType.KEYWORD_INT) {
            val start = expect(TokenType.KEYWORD_INT, tokens)
            val name = parseIdentifier(tokens)
            val declaration =
                parseVariableDeclaration(tokens, name, SourceLocation(start.startLine, start.startColumn, start.endLine, start.endColumn))
            return InitDeclaration(
                declaration,
                SourceLocation(start.startLine, start.startColumn, declaration.location.endLine, declaration.location.endCol)
            )
        }
        val expression = parseOptionalExpression(tokens = tokens, followedByType = TokenType.SEMICOLON)
        return InitExpression(expression, expression?.location ?: SourceLocation(0, 0, 0, 0))
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
                        AssignmentExpression(
                            left,
                            right,
                            SourceLocation(left.location.startLine, left.location.startCol, right.location.endLine, right.location.endCol)
                        )
                    }
                    TokenType.QUESTION_MARK -> {
                        val thenExpression = parseExpression(tokens = tokens)
                        expect(TokenType.COLON, tokens)
                        val elseExpression = parseExpression(prec, tokens)
                        return ConditionalExpression(
                            left,
                            thenExpression,
                            elseExpression,
                            SourceLocation(
                                left.location.startLine,
                                left.location.startCol,
                                elseExpression.location.endLine,
                                elseExpression.location.endCol
                            )
                        )
                    }
                    else -> {
                        val right = parseExpression(prec + 1, tokens)
                        BinaryExpression(
                            left = left,
                            operator = op,
                            right = right,
                            SourceLocation(left.location.startLine, left.location.startCol, right.location.endLine, right.location.endCol)
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
                return IntExpression(
                    value = nextToken.lexeme.toInt(),
                    SourceLocation(nextToken.startLine, nextToken.startColumn, nextToken.endLine, nextToken.endColumn)
                )
            }
            TokenType.IDENTIFIER -> {
                nextToken = tokens.removeFirst()
                if (tokens.firstOrNull()?.type == TokenType.LEFT_PAREN) {
                    // function call
                    val leftParen = tokens.removeFirst() // consume '('
                    val args = mutableListOf<Expression>()
                    if (tokens.firstOrNull()?.type != TokenType.RIGHT_PAREN) {
                        do {
                            args.add(parseExpression(0, tokens))
                        } while (tokens.firstOrNull()?.type == TokenType.COMMA && tokens.removeFirst().type == TokenType.COMMA)
                    }
                    val rightParen = expect(TokenType.RIGHT_PAREN, tokens)
                    return FunctionCall(
                        nextToken.lexeme,
                        args,
                        SourceLocation(nextToken.startLine, nextToken.startColumn, rightParen.endLine, rightParen.endColumn)
                    )
                } else {
                    // It's a variable
                    return VariableExpression(
                        nextToken.lexeme,
                        SourceLocation(nextToken.startLine, nextToken.startColumn, nextToken.endLine, nextToken.endColumn)
                    )
                }
            }

            TokenType.TILDE, TokenType.NEGATION, TokenType.NOT -> {
                val operator = tokens.removeFirst()
                val factor = parseFactor(tokens)
                return UnaryExpression(
                    operator = operator,
                    expression = factor,
                    SourceLocation(operator.startLine, operator.startColumn, factor.location.endLine, factor.location.endCol)
                )
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
                    line = nToken.startLine,
                    column = nToken.startColumn
                )
            }
        }
    }
}
