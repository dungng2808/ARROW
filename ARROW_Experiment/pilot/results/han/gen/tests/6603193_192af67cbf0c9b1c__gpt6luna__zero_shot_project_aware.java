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
    public void matchingWordIsAccepted()
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
    public void partialMatchingWordHasProportionalScore()
    {
        EqualityAutomaton automaton = new EqualityAutomaton("cat");
        automaton = automaton.step('c');
        automaton = automaton.step('a');

        assertEquals(2.0 / 3.0, automaton.getScore(), 0.0);
        assertFalse(automaton.isWordAccepted());
        assertFalse(automaton.isWordRejected());
    }

    @Test
    public void mismatchingSymbolIsRejectedWithoutIncreasingCount()
    {
        EqualityAutomaton automaton = new EqualityAutomaton("cat").step('x');

        assertEquals(0.0, automaton.getScore(), 0.0);
        assertFalse(automaton.isWordAccepted());
        assertTrue(automaton.isWordRejected());
    }

    @Test
    public void extraSymbolAfterMatchingPatternDoesNotIncreaseScore()
    {
        EqualityAutomaton automaton = new EqualityAutomaton("a").step('a').step('b');

        assertEquals(0.5, automaton.getScore(), 0.0);
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
