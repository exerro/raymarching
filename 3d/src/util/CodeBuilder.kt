package util

import java.lang.StringBuilder

sealed class CodeBuilder {
    internal val result = StringBuilder()
    open fun getText(): String = result.toString()
}

class BlockBuilder: CodeBuilder() {
    override fun getText(): String {
        var text = super.getText()
        while (text.endsWith("\n")) text = text.substring(0, text.length - 1)
        if (text == "") return "{}"
        return "{\n\t${text.replace("\n", "\n\t")}\n}"
    }
}

open class RootBuilder: CodeBuilder()

fun <T: CodeBuilder, I> T.ifEmpty(items: Collection<I>, func: (T) -> T): T
        = conditional(items.isEmpty(), func)

fun <T: CodeBuilder, I> T.foreach(items: Iterable<I>, func: (T, I) -> T): T
        = items.fold(this) { acc, i -> func(acc, i) }

fun <T: CodeBuilder> T.conditional(condition: Boolean, func: (T) -> T): T
        = if (condition) func(this) else this

fun <T: CodeBuilder, V> T.appendDefinition(type: String, name: String, value: V): T
        = appendStatement("$type $name = $value")

fun <T: CodeBuilder> T.appendDeclaration(type: String, name: String): T
        = appendStatement("$type $name")

fun <T: CodeBuilder> T.appendStatement(line: String): T
        = appendLine("$line;")

fun <T: CodeBuilder> T.appendLine(line: String = ""): T
        = append("$line\n")

fun <T: CodeBuilder> T.append(text: String): T {
    result.append(text)
    return this
}

fun <T: CodeBuilder> T.appendBlock(builder: (BlockBuilder) -> BlockBuilder): T
        = append(builder(BlockBuilder()).getText()).appendLine()

fun <T: BlockBuilder, String> T.appendIf(condition: String, builder: (BlockBuilder) -> BlockBuilder): T
        = append("if ($condition) ").appendBlock(builder)

fun <T: BlockBuilder, String> T.appendWhile(condition: String, builder: (BlockBuilder) -> BlockBuilder): T
        = append("while ($condition) ").appendBlock(builder)

fun <T: BlockBuilder, V> T.appendReturn(value: V): T
        = appendStatement("return $value")

fun <T: RootBuilder, V> T.appendConstant(type: String, name: String, value: V): T
        = append("const ").appendDefinition(type, name, value)

fun <T: RootBuilder> T.appendVertexParameter(type: String, name: String): T
        = append("in ").appendDeclaration(type, name)

fun <T: RootBuilder> T.appendFragmentParameter(location: Int, type: String, name: String): T
        = append("layout (location = $location) out ").appendDeclaration(type, name)

fun <T: RootBuilder> T.appendUniform(type: String, name: String): T
        = append("uniform ").appendDeclaration(type, name)

fun <T: RootBuilder> T.appendFunctionDeclaration(returns: String, name: String, vararg parameters: Pair<String, String>): T
        = appendStatement("$returns $name(${parameters.joinToString { (type, name) -> "$type $name" }}) ")

fun <T: RootBuilder> T.appendFunction(returns: String, name: String, vararg parameters: Pair<String, String>, builder: (BlockBuilder) -> BlockBuilder): T
        = append("$returns $name(${parameters.joinToString { (type, name) -> "$type $name" }}) ").appendBlock(builder)

fun <T: RootBuilder> T.appendStruct(name: String, builder: (BlockBuilder) -> BlockBuilder): T
        = append("struct $name ").appendBlock(builder).append(";")
