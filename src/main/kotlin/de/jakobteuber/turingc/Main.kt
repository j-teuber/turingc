package de.jakobteuber.turingc

import de.jakobteuber.turingc.compilers.NsdToTollCompiler
import de.jakobteuber.turingc.compilers.NsdToTuringCompiler
import de.jakobteuber.turingc.compilers.TollToNsdCompiler
import java.nio.file.Path
import javax.swing.JOptionPane


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
        "-tollToNsd" -> println(TollToNsdCompiler.compile(input))
        "-nsdToToll" -> println(NsdToTollCompiler.compile(input))
        "-nsdToTuring" -> println(NsdToTuringCompiler.compile(input))
        else -> System.err.println("wrong arguments")
    }
}