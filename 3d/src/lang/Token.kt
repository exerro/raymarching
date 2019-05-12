package lang

data class Token(val type: TokenType, val value: String, val position: Position)

enum class TokenType {
    FLOAT,
    INTEGER,
    NAME,
    KEYWORD,
    SYMBOL,
    EOF
}