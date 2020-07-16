package de.jakobteuber.turingc.interpreters

import java.math.BigInteger

data class Argument(val value: BigInteger) {
    fun toTollArgument() = listOf(value)
    fun toNsdArgument() = listOf(value.signum().toBigInteger(), value.abs())
    fun toTuringArgument() = toNsdArgument()
}

data class Input(val arguments: List<Argument>) {
    constructor(vararg arguments: Argument) : this(arguments.asList())

    val tollInput by lazy { arguments.flatMap { it.toTollArgument() } }
    val nsdInput by lazy { arguments.flatMap { it.toNsdArgument() } }
    val turingInput by lazy { arguments.flatMap { it.toTuringArgument() } }
}

data class Output(val returnValue: Argument) {
    val tollOutput get() = returnValue.toTollArgument()
    val nsdOutput get() = returnValue.toNsdArgument()
    val turingOutput get() = returnValue.toTuringArgument()
}