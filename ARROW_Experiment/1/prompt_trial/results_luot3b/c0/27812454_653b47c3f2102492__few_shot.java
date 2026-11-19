package com.bmw.hmm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ViterbiAlgorithm_ArrowTest {

    @Test
    public void emptyAlgorithmReturnsEmptySequenceAndIsNotBroken() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();

        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
        assertFalse(algorithm.isBroken());
    }

    @Test
    public void initialProbabilitiesDetermineSingleStepSequence() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> states = Arrays.asList("A", "B");
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", -2.0);
        probabilities.put("B", -1.0);

        algorithm.startWithInitialStateProbabilities(states, probabilities);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(1, sequence.size());
        assertEquals("B", sequence.get(0).state);
        assertNull(sequence.get(0).observation);
        assertNull(sequence.get(0).transitionDescriptor);
        assertFalse(algorithm.isBroken());
    }

    @Test
    public void initialObservationIsIncludedInSequence() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> states = Arrays.asList("A", "B");
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", -1.0);
        probabilities.put("B", -3.0);

        algorithm.startWithInitialObservation("first", states, probabilities);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(1, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("first", sequence.get(0).observation);
    }

    @Test
    public void nextStepSelectsMostLikelyPathAndStoresTransitionDescriptor() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> initialStates = Arrays.asList("A", "B");
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", -0.1);
        initialProbabilities.put("B", -0.2);
        algorithm.startWithInitialObservation("first", initialStates, initialProbabilities);

        List<String> nextStates = Arrays.asList("C", "D");
        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("C", -0.1);
        emissions.put("D", -0.1);
        Map<Transition<String>, Double> transitions = new LinkedHashMap<>();
        transitions.put(new Transition<>("A", "C"), -0.1);
        transitions.put(new Transition<>("A", "D"), -2.0);
        transitions.put(new Transition<>("B", "C"), -2.0);
        transitions.put(new Transition<>("B", "D"), -0.1);
        Map<Transition<String>, String> descriptors = new LinkedHashMap<>();
        descriptors.put(new Transition<>("A", "C"), "A-C");
        descriptors.put(new Transition<>("A", "D"), "A-D");
        descriptors.put(new Transition<>("B", "C"), "B-C");
        descriptors.put(new Transition<>("B", "D"), "B-D");

        algorithm.nextStep("second", nextStates, emissions, transitions, descriptors);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("first", sequence.get(0).observation);
        assertEquals("C", sequence.get(1).state);
        assertEquals("second", sequence.get(1).observation);
        assertEquals("A-C", sequence.get(1).transitionDescriptor);
    }

    @Test
    public void messageHistoryRecordsInitialAndForwardMessages() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>(true);
        List<String> states = Arrays.asList("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", -0.5);
        algorithm.startWithInitialStateProbabilities(states, initialProbabilities);

        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("A", -0.25);
        Map<Transition<String>, Double> transitions = new LinkedHashMap<>();
        transitions.put(new Transition<>("A", "A"), -0.1);
        algorithm.nextStep("next", states, emissions, transitions);

        List<Map<String, Double>> history = algorithm.messageHistory();
        assertEquals(2, history.size());
        assertEquals(Double.valueOf(-0.5), history.get(0).get("A"));
        assertEquals(Double.valueOf(-0.85), history.get(1).get("A"));
    }

    @Test
    public void initialZeroProbabilityBreaksAlgorithm() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> states = Arrays.asList("A");
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", Double.NEGATIVE_INFINITY);

        algorithm.startWithInitialStateProbabilities(states, probabilities);

        assertTrue(algorithm.isBroken());
        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
    }

    @Test
    public void missingTransitionsBreakNextStepAndPreservePreviousSequence() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> initialStates = Arrays.asList("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", -0.2);
        algorithm.startWithInitialStateProbabilities(initialStates, initialProbabilities);

        List<String> nextStates = Arrays.asList("B");
        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("B", 0.0);
        algorithm.nextStep("next", nextStates, emissions,
                new LinkedHashMap<Transition<String>, Double>());

        assertTrue(algorithm.isBroken());
        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(1, sequence.size());
        assertEquals("A", sequence.get(0).state);
    }

    @Test(expected = IllegalStateException.class)
    public void nextStepBeforeInitializationThrows() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        algorithm.nextStep("observation", new ArrayList<String>(),
                new LinkedHashMap<String, Double>(),
                new LinkedHashMap<Transition<String>, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void startingTwiceThrows() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> states = Arrays.asList("A");
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(states, probabilities);
        algorithm.startWithInitialStateProbabilities(states, probabilities);
    }

    @Test(expected = NullPointerException.class)
    public void missingInitialProbabilityThrows() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"),
                new LinkedHashMap<String, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void messageHistoryStringThrowsWhenHistoryIsDisabled() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        algorithm.messageHistoryString();
    }
}
