package de.jakobteuber.turingc.compilers

import NsdBaseListener
import NsdLexer
import NsdParser
import org.antlr.v4.runtime.*
import java.nio.file.Path


class NsdToTollCompiler : NsdBaseListener() {
    private val codeBuilder = CodeBuilder("//")
    private val labels = mutableMapOf("#DEFAULT" to 0)

    private fun registerLabel(label: String, token: Token): Int {
        if (labels.containsKey(label)) throw NsdException(token, "label is already defined")
        val labelIndex = labels.size
        labels[label] = labelIndex
        return labelIndex
    }

    private fun registerTempLabel(): Int {
        var labelId = labels.size
        while ("else-$labelId" in labels) labelId++
        val labelIndex = labels.size
        labels["else-$labelId"] = labelIndex
        return labelIndex
    }

    private fun getLabelIndex(label: String, token: Token): Int {
        return labels[label] ?: throw NsdException(token, "label is undefined")
    }

    override fun enterParameterHeader(ctx: NsdParser.ParameterHeaderContext) =
        codeBuilder.add {
            code(ctx.humanizedText)
            emptyLine()
            code("var label = 0;")
            code("var halt = 0;")
            code("var erg = 0;")
            emptyLine()
            code("while (halt == 0) {")
            indent()
            code("when (label) {")
            indent()
            code("0 -> {")
        }

    override fun enterInit(ctx: NsdParser.InitContext) =
        codeBuilder.code("var ${ctx.varName} = 0;")

    override fun enterIncrement(ctx: NsdParser.IncrementContext) =
        codeBuilder.code("${ctx.varName}++;")

    override fun enterDecrement(ctx: NsdParser.DecrementContext) =
        codeBuilder.code("${ctx.varName}--;")

    override fun enterLabel(ctx: NsdParser.LabelContext) =
        codeBuilder.add {
            val labelIndex = registerLabel(ctx.label.text, ctx.label)

            code("label = $labelIndex;")
            dedent()
            code("}")
            code("$labelIndex -> {")
            indent()
        }

    override fun enterGoto(ctx: NsdParser.GotoContext) =
        codeBuilder.add {
            val labelIndex = getLabelIndex(ctx.label.text, ctx.label)
            val elseIndex = registerTempLabel()

            code("if (${ctx.varName} == 0) {")
            indent()
            code("label = $labelIndex;")
            dedent()
            code("} else {")
            indent()
            code("label = $elseIndex;")
            dedent()
            code("};")
            dedent()
            code("}")
            code("$elseIndex -> {")
            indent()
        }


    override fun exitProgramm(ctx: NsdParser.ProgrammContext?) =
        codeBuilder.add {
            code("};")
            dedent()
            code("};")
            code("return erg;")
        }

    companion object {
        fun compile(code: String) = compile(CharStreams.fromString(code))
        fun compile(file: Path) = compile(CharStreams.fromPath(file))

        private fun compile(code: CharStream): String {
            val lexer = NsdLexer(code)
            val tokens: TokenStream = CommonTokenStream(lexer)
            val parser = NsdParser(tokens)

            val listener = NsdToTollCompiler()
            parser.programm().enterRule(listener)
            return listener.codeBuilder.resultingCode()
        }
    }
}