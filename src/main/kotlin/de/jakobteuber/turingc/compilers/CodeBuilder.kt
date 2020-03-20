package de.jakobteuber.turingc.compilers

class CodeBuilder(val comentSign: String) {
    private val code = StringBuilder()
    private var absoluteIndent = 0

    inline fun add(operation: CodeBuilder.() -> Unit) = operation()

    fun resultingCode() = code.toString()

    fun indent(relativeAmount: Int = 1) {
        absoluteIndent += relativeAmount
    }

    fun dedent(relativeAmount: Int = 1) = indent(-relativeAmount)

    fun code(text: String) = text.lines()
        .filter { text.isNotBlank() }
        .forEach { line ->
            repeat(absoluteIndent) { code.append("    ") }
            code.append(line.trim()).append('\n')
        }

    fun emptyLine() {
        code.append("\n")
    }

    fun comment(text: String) {
        emptyLine()
        code("$comentSign $text")
    }

    inline fun comment(text: String, scope: CodeBuilder.() -> Unit) {
        comment(text)
        indent()
        scope()
        dedent()
        emptyLine()
    }

    fun description(text: String) {
        val descriptionWidth = text.length + 4
        code.append("//")
        repeat(descriptionWidth) { code.append("*") }
        code.append("//").append("* ").append(text).append(" *")
        code.append("//")
        repeat(descriptionWidth) { code.append("*") }
    }
}