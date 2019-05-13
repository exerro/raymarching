package lang

import java.io.BufferedInputStream
import java.io.File

class Lexer(val stream: TokenStream, val keywords: Set<String>, var position: Position = Position(1, 1)) {
    lateinit var result: Pair<Token, Lexer>

    fun readToken(): Pair<Token, Lexer> {
        if (::result.isInitialized)
            return result

        while (!stream.isEOF()) {
            val next = stream.peekNextChar()
            if (next == '\n') position = Position(position.line1 + 1, 1)
            if (next == ' ' || next == '\t') position = position.after(1)
            if (next != '\n' && next != ' ' && next != '\t') break
            stream.readNextChar()
        }

        if (stream.isEOF()) {
            result = Pair(
                    Token(TokenType.EOF, "<EOF>", position),
                    this
            )
            return result
        }

        val char = stream.readNextChar()

        val token = if (inRange(char, '0', '9')) {
            val tokenText = StringBuilder()
            var tokenType = TokenType.INTEGER
            tokenText.append(char)

            while (!stream.isEOF() && inRange(stream.peekNextChar(), '0', '9'))
                tokenText.append(stream.readNextChar())

            if (!stream.isEOF() && stream.peekNextChar() == '.') {
                tokenText.append(stream.readNextChar())
                tokenType = TokenType.FLOAT

                while (!stream.isEOF() && inRange(stream.peekNextChar(), '0', '9'))
                    tokenText.append(stream.readNextChar())
            }

            val token = Token(tokenType, tokenText.toString(), position.extendTo(tokenText.length))
            position = token.getPosition().after(1)
            token
        }
        else if (inRange(char, 'a', 'z') || inRange(char, 'A', 'Z') || char == '_') {
            val tokenText = StringBuilder()
            tokenText.append(char)

            while (!stream.isEOF() && (inRange(stream.peekNextChar(), '0', '9')
                            ||  inRange(stream.peekNextChar(), 'a', 'z')
                            ||  inRange(stream.peekNextChar(), 'A', 'Z')
                            ||  stream.peekNextChar() == '_'
                            ||  stream.peekNextChar() == '-'
                            ))
                tokenText.append(stream.readNextChar())

            val token = Token(
                    if (keywords.contains(tokenText.toString())) TokenType.KEYWORD else TokenType.NAME,
                    tokenText.toString(),
                    position.extendTo(tokenText.length)
            )
            position = token.getPosition().after(1)
            token
        }
        else {
            val token = Token(TokenType.SYMBOL, char.toString(), position)
            position = position.after(1)
            token
        }

        result = Pair(token, Lexer(stream, keywords, position))

        return result
    }
}

abstract class TokenStream {
    abstract fun peekNextChar(): Char
    abstract fun readNextChar(): Char
    abstract fun isEOF(): Boolean
}

class StringTokenStream(val content: String): TokenStream() {
    private var position = 0

    override fun peekNextChar(): Char {
        if (position < content.length) {
            return content.toCharArray()[position]
        }
        else {
            error("failed to peek next character")
        }
    }

    override fun readNextChar(): Char {
        if (position < content.length) {
            return content.toCharArray()[position++]
        }
        else {
            error("failed to peek next character")
        }
    }

    override fun isEOF(): Boolean
            = position >= content.length
}

class FileTokenStream(val file: String): TokenStream() {
    private val inputStream = BufferedInputStream(File(file).inputStream())

    override fun peekNextChar(): Char {
        if (inputStream.available() != 0) {
            inputStream.mark(1)
            val char = inputStream.read().toChar()
            inputStream.reset()
            return char
        }
        else {
            error("failed to peek next character")
        }
    }

    override fun readNextChar(): Char
            = if (inputStream.available() != 0) inputStream.read().toChar() else error("failed to read next character")

    override fun isEOF(): Boolean
            = inputStream.available() == 0

}

private fun inRange(c: Char, min: Char, max: Char): Boolean = c in min .. max
