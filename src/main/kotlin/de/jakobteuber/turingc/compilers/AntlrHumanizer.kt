package de.jakobteuber.turingc.compilers

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.Interval

val ParserRuleContext.humanizedText: String
    get() {
        if (start == null || stop == null || start.startIndex < 0 || stop.stopIndex < 0)
            return text // Fallback

        return start.inputStream.getText(
            Interval.of(start.startIndex, stop.stopIndex)
        ).replace(Regex("\\s+"), " ")
    }