package com.miguelfonseca.completely.text.match;

import org.junit.Test;
import static org.junit.Assert.*;

public class EqualityAutomaton_ArrowTest {

    @Test
    public void testConstructor() {
        EqualityAutomaton automaton = new EqualityAutomaton("test");
        assertNotNull(automaton);
    }

    @Test
    public void testGetScoreEmptyPatternAndWord() {
        EqualityAutomaton automaton = new EqualityAutomaton("");
        assertEquals(1.0, automaton.getScore(), 0.0);
    }

    @Test
    public void testGetScoreWithPatternAndWord() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        assertEquals(0.0, automaton.getScore(), 0.0);
    }

    @Test
    public void testIsWordAcceptedFalse() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        assertFalse(automaton.isWordAccepted());
    }

    @Test
    public void testIsWordAcceptedTrue() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        automaton = automaton.step('a');
        automaton = automaton.step('b');
        automaton = automaton.step('c');
        assertTrue(automaton.isWordAccepted());
    }

    @Test
    public void testIsWordRejectedFalse() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        assertFalse(automaton.isWordRejected());
    }

    @Test
    public void testIsWordRejectedTrue() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        automaton = automaton.step('a');
        automaton = automaton.step('b');
        automaton = automaton.step('d');
        assertTrue(automaton.isWordRejected());
    }

    @Test
    public void testStepNoMatch() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        EqualityAutomaton next = automaton.step('x');
        assertEquals(0.0, next.getScore(), 0.0);
        assertFalse(next.isWordAccepted());
        assertTrue(next.isWordRejected());
    }

    @Test
    public void testStepMatch() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        EqualityAutomaton next = automaton.step('a');
        assertEquals(1.0 / 3.0, next.getScore(), 0.0);
        assertFalse(next.isWordAccepted());
        assertFalse(next.isWordRejected());
    }

    @Test
    public void testStepFullMatch() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        automaton = automaton.step('a');
        automaton = automaton.step('b');
        automaton = automaton.step('c');
        assertEquals(1.0, automaton.getScore(), 0.0);
        assertTrue(automaton.isWordAccepted());
        assertFalse(automaton.isWordRejected());
    }

    @Test
    public void testStepBeyondPattern() {
        EqualityAutomaton automaton = new EqualityAutomaton("ab");
        automaton = automaton.step('a');
        automaton = automaton.step('b');
        EqualityAutomaton next = automaton.step('c');
        assertEquals(0.6666666666666666, next.getScore(), 0.0);
        assertFalse(next.isWordAccepted());
        assertTrue(next.isWordRejected());
    }
}
