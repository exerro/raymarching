package lang

import java.io.BufferedInputStream
import java.io.File

abstract class TokenStream {
    private var position = Position(1, 1, 1, 1)
    private var nonWhitespacePosition = position

    abstract fun peekNextChar(): Char
    abstract fun readNextChar(): Char
    abstract fun isEOF(): Boolean

    fun hasNextToken(): Boolean {
        while (!isEOF()) {
            val next = peekNextChar()
            if (next == '\n') setCurrentPosition(Position(getCurrentPosition().line1 + 1, 1), false)
            if (next == ' ' || next == '\t') setCurrentPosition(getCurrentPosition().after(1), false)
            if (next != '\n' && next != ' ' && next != '\t') return true
            readNextChar()
        }
        return false
    }

    fun readToken(keywords: Set<String> = setOf()): Token {
        if (!hasNextToken())
            return Token(TokenType.EOF, "<EOF>", getCurrentPosition(false))

        val char = readNextChar()

        if (inRange(char, '0', '9')) {
            return parseFloatOrInteger(char)
        }
        else if (inRange(char, 'a', 'z') || inRange(char, 'A', 'Z') || char == '_') {
            return parseName(char, keywords)
        }
        else {
            val token = Token(TokenType.SYMBOL, char.toString(), getCurrentPosition())
            setCurrentPosition(getCurrentPosition().after(1))
            return token
        }
    }

    private fun setCurrentPosition(position: Position, token: Boolean = true) {
        this.position = position
        if (token) nonWhitespacePosition = position
    }

    private fun getCurrentPosition(token: Boolean = true): Position {
        return if (token) position else nonWhitespacePosition
    }

    private fun parseFloatOrInteger(first: Char): Token {
        val tokenText = StringBuilder()
        var tokenType = TokenType.INTEGER
        tokenText.append(first)

        while (!isEOF() && inRange(peekNextChar(), '0', '9'))
            tokenText.append(readNextChar())

        if (!isEOF() && peekNextChar() == '.') {
            tokenText.append(readNextChar())
            tokenType = TokenType.FLOAT

            while (!isEOF() && inRange(peekNextChar(), '0', '9'))
                tokenText.append(readNextChar())
        }

        val token = Token(tokenType, tokenText.toString(), getCurrentPosition().extendTo(tokenText.length))
        setCurrentPosition(token.position.after(1))
        return token
    }

    private fun parseName(first: Char, keywords: Set<String>): Token {
        val tokenText = StringBuilder()
        tokenText.append(first)

        while (!isEOF() && (inRange(peekNextChar(), '0', '9')
                        ||  inRange(peekNextChar(), 'a', 'z')
                        ||  inRange(peekNextChar(), 'a', 'Z')
                        ||  peekNextChar() == '_'
                        ||  peekNextChar() == '-'
                        ))
            tokenText.append(readNextChar())

        val token = Token(
                if (keywords.contains(tokenText.toString())) TokenType.KEYWORD else TokenType.NAME,
                tokenText.toString(),
                getCurrentPosition().extendTo(tokenText.length)
        )
        setCurrentPosition(token.position.after(1))
        return token
    }
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
