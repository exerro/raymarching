package lang

import java.lang.StringBuilder

data class Position(val line1: Int, val char1: Int, val line2: Int = line1, val char2: Int = char1) {
    fun to(other: Position): Position = Position(line1, char1, other.line2, other.char2)
    fun extendTo(length: Int): Position = Position(line1, char1, line1, char1 + length - 1)
    fun after(chars: Int): Position = Position(line2, char2 + chars)

    fun getPositionString(): String {
        return if (line1 == line2) {
            if (char1 == char2) "[line $line1 char $char1]"
            else "[line $line1 char $char1 .. $char2]"
        }
        else "[line $line1 char $char1 .. line $line2 char $char2]"
    }

    fun getErrorString(error: String, source: TokenStream): String {
        return if (line1 == line2) {
            "${getPositionString()}: $error\n" +
                    "    ${getSourceLines(source).first}\n" +
                    "    ${rep(" ", char1 - 1)}${rep("^", char2 - char1 + 1)}"
        }
        else {
            val (l1, l2) = getSourceLines(source)
            "${getPositionString()}: $error\n" +
                    "    ${l1}\n" +
                    "    ${rep(" ", char1 - 1)}${rep("^", l1.length - char1 + 1)}...\n" +
                    "    ${l2}\n" +
                    "... ${rep("^", char2)}"
        }
    }
}

fun Position.getSourceLines(stream: TokenStream): Pair<String, String> {
    return if (line1 == line2) {
        val line = getLine(stream, line1)
        return Pair(line, line)
    }
    else {
        val l1 = getLine(stream, line1)
        val l2 = getLine(stream, line2 - line1)
        return Pair(l1, l2)
    }
}

fun Position.getSourceEndLine(stream: TokenStream): String = getLine(stream, line2)

private fun rep(str: String, count: Int): String = str.repeat(count)

private fun getLine(stream: TokenStream, line: Int): String {
    var l = line
    val result = StringBuilder()

    while (l > 1) {
        if (!stream.isEOF()) {
            if (stream.readNextChar() == '\n') {
                l--
            }
        }
        else {
            return ""
        }
    }

    while (!stream.isEOF()) {
        val char = stream.readNextChar()
        if (char == '\n') break
        else result.append(char)
    }

    return result.toString()
}
