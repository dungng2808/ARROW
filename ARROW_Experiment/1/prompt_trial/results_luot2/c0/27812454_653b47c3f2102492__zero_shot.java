package com.bmw.hmm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class ViterbiAlgorithm_ArrowTest {

    @Test
    public void emptyAlgorithmHasEmptySequenceAndNoBreak() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<String, String, String>();

        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
        assertFalse(algorithm.isBroken());
        assertNull(algorithm.messageHistory());
    }

    @Test
    public void initialProbabilitiesProduceSingleStateSequence() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<String, String, String>();
        List<String> states = new ArrayList<String>();
        states.add("A");
        states.add("B");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", -2.0);
        probabilities.put("B", -1.0);

        algorithm.startWithInitialStateProbabilities(states, probabilities);

        List<SequenceState<String, String, String>> sequence = algorithm.computeMostLikelySequence();
        assertEquals(1, sequence.size());
        assertEquals("B", sequence.get(0).state);
        assertNull(sequence.get(0).observation);
        assertNull(sequence.get(0).transitionDescriptor);
        assertFalse(algorithm.isBroken());
    }

    @Test
    public void initialObservationIsIncludedInSequence() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<String, String, String>();
        List<String> states = new ArrayList<String>();
        states.add("A");
        states.add("B");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", -1.0);
        probabilities.put("B", -3.0);

        algorithm.startWithInitialObservation("first", states, probabilities);

        List<SequenceState<String, String, String>> sequence = algorithm.computeMostLikelySequence();
        assertEquals(1, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("first", sequence.get(0).observation);
    }

    @Test
    public void nextStepSelectsBestPathAndRetainsTransitionDescriptor() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<String, String, String>();
        List<String> initialStates = new ArrayList<String>();
        initialStates.add("A");
        initialStates.add("B");
        Map<String, Double> initial = new LinkedHashMap<String, Double>();
        initial.put("A", -1.0);
        initial.put("B", -2.0);
        algorithm.startWithInitialObservation("o1", initialStates, initial);

        List<String> nextStates = new ArrayList<String>();
        nextStates.add("C");
        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("C", -0.5);
        Map<Transition<String>, Double> transitions = new LinkedHashMap<Transition<String>, Double>();
        transitions.put(new Transition<String>("A", "C"), -2.0);
        transitions.put(new Transition<String>("B", "C"), -0.1);
        Map<Transition<String>, String> descriptors = new LinkedHashMap<Transition<String>, String>();
        descriptors.put(new Transition<String>("A", "C"), "from-A");
        descriptors.put(new Transition<String>("B", "C"), "from-B");

        algorithm.nextStep("o2", nextStates, emissions, transitions, descriptors);

        List<SequenceState<String, String, String>> sequence = algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("C", sequence.get(1).state);
        assertEquals("o2", sequence.get(1).observation);
        assertEquals("from-A", sequence.get(1).transitionDescriptor);
    }

    @Test
    public void messageHistoryRecordsInitialAndForwardMessages() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<String, String, String>(true);
        List<String> initialStates = new ArrayList<String>();
        initialStates.add("A");
        Map<String, Double> initial = new LinkedHashMap<String, Double>();
        initial.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(initialStates, initial);

        List<String> nextStates = new ArrayList<String>();
        nextStates.add("B");
        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("B", -0.5);
        Map<Transition<String>, Double> transitions = new LinkedHashMap<Transition<String>, Double>();
        transitions.put(new Transition<String>("A", "B"), -2.0);
        algorithm.nextStep(null, nextStates, emissions, transitions);

        List<Map<String, Double>> history = algorithm.messageHistory();
        assertEquals(2, history.size());
        assertEquals(Double.valueOf(-1.0), history.get(0).get("A"));
        assertEquals(Double.valueOf(-3.5), history.get(1).get("B"));
    }

    @Test
    public void messageHistoryStringFormatsRecordedMessages() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<String, String, String>(true);
        List<String> states = new ArrayList<String>();
        states.add("A");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(states, probabilities);

        assertEquals("Message history with log probabilies\n\n"
                + "Time step 0\nA: -1.0\n\n", algorithm.messageHistoryString());
    }

    @Test
    public void allNegativeInfinityInitialProbabilitiesCauseBreak() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<String, String, String>();
        List<String> states = new ArrayList<String>();
        states.add("A");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", Double.NEGATIVE_INFINITY);

        algorithm.startWithInitialStateProbabilities(states, probabilities);

        assertTrue(algorithm.isBroken());
        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotAdvanceBeforeInitialization() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<String, String, String>();
        algorithm.nextStep(null, new ArrayList<String>(), new LinkedHashMap<String, Double>(),
                new LinkedHashMap<Transition<String>, Double>());
    }

    @Test(expected = NullPointerException.class)
    public void missingInitialProbabilityThrows() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<String, String, String>();
        List<String> states = new ArrayList<String>();
        states.add("A");
        algorithm.startWithInitialStateProbabilities(states, new LinkedHashMap<String, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotInitializeTwice() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<String, String, String>();
        List<String> states = new ArrayList<String>();
        states.add("A");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(states, probabilities);
        algorithm.startWithInitialStateProbabilities(states, probabilities);
    }

    @Test(expected = IllegalStateException.class)
    public void messageHistoryStringRequiresHistoryRecording() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<String, String, String>();
        algorithm.messageHistoryString();
    }
}
