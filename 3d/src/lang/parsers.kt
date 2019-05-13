package lang

class Context(lexer: Lexer, val infixFunctions: Set<String>): ParserContext(lexer) {
    override fun <T : ParserContext> copyWithLexer(lexer: Lexer): T {
        @Suppress("UNCHECKED_CAST")
        return Context(lexer, infixFunctions) as T
    }
}

sealed class Expression: Positioned<Expression>() {
    override fun getValue(): Expression = this
}

sealed class PrimaryExpression: Expression()

data class NameExpression(val name: Positioned<String>): PrimaryExpression() {
    override fun getPosition(): Position = name.getPosition()
    override fun toString(): String = "${name.getValue()} @ " + getPosition().getPositionString()
}

data class NumberExpression(val num: Positioned<Float>): PrimaryExpression() {
    override fun getPosition(): Position = num.getPosition()
    override fun toString(): String = "${num.getValue()} @ " + getPosition().getPositionString()
}

data class UnaryMinus(val operatorPosition: Position, val expression: Expression): Expression() {
    override fun getPosition(): Position = operatorPosition.to(expression.getPosition())
    override fun toString(): String = "-($expression) @ " + getPosition().getPositionString()
}

data class ListExpression(val expressions: List<Expression>): Expression() {
    override fun getPosition(): Position = expressions[0].getPosition().to(expressions[expressions.size - 1].getPosition())
    override fun toString(): String = "[${expressions.joinToString(", ") { it.toString() }}] @ " + getPosition().getPositionString()
}

data class FunctionCall(val expression: Expression, val parameters: List<Expression>, val operatorPosition: Position): Expression() {
    override fun getPosition(): Position = expression.getPosition().to(operatorPosition)
    override fun toString(): String = "($expression)(${parameters.joinToString(", ") { it.toString() }}) @ " + getPosition().getPositionString()
}

sealed class BinaryOperator(val operator: String, val lvalue: Expression, val rvalue: Expression): Expression() {
    override fun getPosition(): Position = lvalue.getPosition().to(rvalue.getPosition())
    override fun toString(): String = "($lvalue) $operator ($rvalue) @ " + getPosition().getPositionString()
}

data class FunctionDeclaration(
        val infix: Boolean,
        val name: Positioned<String>,
        val parameters: List<Positioned<String>>,
        val value: Expression
)

val parentheses = Pair(
        Parsing.symbol<Context>("("),
        Parsing.symbol<Context>(")")
)

val param = Parsing.token<Context>(TokenType.NAME)
val paramList = (param sepBy Parsing.symbol(",")) surroundWith parentheses

fun primaryExpression(): Parser<Expression, Context>
        = Parsing.alternatives(
                Parsing.token<Context>(TokenType.NAME) bindAs { name ->
                        Parsing.value<Expression, Context>(NameExpression(name))
                },
                Parsing.token<Context>(TokenType.FLOAT) bindAs { num ->
                        Parsing.value<Expression, Context>(NumberExpression(num.mapValue { it.toFloat() }))
                },
                Parsing.token<Context>(TokenType.INTEGER) bindAs { num ->
                        Parsing.value<Expression, Context>(NumberExpression(num.mapValue { it.toFloat() }))
                },
                expression() surroundWith parentheses
        )

fun unaryLeftOperator(): Parser<Token, Context>
        = Parsing.alternatives(
                Parsing.symbol("-")
        )

fun functionCall(): Parser<Pair<List<Expression>, Position>, Context> =
        Parsing.symbol<Context>("(") then (
                (expression() sepBy Parsing.symbol(",")) pairedWith
                        (Parsing.symbol<Context>(")") map { it.getPosition() } )
        )

fun unaryExpression(): Parser<Expression, Context> =
        Parsing.list(unaryLeftOperator()) bindAs { operators ->
            primaryExpression() bindAs { expression ->
                Parsing.list(functionCall()) bindAs { calls ->
                    val call = calls.fold(expression) { e, c -> FunctionCall(e, c.first, c.second) }
                    val unm = operators.foldRight(call) { t, e -> UnaryMinus(t.getPosition(), e) }
                    Parsing.value<Expression, Context>(unm)
                }}}

fun infixOperator(): Parser<Expression, Context> =
        Parsing.token<Context>(TokenType.NAME) filterc { item, context ->
            if (context.infixFunctions.contains(item.text)) { null } else {
                ParseError("'${item.text}' is not an infix function", item.getPosition())
            }
        } map { NameExpression(it) }

fun infixExpression(): Parser<Expression, Context> =
        unaryExpression() bindAs { first ->
            Parsing.list((infixOperator() pairedWith unaryExpression())) bindAs { operators ->
                val call = operators.fold(first) { acc, op ->
                    FunctionCall(op.first, listOf(acc, op.second), op.second.getPosition())
                }
                Parsing.value<Expression, Context>(call)
        }}

fun binaryOperator(): Parser<String, Context> {
    fun op(o: String) = Parsing.symbol<Context>(o) map { it.getValue() }
    return op("==") or op("!=") or op(">=") or op("<=") or op(">") or op("<") or op("+") or op("-") or op("*") or op("/") or op("%") or op("^")
}

fun binaryExpression(): Parser<Expression, Context> =
        infixExpression() bindAs { first ->
            Parsing.list((binaryOperator() pairedWith infixExpression())) bindAs { operators ->
                Parsing.value<Expression, Context>(resolveOperators(first, operators))
            }
        }

fun expression(): Parser<Expression, Context>
        = (Parsing.defer { binaryExpression() } sepBy Parsing.symbol(",")) map { items ->
                if (items.size == 1) items[0] else ListExpression(items)
        }

fun functionDeclaration(): Parser<FunctionDeclaration, Context> =
        modifier<Context>("infix") bindAs { infix ->
            Parsing.token<Context>(TokenType.NAME) bindAs { name ->
                paramList.optional().firstOnly() bindAs { parameters ->
                    Parsing.symbol<Context>("=") then (
                        Parsing.defer { expression() followedBy Parsing.token(TokenType.EOF) } bindAs { value ->
                            val decl = FunctionDeclaration(infix, name, parameters ?: listOf(), value)
                            Parsing.value<FunctionDeclaration, Context>(decl)
                        })}}}

fun resolveOperators(first: Expression, operations: List<Pair<String, Expression>>): Expression {
    return first // TODO
}

fun <C: ParserContext> modifier(keyword: String): Parser<Boolean, C>
        = Parsing.keyword<C>(keyword).optional() map { it != null }
