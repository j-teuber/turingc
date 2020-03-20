package de.jakobteuber.turingc.compilers

import org.antlr.v4.runtime.Token

sealed class CompilerException(token: Token, message: String) :
    RuntimeException("${token.text} (${token.line}:${token.charPositionInLine}): $message")

class TollException(token: Token, message: String) : CompilerException(token, message)
class NsdException(token: Token, message: String) : CompilerException(token, message)
class TuringException(token: Token, message: String) : CompilerException(token, message)
