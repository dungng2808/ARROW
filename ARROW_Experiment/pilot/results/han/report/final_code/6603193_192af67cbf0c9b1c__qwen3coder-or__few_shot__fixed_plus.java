package com.miguelfonseca.completely.text.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EqualityAutomaton_ArrowTest {

    @Test
    public void testConstructor() {
        EqualityAutomaton automaton = new EqualityAutomaton("test");
        assertEquals(0.0, automaton.getScore(), 0.0);
        assertFalse(automaton.isWordAccepted());
        assertFalse(automaton.isWordRejected());
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
        
        // Step through the automaton
        automaton = automaton.step('a');
        assertEquals(1.0/3.0, automaton.getScore(), 0.0);
        
        automaton = automaton.step('b');
        assertEquals(2.0/3.0, automaton.getScore(), 0.0);
        
        automaton = automaton.step('c');
        assertEquals(1.0, automaton.getScore(), 0.0);
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
    public void testIsWordAcceptedFalse() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        automaton = automaton.step('a');
        automaton = automaton.step('b');
        assertFalse(automaton.isWordAccepted());
    }

    @Test
    public void testIsWordRejectedTrue() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        automaton = automaton.step('a');
        automaton = automaton.step('b');
        automaton = automaton.step('d'); // mismatch
        assertTrue(automaton.isWordRejected());
    }

    @Test
    public void testIsWordRejectedFalse() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        automaton = automaton.step('a');
        automaton = automaton.step('b');
        automaton = automaton.step('c');
        assertFalse(automaton.isWordRejected());
    }

    @Test
    public void testStepMatch() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        EqualityAutomaton next = automaton.step('a');
        assertEquals(1.0/3.0, next.getScore(), 0.0);
        assertEquals("a", next.word);
    }

    @Test
    public void testStepNoMatch() {
        EqualityAutomaton automaton = new EqualityAutomaton("abc");
        EqualityAutomaton next = automaton.step('d');
        assertEquals(0.0, next.getScore(), 0.0);
        assertEquals("d", next.word);
    }

    @Test
    public void testStepBeyondPattern() {
        EqualityAutomaton automaton = new EqualityAutomaton("ab");
        automaton = automaton.step('a');
        automaton = automaton.step('b');
        EqualityAutomaton next = automaton.step('c');
        assertEquals(0.6666666666666666, next.getScore(), 0.0);
        assertEquals("abc", next.word);
    }
}
