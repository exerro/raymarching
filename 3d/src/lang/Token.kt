package lang

data class Token(val type: TokenType, val text: String, private val pos: Position): Positioned<String>() {
    override fun getValue(): String = text
    override fun getPosition(): Position = pos
}

enum class TokenType {
    FLOAT,
    INTEGER,
    NAME,
    KEYWORD,
    SYMBOL,
    EOF
}