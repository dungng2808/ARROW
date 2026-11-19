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
        assertEquals("B", sequence.get(0).state);
        assertNull(sequence.get(0).observation);
        assertNull(sequence.get(0).transitionDescriptor);
    }

    @Test
    public void initialObservationIsStoredInSequence() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", -0.5);

        algorithm.startWithInitialObservation("first", Arrays.asList("A"), probabilities);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(1, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("first", sequence.get(0).observation);
    }

    @Test
    public void nextStepSelectsMostLikelyPathAndStoresTransitionDescriptor() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        Map<String, Double> initial = new LinkedHashMap<>();
        initial.put("A", -0.1);
        initial.put("B", -0.2);
        algorithm.startWithInitialObservation("first", Arrays.asList("A", "B"), initial);

        Map<Transition<String>, Double> transitions = new LinkedHashMap<>();
        transitions.put(new Transition<>("A", "C"), -2.0);
        transitions.put(new Transition<>("B", "C"), -0.1);
        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("C", -0.3);
        Map<Transition<String>, String> descriptors = new LinkedHashMap<>();
        descriptors.put(new Transition<>("B", "C"), "B-to-C");

        algorithm.nextStep("second", Arrays.asList("C"), emissions, transitions, descriptors);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("B", sequence.get(0).state);
        assertEquals("C", sequence.get(1).state);
        assertEquals("second", sequence.get(1).observation);
        assertEquals("B-to-C", sequence.get(1).transitionDescriptor);
        assertFalse(algorithm.isBroken());
    }

    @Test
    public void missingTransitionsCauseBreakAndKeepPreviousSequence() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        Map<String, Double> initial = new LinkedHashMap<>();
        initial.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initial);

        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("B", 0.0);
        algorithm.nextStep("next", Arrays.asList("B"), emissions,
                new LinkedHashMap<Transition<String>, Double>());

        assertTrue(algorithm.isBroken());
        assertEquals(1, algorithm.computeMostLikelySequence().size());
    }

    @Test
    public void allNegativeInfinityInitialProbabilitiesBreakImmediately() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        Map<String, Double> initial = new LinkedHashMap<>();
        initial.put("A", Double.NEGATIVE_INFINITY);

        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initial);

        assertTrue(algorithm.isBroken());
        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
    }

    @Test
    public void messageHistoryRecordsInitialAndForwardMessages() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>(true);
        Map<String, Double> initial = new LinkedHashMap<>();
        initial.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initial);

        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("B", -0.5);
        Map<Transition<String>, Double> transitions = new LinkedHashMap<>();
        transitions.put(new Transition<>("A", "B"), -0.25);
        algorithm.nextStep("next", Arrays.asList("B"), emissions, transitions);

        List<Map<String, Double>> history = algorithm.messageHistory();
        assertEquals(2, history.size());
        assertEquals(Double.valueOf(-1.0), history.get(0).get("A"));
        assertEquals(Double.valueOf(-1.75), history.get(1).get("B"));
    }

    @Test
    public void messageHistoryStringFormatsRecordedMessages() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>(true);
        Map<String, Double> initial = new LinkedHashMap<>();
        initial.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initial);

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
        Map<String, Double> initial = new LinkedHashMap<>();
        initial.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initial);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initial);
    }

    @Test(expected = IllegalStateException.class)
    public void nextStepAfterBreakThrows() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        Map<String, Double> initial = new LinkedHashMap<>();
        initial.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initial);

        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("B", 0.0);
        algorithm.nextStep("next", Arrays.asList("B"), emissions,
                new LinkedHashMap<Transition<String>, Double>());
        algorithm.nextStep("later", Arrays.asList("B"), emissions,
                new LinkedHashMap<Transition<String>, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void messageHistoryStringWithoutRecordingThrows() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        algorithm.messageHistoryString();
    }
}
