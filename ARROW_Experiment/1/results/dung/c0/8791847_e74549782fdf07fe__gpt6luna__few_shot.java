package net.automatalib.util.automata;

import java.util.ArrayList;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Automata_ArrowTest {

    @Test
    public void asGraphReturnsAutomatonTransitionGraphView() {
        Automaton<?, String, ?> automaton = null;
        try {
            Automata.asGraph(automaton, Collections.<String>emptyList());
        } catch (NullPointerException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void asUniversalGraphReturnsAutomatonTransitionGraphView() {
        UniversalDeterministicAutomaton<?, String, ?, ?, ?> automaton = null;
        try {
            Automata.asUniversalGraph(automaton, Collections.<String>emptyList());
        } catch (NullPointerException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void findSeparatingWordReturnsNullForNullAutomata() {
        assertNull(Automata.findSeparatingWord(
                (UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                (UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                Collections.<String>emptyList()));
    }

    @Test
    public void findShortestSeparatingWordReturnsNullForNullAutomata() {
        assertNull(Automata.findShortestSeparatingWord(
                (UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                (UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                Collections.<String>emptyList()));
    }

    @Test
    public void testEquivalenceReturnsTrueForNullAutomata() {
        assertTrue(Automata.testEquivalence(
                (UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                (UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                Collections.<String>emptyList()));
    }

    @Test
    public void characterizingSetIsEmptyForNullAutomaton() {
        assertEquals(Collections.emptyList(),
                     Automata.characterizingSet((UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                                                 Collections.<String>emptyList()));
    }

    @Test
    public void characterizingSetAddsNothingForNullAutomaton() {
        List<Word<String>> result = new ArrayList<>();
        Automata.characterizingSet((UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                                   Collections.<String>emptyList(),
                                   result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void stateCharacterizingSetIsEmptyForNullAutomaton() {
        assertEquals(Collections.emptyList(),
                     Automata.stateCharacterizingSet((UniversalDeterministicAutomaton<Object, String, ?, ?, ?>) null,
                                                     Collections.<String>emptyList(),
                                                     new Object()));
    }

    @Test
    public void stateCharacterizingSetAddsNothingForNullAutomaton() {
        List<Word<String>> result = new ArrayList<>();
        Automata.stateCharacterizingSet((UniversalDeterministicAutomaton<Object, String, ?, ?, ?>) null,
                                        Collections.<String>emptyList(),
                                        new Object(),
                                        result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void incrementalCharacterizingSetReturnsFalseForNullAutomaton() {
        assertFalse(Automata.incrementalCharacterizingSet(
                (UniversalDeterministicAutomaton<?, String, ?, ?, ?>) null,
                Collections.<String>emptyList(),
                Collections.<Word<String>>emptyList(),
                new ArrayList<Word<String>>()));
    }

    @Test
    public void stateCoverIsEmptyForNullAutomaton() {
        assertEquals(Collections.emptyList(),
                     Automata.stateCover((DeterministicAutomaton<?, String, ?>) null,
                                         Collections.<String>emptyList()));
    }

    @Test
    public void transitionCoverIsEmptyForNullAutomaton() {
        assertEquals(Collections.emptyList(),
                     Automata.transitionCover((DeterministicAutomaton<?, String, ?>) null,
                                              Collections.<String>emptyList()));
    }

    @Test
    public void structuralCoverIsEmptyForNullAutomaton() {
        assertEquals(Collections.emptyList(),
                     Automata.structuralCover((DeterministicAutomaton<?, String, ?>) null,
                                              Collections.<String>emptyList()));
    }

    @Test
    public void allDefinedInputsReturnsEmptyForNullAutomaton() {
        Iterable<Automata.TransRef<Object, String, ?>> result =
                Automata.allDefinedInputs((Automaton<Object, String, ?>) null, Arrays.asList("a"));
        assertFalse(result.iterator().hasNext());
    }

    @Test
    public void allDefinedInputsIteratorReturnsEmptyForNullAutomaton() {
        Iterator<Automata.TransRef<Object, String, ?>> result =
                Automata.allDefinedInputsIterator((Automaton<Object, String, ?>) null, Arrays.asList("a"));
        assertFalse(result.hasNext());
    }

    @Test
    public void allUndefinedInputsReturnsEmptyForNullAutomaton() {
        Iterable<Automata.TransRef<Object, String, ?>> result =
                Automata.allUndefinedInputs((Automaton<Object, String, ?>) null, Arrays.asList("a"));
        assertFalse(result.iterator().hasNext());
    }

    @Test
    public void allUndefinedInputsIteratorReturnsEmptyForNullAutomaton() {
        Iterator<Automata.TransRef<Object, String, ?>> result =
                Automata.allUndefinedInputsIterator((Automaton<Object, String, ?>) null, Arrays.asList("a"));
        assertFalse(result.hasNext());
    }

    @Test
    public void hasUndefinedInputReturnsFalseForNullAutomaton() {
        assertFalse(Automata.hasUndefinedInput((Automaton<?, String, ?>) null, Arrays.asList("a")));
    }

    @Test
    public void findUndefinedInputReturnsNullForNullAutomaton() {
        assertNull(Automata.findUndefinedInput((Automaton<Object, String, ?>) null, Arrays.asList("a")));
    }
}
