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
    public void emptyAlgorithmHasEmptySequenceAndIsNotBroken() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();

        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
        assertFalse(algorithm.isBroken());
    }

    @Test
    public void initialProbabilitiesProduceSequenceInCandidateOrder() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> candidates = Arrays.asList("A", "B");
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", -2.0);
        probabilities.put("B", -1.0);

        algorithm.startWithInitialStateProbabilities(candidates, probabilities);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("B", sequence.get(1).state);
    }

    @Test
    public void initialObservationIsIncludedInSequence() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> candidates = Arrays.asList("A", "B");
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", -1.0);
        probabilities.put("B", -2.0);

        algorithm.startWithInitialObservation("first", candidates, probabilities);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("first", sequence.get(0).observation);
        assertNull(sequence.get(0).transitionDescriptor);
        assertEquals("B", sequence.get(1).state);
    }

    @Test
    public void nextStepSelectsMostLikelyPathAndStoresDescriptor() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> initialCandidates = Arrays.asList("A", "B");
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", -1.0);
        initialProbabilities.put("B", -2.0);
        algorithm.startWithInitialObservation("first", initialCandidates, initialProbabilities);

        List<String> nextCandidates = Arrays.asList("C", "D");
        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("C", -1.0);
        emissions.put("D", -0.5);
        Map<Transition<String>, Double> transitions = new LinkedHashMap<>();
        transitions.put(new Transition<>("A", "C"), -0.1);
        transitions.put(new Transition<>("A", "D"), -2.0);
        transitions.put(new Transition<>("B", "C"), -3.0);
        transitions.put(new Transition<>("B", "D"), -0.1);
        Map<Transition<String>, String> descriptors = new LinkedHashMap<>();
        descriptors.put(new Transition<>("A", "C"), "A-C");
        descriptors.put(new Transition<>("A", "D"), "A-D");
        descriptors.put(new Transition<>("B", "C"), "B-C");
        descriptors.put(new Transition<>("B", "D"), "B-D");

        algorithm.nextStep("second", nextCandidates, emissions, transitions, descriptors);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("C", sequence.get(1).state);
        assertEquals("second", sequence.get(1).observation);
        assertEquals("A-C", sequence.get(1).transitionDescriptor);
        assertFalse(algorithm.isBroken());
    }

    @Test
    public void initialZeroProbabilitiesCauseBreakAndEmptySequence() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> candidates = Arrays.asList("A", "B");
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", Double.NEGATIVE_INFINITY);
        probabilities.put("B", Double.NEGATIVE_INFINITY);

        algorithm.startWithInitialStateProbabilities(candidates, probabilities);

        assertTrue(algorithm.isBroken());
        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
    }

    @Test
    public void missingTransitionsCauseBreak() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> initialCandidates = Arrays.asList("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(initialCandidates, initialProbabilities);

        List<String> nextCandidates = Arrays.asList("B");
        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("B", 0.0);
        algorithm.nextStep("next", nextCandidates, emissions,
                new LinkedHashMap<Transition<String>, Double>());

        assertTrue(algorithm.isBroken());
        assertEquals(1, algorithm.computeMostLikelySequence().size());
    }

    @Test
    public void messageHistoryRecordsInitialAndNextMessages() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>(true);
        List<String> candidates = Arrays.asList("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(candidates, initialProbabilities);

        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("A", -2.0);
        Map<Transition<String>, Double> transitions = new LinkedHashMap<>();
        transitions.put(new Transition<>("A", "A"), -0.5);
        algorithm.nextStep("next", candidates, emissions, transitions);

        List<Map<String, Double>> history = algorithm.messageHistory();
        assertEquals(2, history.size());
        assertEquals(Double.valueOf(-1.0), history.get(0).get("A"));
        assertEquals(Double.valueOf(-3.5), history.get(1).get("A"));
    }

    @Test
    public void messageHistoryStringFormatsRecordedMessages() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>(true);
        List<String> candidates = Arrays.asList("A");
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(candidates, probabilities);

        assertEquals("Message history with log probabilies\n\n"
                + "Time step 0\nA: -1.0\n\n", algorithm.messageHistoryString());
    }

    @Test(expected = IllegalStateException.class)
    public void nextStepBeforeInitializationThrows() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        algorithm.nextStep("next", new ArrayList<String>(),
                new LinkedHashMap<String, Double>(),
                new LinkedHashMap<Transition<String>, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void startingTwiceThrows() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> candidates = Arrays.asList("A");
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(candidates, probabilities);
        algorithm.startWithInitialStateProbabilities(candidates, probabilities);
    }

    @Test(expected = NullPointerException.class)
    public void missingInitialProbabilityThrows() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"),
                new LinkedHashMap<String, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void nextStepAfterBreakThrows() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> candidates = Arrays.asList("A");
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", Double.NEGATIVE_INFINITY);
        algorithm.startWithInitialStateProbabilities(candidates, probabilities);
        algorithm.nextStep("next", candidates, probabilities,
                new LinkedHashMap<Transition<String>, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void messageHistoryStringWithoutHistoryThrows() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        algorithm.messageHistoryString();
    }
}
