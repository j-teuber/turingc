package de.jakobteuber.turingc

import de.jakobteuber.turingc.compilers.NsdToTollCompiler
import de.jakobteuber.turingc.compilers.NsdToTuringCompiler
import de.jakobteuber.turingc.compilers.TollToNsdCompiler
import de.jakobteuber.turingc.gui.TuringEditor
import tornadofx.App
import tornadofx.launch
import java.nio.file.Path
import javax.swing.JOptionPane

class TuringGui : App(TuringEditor::class)

fun main() {
    val arguments = JOptionPane.showInputDialog(
        null,
        "Syntax: -<command> <input-file>",
        "Your input was wrong! Try Again",
        JOptionPane.INFORMATION_MESSAGE
    ).trim().split(" ").toTypedArray()
    val command = arguments[0]
    val input = Path.of(arguments[1]).toAbsolutePath()
    when (command) {
        "-gui" -> launch<TuringGui>()
        "-tollToNsd" -> println(TollToNsdCompiler.compile(input))
        "-nsdToToll" -> println(NsdToTollCompiler.compile(input))
        "-nsdToTuring" -> println(NsdToTuringCompiler.compile(input))
        else -> System.err.println("wrong arguments")
    }
}