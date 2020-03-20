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


class NsdToTuringCompiler(private val labels: Map<String, Int>) : NsdBaseListener() {
    private val code = StringBuilder()
    private var currentState = 0
    private val variables = mutableMapOf<String, Int>()

    private fun registerVar(varName: String, token: Token) {
        if (variables.containsKey(varName)) {
            throw NsdException(token, "variable already exists")
        } else {
            variables[varName] = variables.size
        }
    }

    private fun getVarIndex(varName: String, token: Token): Int {
        return variables[varName] ?: throw NsdException(token, "variable not defined")
    }

    private fun getLabelIndex(label: String, token: Token): Int {
        return labels[label] ?: throw NsdException(token, "label is undefined")
    }

    private fun addCode(string: String) {
        code.append(string)
    }

    private fun moveToVar(varName: String, token: Token) {
        val varIndex = getVarIndex(varName, token)
        addCode(
            """
            # Gehe zum Anfang
            ((1 1 L +0)
             (0 0 L +0)
             (B B L +0)
             (* * R +1))
            # Gehe zur Variable $varName (index: $varIndex)
        """.trimIndent()
        )
        repeat(varIndex) {
            addCode(
                """
                ((1 1 R +0)
                 (0 0 R +0)
                 (B B R +1))
            """.trimIndent()
            )
        }
        addCode(
            """
            # Gehe zur letzetn Stelle
            ((1 1 R +0)
             (0 0 R +0)
             (B B L +1))
        """.trimIndent()
        )
        currentState += varIndex + 2
    }

    private fun addDigit(varName: String, token: Token) {
        moveToVar(varName, token)
        addCode(
            """
            # Gehe an den Anfang der Variable
            ((1 1 L +0)
             (0 0 L +0)
             (B B R +1)
             (* * R +1))
            # Füge führende Null hinzu
            ((1 0 R +1)
             (0 0 R +4)) # Keine Arbeit erforderlich
            # Schieben
            ((1 1 R +0)
             (0 1 R +1)
             (B 1 R +2))
            ((1 0 R +0)
             (0 0 R -1)
             (B 0 R +2))
            ((1 B R -1)
             (0 B R -2)
             (B B R +1))
        """.trimIndent()
        )
        currentState += 5
        moveToVar(varName, token)
    }

    override fun enterIncrement(ctx: NsdParser.IncrementContext) {
        addDigit(ctx.varName.text, ctx.varName)
        addCode(
            """
            # ==== ${ctx.humanizedText} ====
            ((1 1 L +0)
             (0 1 L +1))
        """.trimIndent()
        )
        currentState++
    }

    override fun enterDecrement(ctx: NsdParser.DecrementContext) {
        moveToVar(ctx.varName.text, ctx.varName)
        addCode(
            """
            # ==== ${ctx.humanizedText} ====
            ((1 0 L +1)
             (0 0 L +0)
             (B B L +1)
             (* * L +1))
        """.trimIndent()
        )
        currentState++
    }

    override fun enterLabel(ctx: NsdParser.LabelContext) {
        // Ignore
        // Labels are handled in the LabelVisitor on first pass.
    }

    override fun enterGoto(ctx: NsdParser.GotoContext) {
        moveToVar(ctx.varName.text, ctx.varName)
        currentState += 2
        val newState = getLabelIndex(ctx.label.text, ctx.label)
        val changeState = newState - currentState
        addCode(
            """
            # ==== ${ctx.humanizedText} ====
            # Sprung zum Label ${ctx.label.text} wenn ${ctx.varName.text} == 0
            ((0 0 L +0)
             (1 1 L +3)
             (B B R +1)
             (* * R +1))
            ((0 0 R 0)
             (1 1 R 0)
             (B B L $changeState))
        """.trimIndent()
        )
    }

    override fun enterInit(ctx: NsdParser.InitContext) {
        registerVar(ctx.varName.text, ctx.varName)
        addCode(
            """
            # ==== ${ctx.humanizedText} ====
            # Gehe ganz ans Ende
            ((0 0 R +0)
             (1 1 R +0)
             (B B R +1))
            ((0 0 R -1)
             (1 1 R -1)
             (B B R +1)
            ((B B L +1))
            # Fuege Variable ${ctx.varName.text} hinzu
            ((B 0 L +1))
        """.trimIndent()
        )
        currentState += 4
    }

    override fun enterReturn(ctx: NsdParser.ReturnContext) {
        moveToVar(ctx.varName.text, ctx.varName)
        addCode(
            """
            # ==== ${ctx.humanizedText} ====
            ((0 0 R +1)
             (1 1 R +1)
            # Alles hinter Variable ${ctx.varName.text} loeschen
            ((0 B R +0)
             (1 B R +0)
             (B B R +1))
            ((0 B R -1)
             (1 B R -1)
             (B B R +1))
            # Zurueck zur letzten Variablen
            ((B B L +0)
             (0 0 L +1)
             (1 1 L +1))
            # Gehe an den Anfang der Variablen
            ((0 0 L +0)
             (1 1 L +0)
             (B B L +1)
             (* * R H))
            # Alles vor Variable ${ctx.varName} loeschen
            ((0 B L +0)
             (1 B L +0)
             (B B L +0)
             (* * R +1))
            # Variable ${ctx.varName} an den Anfang schieben
            ((B B R +0)
             (1 1 R +1)
             (0 0 R +1))
            ((1 1 R +0)
             (0 0 R +0)
             (B B L +1))
            ((1 B L +1) # Zuletzt wurde ein B gelesen
             (0 B L +2))
            ((1 1 L +0) # Zuletzt wurde eine 1 gelesen
             (0 1 L +1)
             (B 1 R -3)
             (* * R +2))
            ((1 0 L -1) # Zuletzt wurde eine 0 gelesen
             (0 0 L +0)
             (B 0 R -4)
             (* * R +2))
            ((1 1 R +0)
             (0 1 R +1)
             (B 1 R H))
            ((0 0 R +0)
             (1 0 R -1)
             (B 0 R H))
        """.trimIndent()
        )
        currentState += 13
    }

    companion object {

        fun compile(code: String) = compile(CharStreams.fromString(code))
        fun compile(file: Path) = compile(CharStreams.fromPath(file))
        private fun compile(input: CharStream): String {
            val lexer = NsdLexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = NsdParser(tokens)
            val compiler = NsdToTuringCompiler(LabelVisitor.computeLabelTable(input))
            ParseTreeWalker.DEFAULT.walk(compiler, parser.programm())
            return compiler.code.toString()
        }

    }
}

private class LabelVisitor : NsdBaseListener() {
    private var currentState = 0
    private val labels = mutableMapOf<String, Int>()
    private val variables = mutableMapOf<String, Int>()

    private fun registerVar(varName: String, token: Token) {
        if (variables.containsKey(varName)) {
            throw NsdException(token, "variable already exists")
        } else {
            variables[varName] = currentState
        }
    }

    private fun registerLabel(label: String, token: Token) {
        if (labels.containsKey(label)) {
            throw NsdException(token, "label is already defined")
        }
        labels[label] = currentState
    }

    private fun getVarIndex(varName: String, token: Token): Int {
        return (variables[varName] ?: throw NsdException(token, "variable not defined")) + 2
    }

    override fun enterLabel(ctx: NsdParser.LabelContext) {
        registerLabel(ctx.label.text, ctx.label)
    }

    override fun enterIncrement(ctx: NsdParser.IncrementContext) {
        currentState += 6 + getVarIndex(ctx.varName.text, ctx.varName)
    }

    override fun enterDecrement(ctx: NsdParser.DecrementContext) {
        currentState += 1 + getVarIndex(ctx.varName.text, ctx.varName)
    }

    override fun enterInit(ctx: NsdParser.InitContext) {
        currentState += 4
        registerVar(ctx.varName.text, ctx.varName)
    }

    override fun enterGoto(ctx: NsdParser.GotoContext) {
        currentState += 2 + getVarIndex(ctx.varName.text, ctx.varName)
    }

    override fun enterReturn(ctx: NsdParser.ReturnContext) {
        currentState += 13 + getVarIndex(ctx.varName.text, ctx.varName)
    }

    companion object {
        fun computeLabelTable(input: CharStream): Map<String, Int> {
            val lexer = NsdLexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = NsdParser(tokens)
            val compiler = LabelVisitor()
            ParseTreeWalker.DEFAULT.walk(compiler, parser.programm())
            return compiler.labels
        }
    }
}
