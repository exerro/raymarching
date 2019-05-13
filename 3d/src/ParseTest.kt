import lang.*

class MyContext(lexer: Lexer, val multiplier: Int = 0): ParserContext(lexer) {
    override fun <T : ParserContext> copyWithLexer(lexer: Lexer): T {
        @Suppress("UNCHECKED_CAST")
        return MyContext(lexer, multiplier) as T
    }
}

object ParseTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val stream = getTokenStream()
        val lexer = Lexer(stream, setOf("infix"))

        if (true) {
//            val term = Parsing.keyword<Context>("infix").optional().firstOnly() then Parsing.token(TokenType.NAME)
//            (Parsing.list(term) followedBy Parsing.token(TokenType.EOF)).parse(Context(lexer, setOf("map")))
//                    .onSuccess { decls -> println(decls.joinToString(", ") { it.text }) }
//                    .onError { error ->
//                        println(error.getString { getTokenStream() })
//                        error
//                    }
            (Parsing.integer<Context>() sepBy Parsing.symbol("+") followedBy Parsing.token(TokenType.EOF))
                    .parse(Context(lexer, setOf("map")))
                    .filterErrors()
                    .onSuccess { decls -> println(decls.fold(0) { acc, i -> acc + i }) }
                    .onError { error ->
                        println(error.getString { getTokenStream() })
                        error
                    }
        }
        else {
            (functionDeclaration() map { listOf(it) } followedBy Parsing.token(TokenType.EOF)).parse(Context(lexer, setOf("map")))
                    .onSuccess { decls ->
                        decls.map { decl ->
                            //                    println(decl.joinToString(", ") { it.text })
                            println("${decl.infix} ${decl.name.getValue()} (${decl.parameters.joinToString(", ") { it.getValue() }}) = ${decl.value}")
                        }
                    }
                    .onError { error ->
                        println(error.getString { getTokenStream() })
                        error
                    }
        }
    }

    fun getTokenStream(): TokenStream = StringTokenStream(" 1 + 2 + 3 + 4 + ")

//    fun getTokenStream(): TokenStream = StringTokenStream("\n" +
//            "infix minus(a, b) = ----------------ShapeDifference(a, b)\n" +
//            "\n" +
//            "box_outline(size) = box(size) minus box(size * (1, 0.8, 0.8))\n" +
//            "                              minus box(size * (0.8, 1, 0.8))\n" +
//            "                              minus box(size * (0.8, 0.8, 1))\n" +
//            "\n" +
//            "main = (1 to 5) map { i ->\n" +
//            "    box_outline((i, i, i))\n" +
//            "}\n")
}
