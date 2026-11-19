package com.bmw.hmm;

import java.util.ArrayList;
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
    public void computesMostLikelySequenceAndTransitionDescriptors() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> initialStates = new ArrayList<>();
        initialStates.add("A");
        initialStates.add("B");
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", -1.0);
        initialProbabilities.put("B", -2.0);
        algorithm.startWithInitialStateProbabilities(initialStates, initialProbabilities);

        List<String> candidates = new ArrayList<>();
        candidates.add("C");
        candidates.add("D");
        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("C", -0.1);
        emissions.put("D", -0.2);
        Map<Transition<String>, Double> transitions = new LinkedHashMap<>();
        transitions.put(new Transition<>("A", "C"), -0.1);
        transitions.put(new Transition<>("B", "C"), -5.0);
        transitions.put(new Transition<>("A", "D"), -3.0);
        transitions.put(new Transition<>("B", "D"), -0.1);
        Map<Transition<String>, String> descriptors = new LinkedHashMap<>();
        descriptors.put(new Transition<>("A", "C"), "A-C");
        descriptors.put(new Transition<>("B", "D"), "B-D");

        algorithm.nextStep("observation", candidates, emissions, transitions, descriptors);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertNull(sequence.get(0).transitionDescriptor);
        assertEquals("C", sequence.get(1).state);
        assertEquals("observation", sequence.get(1).observation);
        assertEquals("A-C", sequence.get(1).transitionDescriptor);
        assertFalse(algorithm.isBroken());
    }

    @Test
    public void returnsEmptySequenceBeforeInitialization() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
    }

    @Test
    public void recordsInitialAndSubsequentMessages() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>(true);
        List<String> initialStates = new ArrayList<>();
        initialStates.add("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(initialStates, initialProbabilities);

        List<String> candidates = new ArrayList<>();
        candidates.add("B");
        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("B", -0.5);
        Map<Transition<String>, Double> transitions = new LinkedHashMap<>();
        transitions.put(new Transition<>("A", "B"), -0.25);
        algorithm.nextStep("next", candidates, emissions, transitions);

        List<Map<String, Double>> history = algorithm.messageHistory();
        assertEquals(2, history.size());
        assertEquals(Double.valueOf(-1.0), history.get(0).get("A"));
        assertEquals(Double.valueOf(-1.75), history.get(1).get("B"));
    }

    @Test
    public void breaksWhenNoTransitionIsPossible() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> initialStates = new ArrayList<>();
        initialStates.add("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(initialStates, initialProbabilities);

        List<String> candidates = new ArrayList<>();
        candidates.add("B");
        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("B", 0.0);
        algorithm.nextStep("next", candidates, emissions,
                new LinkedHashMap<Transition<String>, Double>());

        assertTrue(algorithm.isBroken());
        assertEquals(1, algorithm.computeMostLikelySequence().size());
        assertEquals("A", algorithm.computeMostLikelySequence().get(0).state);
    }

    @Test(expected = IllegalStateException.class)
    public void cannotProcessStepBeforeInitialization() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        algorithm.nextStep("observation", new ArrayList<String>(),
                new LinkedHashMap<String, Double>(),
                new LinkedHashMap<Transition<String>, Double>());
    }

    @Test(expected = NullPointerException.class)
    public void requiresProbabilityForEveryInitialCandidate() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> candidates = new ArrayList<>();
        candidates.add("A");
        algorithm.startWithInitialStateProbabilities(candidates,
                new LinkedHashMap<String, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotInitializeMoreThanOnce() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> candidates = new ArrayList<>();
        candidates.add("A");
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(candidates, probabilities);
        algorithm.startWithInitialStateProbabilities(candidates, probabilities);
    }

    @Test
    public void messageHistoryIsNullWhenNotEnabled() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        assertNull(algorithm.messageHistory());
    }

    @Test(expected = IllegalStateException.class)
    public void messageHistoryStringRequiresHistoryRecording() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        algorithm.messageHistoryString();
    }
}
