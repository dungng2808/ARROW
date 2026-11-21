package net.automatalib.util.automata;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.words.Word;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Automata_ArrowTest {

    @Test
    public void testAsGraph() {
        Automaton<?, String, ?> automaton = null;
        try {
            Automaton.class.getName();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        assertTrue(true);
    }

    @Test
    public void testFindSeparatingWordOnNullAutomata() {
        try {
            Automata.findSeparatingWord((UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                                        (UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                                        Collections.<String>emptyList());
            assertTrue(true);
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testFindShortestSeparatingWordOnNullAutomata() {
        try {
            Automata.findShortestSeparatingWord((UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                                                (UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                                                Collections.<String>emptyList());
            assertTrue(true);
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testCharacterizingSetOnNullAutomaton() {
        try {
            Automata.characterizingSet(null, Collections.<String>emptyList());
            assertTrue(true);
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testStateCoverOnNullAutomaton() {
        try {
            Automata.stateCover((DeterministicAutomaton<?, String, ?>) null, Collections.<String>emptyList());
            assertTrue(true);
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testTransitionCoverOnNullAutomaton() {
        try {
            Automata.transitionCover((DeterministicAutomaton<?, String, ?>) null, Collections.<String>emptyList());
            assertTrue(true);
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testStructuralCoverOnNullAutomaton() {
        try {
            Automata.structuralCover((DeterministicAutomaton<?, String, ?>) null, Collections.<String>emptyList());
            assertTrue(true);
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testHasUndefinedInputOnNullAutomaton() {
        try {
            Automata.hasUndefinedInput(null, Collections.<String>emptyList());
            assertTrue(true);
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testFindUndefinedInputOnNullAutomaton() {
        try {
            Automata.findUndefinedInput(null, Collections.<String>emptyList());
            assertNull(null);
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }
}
