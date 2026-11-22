package com.miguelfonseca.completely.text.match;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EqualityAutomaton_ArrowTest
{
    @Test
    public void emptyPatternIsAcceptedWithPerfectScore()
    {
        EqualityAutomaton automaton = new EqualityAutomaton("");

        assertEquals(1.0, automaton.getScore(), 0.0);
        assertTrue(automaton.isWordAccepted());
        assertFalse(automaton.isWordRejected());
    }

    @Test
    public void matchingWordIsAcceptedWithPerfectScore()
    {
        EqualityAutomaton automaton = new EqualityAutomaton("cat")
                .step('c')
                .step('a')
                .step('t');

        assertEquals(1.0, automaton.getScore(), 0.0);
        assertTrue(automaton.isWordAccepted());
        assertFalse(automaton.isWordRejected());
    }

    @Test
    public void partialMatchHasProportionalScore()
    {
        EqualityAutomaton automaton = new EqualityAutomaton("cat")
                .step('c')
                .step('a');

        assertEquals(2.0 / 3.0, automaton.getScore(), 0.0);
        assertFalse(automaton.isWordAccepted());
        assertFalse(automaton.isWordRejected());
    }

    @Test
    public void mismatchingSymbolIsRejected()
    {
        EqualityAutomaton automaton = new EqualityAutomaton("cat").step('x');

        assertEquals(0.0, automaton.getScore(), 0.0);
        assertFalse(automaton.isWordAccepted());
        assertTrue(automaton.isWordRejected());
    }

    @Test
    public void extraSymbolIsRejectedWithoutIncreasingMatchCount()
    {
        EqualityAutomaton automaton = new EqualityAutomaton("a")
                .step('a')
                .step('b');

        assertEquals(0.5, automaton.getScore(), 0.0);
        assertFalse(automaton.isWordAccepted());
        assertTrue(automaton.isWordRejected());
    }

    @Test
    public void emptyWordAgainstNonemptyPatternHasZeroScore()
    {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");

        assertEquals(0.0, automaton.getScore(), 0.0);
        assertFalse(automaton.isWordAccepted());
        assertFalse(automaton.isWordRejected());
    }
}
