package com.miguelfonseca.completely.text.match;

import org.junit.Test;
import static org.junit.Assert.*;

public class EqualityAutomaton_ArrowTest {

    @Test
    public void testConstructor() {
        EqualityAutomaton automaton = new EqualityAutomaton("test");
        assertNotNull(automaton);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullPattern() {
        new EqualityAutomaton(null);
    }

    @Test
    public void testGetScoreEmptyStrings() {
        EqualityAutomaton automaton = new EqualityAutomaton("");
        assertEquals(1.0, automaton.getScore(), 0.0);
    }

    @Test
    public void testGetScoreEqualStrings() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        EqualityAutomaton stepped = automaton.step('a').step('b').step('c');
        assertEquals(1.0, stepped.getScore(), 0.0);
    }

    @Test
    public void testGetScoreDifferentLengths() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        EqualityAutomaton stepped = automaton.step('a');
        assertEquals(1.0/3.0, stepped.getScore(), 0.0);
    }

    @Test
    public void testIsWordAcceptedTrue() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        EqualityAutomaton stepped = automaton.step('a').step('b').step('c');
        assertTrue(stepped.isWordAccepted());
    }

    @Test
    public void testIsWordAcceptedFalse() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        assertFalse(automaton.isWordAccepted());
    }

    @Test
    public void testIsWordRejectedTrue() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        EqualityAutomaton stepped = automaton.step('x');
        assertTrue(stepped.isWordRejected());
    }

    @Test
    public void testIsWordRejectedFalse() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        EqualityAutomaton stepped = automaton.step('a').step('b').step('c');
        assertFalse(stepped.isWordRejected());
    }

    @Test
    public void testStepMatch() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        EqualityAutomaton stepped = automaton.step('a');
        assertEquals(1.0/3.0, stepped.getScore(), 0.0);
        assertEquals("a", stepped.word);
    }

    @Test
    public void testStepNoMatch() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        EqualityAutomaton stepped = automaton.step('x');
        assertEquals(0.0, stepped.getScore(), 0.0);
        assertEquals("x", stepped.word);
    }

    @Test
    public void testStepBeyondPattern() {
        EqualityAutomaton automaton = new EqualityAutomaton("ab");
        EqualityAutomaton stepped = automaton.step('a').step('b').step('c');
        assertEquals(2.0/3.0, stepped.getScore(), 0.0);
        assertEquals("abc", stepped.word);
    }
}
