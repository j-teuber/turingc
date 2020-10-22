package de.jakobteuber.turingc.compilers

import NsdBaseListener
import NsdLexer
import NsdParser
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.nio.file.Path


class NsdToTollCompiler(private val labelTable: Map<String, Int>) : NsdBaseListener() {
    private val code = CodeBuilder(commentSign = "//")
    private var tempLabelIndex = labelTable.size

    private fun registerTempLabel(): Int = tempLabelIndex++

    private fun getLabelIndex(label: String, token: Token): Int {
        return labelTable[label] ?: throw CompilerException(token, "label is undefined")
    }

    override fun enterParameterHeader(ctx: NsdParser.ParameterHeaderContext) =
        code.add {
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
            indent()
        }

    override fun enterInit(ctx: NsdParser.InitContext) =
        code.code("var ${ctx.varName.text} = 0;")

    override fun enterIncrement(ctx: NsdParser.IncrementContext) =
        code.code("${ctx.varName.text}++;")

    override fun enterDecrement(ctx: NsdParser.DecrementContext) =
        code.code("${ctx.varName.text}--;")

    override fun enterLabel(ctx: NsdParser.LabelContext) =
        code.add {
            val labelIndex = getLabelIndex(ctx.label.text, ctx.label)

            code("label = $labelIndex;")
            dedent()
            code("}")
            code("$labelIndex -> {")
            indent()
        }

    override fun enterGoto(ctx: NsdParser.GotoContext) =
        code.add {
            val labelIndex = getLabelIndex(ctx.label.text, ctx.label)
            val elseIndex = registerTempLabel()

            code("if (${ctx.varName.text} == 0) {")
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
        code.add {
            code("}")
            dedent()
            code("};")
            dedent()
            code("};")
            dedent()
            code("return erg;")
        }

    companion object {
        fun compile(code: String) = compile(
            CharStreams.fromString(code),
            LabelVisitor.computeLabelTable(CharStreams.fromString(code))
        )

        fun compile(file: Path) = compile(
            CharStreams.fromPath(file),
            LabelVisitor.computeLabelTable(CharStreams.fromPath(file))
        )

        private fun compile(input: CharStream, labelTable: Map<String, Int>): String {
            val lexer = NsdLexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = NsdParser(tokens)
            val compiler = NsdToTollCompiler(labelTable)
            ParseTreeWalker.DEFAULT.walk(compiler, parser.programm())
            return compiler.code.resultingCode()
        }

    }

    private class LabelVisitor : NsdBaseListener() {
        val labels = mutableMapOf("" to 0)

        override fun enterLabel(ctx: NsdParser.LabelContext) {
            val label = ctx.label.text
            if (labels.containsKey(label)) throw CompilerException(ctx.label, "label is already defined")
            labels[label] = labels.size
        }

        companion object {
            fun computeLabelTable(input: CharStream): Map<String, Int> {
                val lexer = NsdLexer(input)
                val tokens = CommonTokenStream(lexer)
                val parser = NsdParser(tokens)
                val labelVisitor = LabelVisitor()
                ParseTreeWalker.DEFAULT.walk(labelVisitor, parser.programm())
                return labelVisitor.labels
            }
        }
    }
}