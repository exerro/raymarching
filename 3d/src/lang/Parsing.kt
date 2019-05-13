package lang

/**
 * Hey future you!
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * You need to handle errors better
 * I think using the causes a lot more is an option, just keep every single error and filter at the end
 * firstOnly() is a bit of an issue but eh
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

import java.util.*

object Parsing {
    /**
     * Create a parser matching any token
     */
    fun <C: ParserContext> any(): Parser<Token, C> = wrapf { context ->
        val (token, lexer) = context.lexer.readToken()
        parseSuccess(token, context.copyWithLexer(lexer))
    }

    /**
     * Create a parser matching any token with the given type
     * @param type the type of tokens to match
     */
    fun <C: ParserContext> token(type: TokenType): Parser<Token, C> {
        return any<C>() filter { token ->
            if (token.type == type) { null } else {
                ParseError("Expecting ${type.name.toLowerCase()} token, got ${token.type.name.toLowerCase()} '${token.text}'", token.getPosition())
            }
        }
    }

    /**
     * Create a parser matching any integer token
     */
    fun <C: ParserContext> integer(): Parser<Int, C>
            = token<C>(TokenType.INTEGER) map { token -> token.text.toInt() }

    /**
     * Create a parser matching a specific keyword
     * @param keyword the symbol to match
     */
    fun <C: ParserContext> keyword(keyword: String): Parser<Token, C> {
        return any<C>() filter { token ->
            if (token.type == TokenType.KEYWORD && token.text == keyword) { null } else {
                ParseError("expected keyword '$keyword', got ${token.type.name.toLowerCase()} '${token.text}'", token.getPosition())
            }
        }
    }

    /**
     * Create a parser matching a specific symbol
     * @param symbol the symbol to match
     */
    fun <C: ParserContext> symbol(symbol: String): Parser<Token, C> {
        return any<C>() filter { token ->
            if (token.type == TokenType.SYMBOL && token.text == symbol) { null } else {
                ParseError("expected symbol '$symbol', got ${token.type.name.toLowerCase()} '${token.text}'", token.getPosition())
            }
        }
    }

    /**
     * Create a parser consuming no input and outputting the value given
     * @param value the value to output
     */
    fun <T, C: ParserContext> value(value: T): Parser<T, C>
            = wrapf { context -> parseSuccess(value, context) }

    /**
     * Create a parser matching 0 or more occurrences of the parser given, returning the list of results
     * @param parser the parser to match 0 or more occurrences of
     */
    fun <T, C: ParserContext> list(parser: Parser<T, C>): Parser<List<T>, C> = newList(parser)

    /**
     * Create a parser matching 0 or more occurrences of the parser given, returning the list of results
     * @param parser the parser to match 0 or more occurrences of
     */
    fun <T, C: ParserContext> newList(parser: Parser<T, C>): Parser<List<T>, C> = wrapf { context ->
            val results = parser.parse(context).bind { item, c1 ->
                newList(parser).parse(c1).bind { items, c2 ->
                    parseSuccess(cons(item, items), c2)
                }
            }
            if (results.none { it.isSuccess() }) {
                val r = ArrayList<ParseResult<List<T>, C>>()
                r.addAll(results)
                r.add(ParseResult(Pair(listOf(), context), null))
                r
            }
            else {
                results
            }
    }

    /**
     * Create a parser matching many of a set of alternatives
     *
     * Note this will return many matches if many do match, use .firstOnly() to narrow this down to one option
     * @param alternatives the parsers to match against
     */
    fun <T, C: ParserContext> alternatives(vararg alternatives: Parser<T, C>): Parser<T, C> = wrapf { context ->
        val results = alternatives.flatMap { alternative -> alternative.parse(context) } .filterErrors()

        if (results.isNotEmpty() && results.none { it.isSuccess() }) {
            parseFailure("no viable alternatives", context.lexer.position, results.map { it.getError() }.toSet())
        }
        else {
            results
        }
    }

    /**
     * Defer the evaluation of a parser, useful for cyclic references
     * @param parser a function returning the parser to use
     */
    fun <T, C: ParserContext> defer(parser: () -> Parser<T, C>): Parser<T, C>
            = wrapf { context -> parser().parse(context).take(1) }
}

/**
 * Create an optional version of this parser
 */
fun <T, C: ParserContext> Parser<T, C>.optional(): Parser<T?, C>
        = Parsing.alternatives(this map { it as T? }, Parsing.value(null as T?))

/**
 * Create a parser matching exactly one or less of its original matches
 */
fun <T, C: ParserContext> Parser<T, C>.firstOnly(): Parser<T, C>
        = wrapf { context -> val results = parse(context); results.filter { it.isSuccess() } .take(1) .ifEmpty { results } }

/**
 * Create a parser mapping the value of matches to a new value
 * @param map the function to update the value
 */
infix fun <A, B, C: ParserContext> Parser<A, C>.map(map: (A) -> B): Parser<B, C>
        = wrapf { context -> parse(context).onSuccess { item -> map(item) } }

/**
 * Create a parser mapping the value of matches to a new value, given some context information
 * @param map the function to update the value
 */
infix fun <A, B, C: ParserContext> Parser<A, C>.mapc(map: (A, C) -> B): Parser<B, C>
        = wrapf { context -> parse(context).bind { item, ctx -> parseSuccess(map(item, ctx), ctx) } }

/**
 * Create a parser updating the context when matching
 * @param update the function to update the context
 */
infix fun <T, C: ParserContext> Parser<T, C>.updateContext(update: (C) -> C): Parser<T, C>
        = wrapf { context -> parse(context).bind { item, ctx -> parseSuccess(item, update(ctx)) } }

/**
 * Create a parser matching only previous matches which also match the filter given
 * @param filter the function to filter matches - should return `null` to match, or an error otherwise
 */
infix fun <T, C: ParserContext> Parser<T, C>.filter(filter: (T) -> ParseError?): Parser<T, C>
        = wrapf { context -> parse(context).bind { item, c1 ->
    val error = filter(item)
    if (error == null) parseSuccess(item, c1) else parseFailure(error) } }

/**
 * Create a parser matching only previous matches which also match the filter given
 * @param filter the function to filter matches - should return `null` to match, or an error otherwise
 */
infix fun <T, C: ParserContext> Parser<T, C>.filterc(filter: (T, C) -> ParseError?): Parser<T, C>
        = wrapf { context -> parse(context).bind { item, ctx ->
    val error = filter(item, context)
    if (error == null) parseSuccess(item, ctx) else parseFailure(error) } }

/**
 * Create a parser matching the first then second parameter, then return the matches of the second
 */
infix fun <I, T, C: ParserContext> Parser<I, C>.then(next: Parser<T, C>): Parser<T, C>
        = wrapf { context -> parse(context).bind { _, ctx -> next.parse(ctx) } }

/**
 * Create a parser matching the first then second parameter, then return the matches of the first
 */
infix fun <A, B, C: ParserContext> Parser<A, C>.followedBy(next: Parser<B, C>): Parser<A, C>
        = wrapf { context -> parse(context).bind { a, c1 -> next.parse(c1).bind { _, c2 -> parseSuccess(a, c2) } } }

/**
 * Create a parser matching the first then second parameter, then return pairs of the matches of both
 */
infix fun <A, B, C: ParserContext> Parser<A, C>.pairedWith(other: Parser<B, C>): Parser<Pair<A, B>, C>
        = wrapf { context -> parse(context).bind { a, c1 ->
            other.parse(c1).bind { b, c2 -> parseSuccess(Pair(a, b), c2) } } }

/**
 * Create a parser matching the first parameter, binding that in the function given, then matching the second parameter
 */
infix fun <A, B, C: ParserContext> Parser<A, C>.bindAs(next: (A) -> Parser<B, C>): Parser<B, C>
        = wrapf { context -> parse(context).bind { item, ctx -> next(item).parse(ctx) } }

/**
 * Create a parser matching the first or the second parameter, matching the second only if the first doesn't match
 */
infix fun <T, C: ParserContext> Parser<T, C>.or(other: Parser<T, C>): Parser<T, C> {
    return wrapf { context: C ->
        val result = parse(context)
        if (result.none { it.isSuccess() }) {
            val otherResult = other.parse(context)
            otherResult .filter { it.isSuccess() } .ifEmpty {
                val combined = ArrayList<ParseResult<T, C>>()
                combined.addAll(result)
                combined.addAll(otherResult)
                combined
            }
        }
        else {
            result
        }
    }
}

/**
 * Create a parser matching a list of the first parameter, separated by the second
 *
 * Will match 1 or more occurrences
 */
infix fun <T, C: ParserContext> Parser<T, C>.sepBy(separator: Parser<*, C>)
        = (this pairedWith Parsing.list(separator then this)) bindAs { (a, b) -> Parsing.value<List<T>, C>(cons(a, b)) }

/**
 * Create a parser matching the first parameter, surrounded by the first and second items of the second parameter
 */
infix fun <T, A, B, C: ParserContext> Parser<T, C>.surroundWith(outer: Pair<Parser<A, C>, Parser<B, C>>): Parser<T, C>
        = outer.first then this followedBy outer.second

private fun <T> cons(item: T, items: List<T>): List<T> {
    val list = LinkedList(items)
    list.push(item)
    return list.toList()
}

internal fun <T, C: ParserContext> wrapf(func: (C) -> ParserOutput<T, C>): Parser<T, C> = object: Parser<T, C>() {
    override fun parse(context: C): ParserOutput<T, C> = func(context)
}
