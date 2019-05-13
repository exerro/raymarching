package lang

typealias ParserOutput<T, C> = List<ParseResult<T, C>>

open class ParserContext(val lexer: Lexer) {
    open fun <T: ParserContext> copyWithLexer(lexer: Lexer): T {
        @Suppress("UNCHECKED_CAST") // yeah this is a bit dodgy but there are no Self types
        return ParserContext(lexer) as T
    }
}

abstract class Parser<T, C: ParserContext> {
    abstract fun parse(context: C): List<ParseResult<T, C>>
}

data class ParseResult<A, C: ParserContext> internal constructor(val value: Pair<A, C>?, val e: ParseError?) {
    fun isSuccess(): Boolean = value != null
    fun isError(): Boolean = value == null
    fun getValue(): A = value!!.first
    fun getContext(): C = value!!.second
    fun getError(): ParseError = e!!
}

data class ParseError(val error: String, val position: Position, val causes: Set<ParseError>? = null) : Throwable() {
    fun getString(getSource: () -> TokenStream): String {
        return position.getErrorString(error, getSource()) + if (causes == null || causes.isEmpty()) "" else "\n  ... caused by:\n" + causes.joinToString("\n") {cause ->
            "\t" + cause.getShortString(getSource).replace("\n", "\n\t")
        }
    }

    private fun getShortString(getSource: () -> TokenStream): String {
        return position.getInfoOnlyErrorString(error, getSource()) + if (causes == null || causes.isEmpty()) "" else "\n  ... caused by:\n" + causes.joinToString("\n") {cause ->
            "\t" + cause.getString(getSource).replace("\n", "\n\t")
        }
    }
}

fun <T, R, C: ParserContext, CR: ParserContext> List<ParseResult<T, C>>.bind(func: (T, C) -> List<ParseResult<R, CR>>): List<ParseResult<R, CR>>
        = flatMap { result -> if (result.isSuccess()) func(result.getValue(), result.getContext()) else listOf(ParseResult(null, result.getError())) }

fun <T, C: ParserContext> List<ParseResult<T, C>>.filterErrors()
        = filter { it.isSuccess() } .ifEmpty { this }

fun <T, C: ParserContext> List<ParseResult<T, C>>.onError(func: (ParseError) -> ParseError): List<ParseResult<T, C>>
        = map { result -> if (result.isError()) ParseResult(null, func(result.getError())) else result }

fun <T, R, C: ParserContext> List<ParseResult<T, C>>.onSuccess(func: (T) -> R): List<ParseResult<R ,C>>
        = map { result -> if (result.isSuccess()) ParseResult(Pair(func(result.getValue()), result.getContext()), null) else ParseResult(null, result.getError()) }

fun <A, C: ParserContext> parseSuccess(a: A, context: C): List<ParseResult<A, C>>
        = listOf(ParseResult(Pair(a, context), null))

fun <A, C: ParserContext> parseFailure(error: String, position: Position, causes: Set<ParseError>? = null): List<ParseResult<A, C>> {
    return if (causes != null && causes.size == 1) {
        causes.map { ParseResult<A, C>(null, it) }
    }
    else {
        listOf(ParseResult(null, ParseError(error, position, causes)))
    }
}

fun <A, C: ParserContext> parseFailure(error: ParseError): List<ParseResult<A, C>> {
    return listOf(ParseResult(null, error))
}
