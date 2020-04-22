package de.jakobteuber.turingc.compilers

import org.antlr.v4.runtime.Token

class CompilerException(token: Token, message: String) :
    RuntimeException("${token.text} (${token.line}:${token.charPositionInLine}): $message")
