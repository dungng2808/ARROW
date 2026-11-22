package com.miguelfonseca.completely.text.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
        EqualityAutomaton automaton = new EqualityAutomaton("cat");
        automaton = automaton.step('c');
        automaton = automaton.step('a');
        automaton = automaton.step('t');

        assertEquals(1.0, automaton.getScore(), 0.0);
        assertTrue(automaton.isWordAccepted());
        assertFalse(automaton.isWordRejected());
    }

    @Test
    public void partialMatchHasProportionalScore()
    {
        EqualityAutomaton automaton = new EqualityAutomaton("cat");
        automaton = automaton.step('c');
        automaton = automaton.step('a');

        assertEquals(2.0 / 3.0, automaton.getScore(), 0.0);
        assertFalse(automaton.isWordAccepted());
        assertFalse(automaton.isWordRejected());
    }

    @Test
    public void mismatchingSymbolIsRejected()
    {
        EqualityAutomaton automaton = new EqualityAutomaton("cat");
        automaton = automaton.step('x');

        assertEquals(0.0, automaton.getScore(), 0.0);
        assertFalse(automaton.isWordAccepted());
        assertTrue(automaton.isWordRejected());
    }

    @Test
    public void extraSymbolIsRejected()
    {
        EqualityAutomaton automaton = new EqualityAutomaton("a");
        automaton = automaton.step('a');
        automaton = automaton.step('a');

        assertEquals(1.0 / 2.0, automaton.getScore(), 0.0);
        assertFalse(automaton.isWordAccepted());
        assertTrue(automaton.isWordRejected());
    }

    @Test
    public void nullPatternThrowsNullPointerException()
    {
        try
        {
            new EqualityAutomaton(null);
        }
        catch (NullPointerException expected)
        {
            return;
        }
        throw new AssertionError("Expected NullPointerException");
    }
}
