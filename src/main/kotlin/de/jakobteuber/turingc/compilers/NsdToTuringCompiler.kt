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
    private val code = CodeBuilder(commentSign = "#")
    private var currentState = 0
    private val variables = mutableMapOf<String, Int>()

    private fun registerVar(varName: String, token: Token) {
        if (variables.containsKey(varName)) {
            throw CompilerException(token, "variable already exists")
        } else {
            variables[varName] = variables.size
        }
    }

    private fun getVarIndex(varName: String, token: Token): Int {
        return variables[varName] ?: throw CompilerException(token, "variable not defined")
    }

    private fun getLabelIndex(label: String, token: Token): Int {
        return labels[label] ?: throw CompilerException(token, "label is undefined")
    }

    private fun moveToVar(varName: String, token: Token) {
        val varIndex = getVarIndex(varName, token)
        code.comment("Gehe zur Variablen $varName (index: $varIndex)") {
            comment("Gehe zum Anfang des Bandes") {
                code(
                    """
                    (
                    (1 1 L +0)
                    (0 0 L +0)
                    (B B L +0)
                    (* * R +1)
                    )
                """
                )
            }
            comment("Zum Index $varIndex überspringe die Variable 0 bis ${varIndex - 1}") {
                repeat(varIndex) {
                    code(
                        """
                        (
                        (1 1 R +0)
                        (0 0 R +0)
                        (B B R +1)
                        )
                    """
                    )
                }
            }
            comment("Gehe zur letzten Stelle der Variablen") {
                code(
                    """
                    (
                    (1 1 R +0)
                    (0 0 R +0)
                    (B B L +1)
                    )
                """
                )
            }
        }
        currentState += varIndex + 2
    }

    private fun addDigit(varName: String, token: Token) =
        code.comment("Füge eine führende Null bei der Variable $varName hinzu") {
            moveToVar(varName, token)
            comment("Gehe an den Anfang der Variable") {
                code(
                    """
                    (
                    (1 1 L +0)
                    (0 0 L +0)
                    (B B R +1)
                    (* * R +1)
                    )
                """
                )
            }
            comment("Füge führende Null hinzu") {
                code(
                    """
                    (
                    (1 0 R +1)
                    (0 0 R +4) # Keine Arbeit erforderlich
                    )
                """
                )
            }
            comment("Schiebe alle weiteren Stellen und Variable um 1 nach rechts") {
                code(
                    """
                    (
                    (1 1 R +0)
                    (0 1 R +1)
                    (B 1 R +2)
                    )
                    (
                    (1 0 R +0)
                    (0 0 R -1)
                    (B 0 R +2)
                    )
                    (
                    (1 B R -1)
                    (0 B R -2)
                    (B B R +1)
                    )
                """
                )
            }
            currentState += 5
            moveToVar(varName, token)
        }

    override fun enterParameter(ctx: NsdParser.ParameterContext) {
        registerVar(ctx.name.text, ctx.name)
    }

    override fun enterIncrement(ctx: NsdParser.IncrementContext) =
        code.comment(ctx.humanizedText) {
            addDigit(ctx.varName.text, ctx.varName)
            code(
                """
            (
            (1 1 L +0)
            (0 1 L +1)
            )
        """
            )
            currentState++
        }

    override fun enterDecrement(ctx: NsdParser.DecrementContext) =
        code.comment(ctx.humanizedText) {
            moveToVar(ctx.varName.text, ctx.varName)
            code(
                """
            (
            (1 0 L +1)
            (0 0 L +0)
            (B B L +1)
            (* * R +1)
            )
        """
            )
            currentState++
        }

    override fun enterLabel(ctx: NsdParser.LabelContext) {
        // Ignore
        // Labels are handled in the LabelVisitor on first pass.
        code.comment("Label Registered: ${ctx.label.text}")
    }

    override fun enterGoto(ctx: NsdParser.GotoContext) =
        code.comment(ctx.humanizedText) {
            moveToVar(ctx.varName.text, ctx.varName)
            currentState += 2
            val newState = getLabelIndex(ctx.label.text, ctx.label)
            val changeState = newState - currentState
            code.comment("Sprung zum Label ${ctx.label.text} wenn ${ctx.varName.text} == 0") {
                code(
                    """
                        (
                        (0 0 L +0)
                        (1 1 L +2)
                        (B B R +1)
                        (* * R +1)
                        )
                        (
                        (0 0 R +0)
                        (1 1 R +0)
                        (B B L $changeState)
                        )
                """
                )
            }
        }

    override fun enterInit(ctx: NsdParser.InitContext) =
        code.comment(ctx.humanizedText) {
            registerVar(ctx.varName.text, ctx.varName)
            comment("Gehe ganz ans Ende") {
                code(
                    """
                    (
                    (0 0 R +0)
                    (1 1 R +0)
                    (B B R +1)
                    )
                    (
                    (0 0 R -1)
                    (1 1 R -1)
                    (B B R +1)
                    )
                    (
                    (B B L +1)
                    )
                """
                )
            }
            comment("Fuege Variable ${ctx.varName.text} hinzu") {
                code(
                    """
                    (
                    (B 0 L +1)
                    )
                """
                )
            }
            currentState += 4
        }

    override fun enterReturn(ctx: NsdParser.ReturnContext) =
        code.comment(ctx.humanizedText) {
            moveToVar(ctx.varName.text, ctx.varName)
            comment("Gehe hinter die Variable") {
                code(
                    """
                    (
                    (0 0 R +1)
                    (1 1 R +1)
                    )
                """
                )
            }
            comment("Alles hinter Variable ${ctx.varName.text} loeschen") {
                code(
                    """
                    (
                    (0 B R +0)
                    (1 B R +0)
                    (B B R +1)
                    )
                    (
                    (0 B R -1)
                    (1 B R -1)
                    (B B R +1)
                    )
                """
                )
            }
            comment("Zurueck zur letzten Variablen") {
                code(
                    """
                    (
                    (B B L +0)
                    (0 0 L +1)
                    (1 1 L +1)
                    )
                """
                )
            }
            comment("Gehe an den Anfang der Variablen") {
                code(
                    """
                    (
                    (0 0 L +0)
                    (1 1 L +0)
                    (B B L +1)
                    (* * R H)
                    )
                """
                )
            }
            comment("Alles vor Variable ${ctx.varName} loeschen") {
                code(
                    """
                    (
                    (0 B L +0)
                    (1 B L +0)
                    (B B L +0)
                    (* * R +1)
                    )
                """
                )
            }
            comment("Variable ${ctx.varName} an den Anfang schieben") {
                code(
                    """
                    (
                    (B B R +0)
                    (1 1 R +1)
                    (0 0 R +1)
                    )
                    (
                    (1 1 R +0)
                    (0 0 R +0)
                    (B B L +1)
                    )
                    (
                    (1 B L +1) # Zuletzt wurde ein B gelesen
                    (0 B L +2)
                    )
                    (
                    (1 1 L +0) # Zuletzt wurde eine 1 gelesen
                    (0 1 L +1)
                    (B 1 R -3)
                    (* * R +2)
                    )
                    (
                    (1 0 L -1) # Zuletzt wurde eine 0 gelesen
                    (0 0 L +0)
                    (B 0 R -4)
                    (* * R +2)
                    )
                    (
                    (1 1 R +0)
                    (0 1 R +1)
                    (B 1 R H)
                    )
                    (
                    (0 0 R +0)
                    (1 0 R -1)
                    (B 0 R H)
                    )
                """
                )
            }
            currentState += 13
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
            val compiler = NsdToTuringCompiler(labelTable)
            ParseTreeWalker.DEFAULT.walk(compiler, parser.programm())
            return compiler.code.resultingCode()
        }

    }
}

private class LabelVisitor : NsdBaseListener() {
    private var currentState = 0
    private val labels = mutableMapOf<String, Int>()
    private val variables = mutableMapOf<String, Int>()

    private fun registerVar(varName: String, token: Token) {
        if (variables.containsKey(varName)) {
            throw CompilerException(token, "variable already exists")
        } else {
            variables[varName] = currentState
        }
    }

    private fun registerLabel(label: String, token: Token) {
        if (labels.containsKey(label)) {
            throw CompilerException(token, "label is already defined")
        }
        labels[label] = currentState
    }

    private fun getVarIndex(varName: String, token: Token): Int {
        return (variables[varName] ?: throw CompilerException(token, "variable not defined")) + 2
    }

    override fun enterParameter(ctx: NsdParser.ParameterContext) {
        registerVar(ctx.name.text, ctx.name)
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
            val labelVisitor = LabelVisitor()
            ParseTreeWalker.DEFAULT.walk(labelVisitor, parser.programm())
            return labelVisitor.labels
        }
    }
}
