package com.bmw.hmm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

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
        assertFalse(algorithm.isBroken());
    }

    @Test
    public void initialObservationIsIncludedInMostLikelySequence() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> candidates = Arrays.asList("A", "B");
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", -1.0);
        probabilities.put("B", -3.0);

        algorithm.startWithInitialObservation("first", candidates, probabilities);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(1, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("first", sequence.get(0).observation);
        assertNull(sequence.get(0).transitionDescriptor);
    }

    @Test
    public void nextStepSelectsMostLikelyPathAndStoresTransitionDescriptor() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> initialCandidates = Arrays.asList("A", "B");
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", -1.0);
        initialProbabilities.put("B", -2.0);
        algorithm.startWithInitialObservation("first", initialCandidates, initialProbabilities);

        List<String> nextCandidates = Arrays.asList("C", "D");
        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("C", -1.0);
        emissions.put("D", -1.0);

        Map<Transition<String>, Double> transitions = new LinkedHashMap<>();
        transitions.put(new Transition<>("A", "C"), -5.0);
        transitions.put(new Transition<>("B", "C"), -1.0);
        transitions.put(new Transition<>("A", "D"), -1.0);
        transitions.put(new Transition<>("B", "D"), -5.0);

        Map<Transition<String>, String> descriptors = new LinkedHashMap<>();
        descriptors.put(new Transition<>("B", "C"), "B-to-C");
        descriptors.put(new Transition<>("A", "D"), "A-to-D");

        algorithm.nextStep("second", nextCandidates, emissions, transitions, descriptors);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("first", sequence.get(0).observation);
        assertEquals("D", sequence.get(1).state);
        assertEquals("second", sequence.get(1).observation);
        assertEquals("A-to-D", sequence.get(1).transitionDescriptor);
        assertFalse(algorithm.isBroken());
    }

    @Test
    public void missingTransitionsCauseBreakAndKeepPreviousSequence() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        List<String> initialCandidates = Arrays.asList("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(initialCandidates, initialProbabilities);

        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("B", 0.0);
        algorithm.nextStep("next", Arrays.asList("B"), emissions,
                new LinkedHashMap<Transition<String>, Double>());

        assertTrue(algorithm.isBroken());
        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(1, sequence.size());
        assertEquals("A", sequence.get(0).state);
    }

    @Test
    public void allNegativeInfinityInitialProbabilitiesBreakImmediately() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", Double.NEGATIVE_INFINITY);

        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), probabilities);

        assertTrue(algorithm.isBroken());
        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
    }

    @Test
    public void messageHistoryRecordsInitialAndNextMessages() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>(true);
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", -2.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initialProbabilities);

        Map<String, Double> emissions = new LinkedHashMap<>();
        emissions.put("B", -3.0);
        Map<Transition<String>, Double> transitions = new LinkedHashMap<>();
        transitions.put(new Transition<>("A", "B"), -1.0);
        algorithm.nextStep("next", Arrays.asList("B"), emissions, transitions);

        List<Map<String, Double>> history = algorithm.messageHistory();
        assertEquals(2, history.size());
        assertEquals(Double.valueOf(-2.0), history.get(0).get("A"));
        assertEquals(Double.valueOf(-6.0), history.get(1).get("B"));
        assertEquals("Message history with log probabilies\n\n"
                + "Time step 0\nA: -2.0\n\n"
                + "Time step 1\nB: -6.0\n\n", algorithm.messageHistoryString());
    }

    @Test
    public void messageHistoryIsNullWhenNotEnabled() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();

        assertNull(algorithm.messageHistory());
    }

    @Test
    public void messageHistoryStringRequiresHistoryToBeEnabled() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        try {
            algorithm.messageHistoryString();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Message history was not recorded.", expected.getMessage());
        }
    }

    @Test
    public void startingTwiceIsRejected() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), probabilities);

        try {
            algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), probabilities);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Initial probabilities have already been set.", expected.getMessage());
        }
    }

    @Test
    public void nextStepBeforeStartingIsRejected() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        try {
            algorithm.nextStep("next", new ArrayList<String>(),
                    new LinkedHashMap<String, Double>(),
                    new LinkedHashMap<Transition<String>, Double>());
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("must be called first"));
        }
    }

    @Test
    public void nextStepAfterBreakIsRejected() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", Double.NEGATIVE_INFINITY);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initialProbabilities);

        try {
            algorithm.nextStep("next", Arrays.asList("B"),
                    new LinkedHashMap<String, Double>(),
                    new LinkedHashMap<Transition<String>, Double>());
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Method must not be called after an HMM break.", expected.getMessage());
        }
    }

    @Test
    public void missingInitialProbabilityThrowsNullPointerException() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        try {
            algorithm.startWithInitialStateProbabilities(Arrays.asList("A"),
                    new LinkedHashMap<String, Double>());
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertEquals("No initial probability for A", expected.getMessage());
        }
    }

    @Test
    public void missingEmissionProbabilityThrowsNullPointerException() {
        ViterbiAlgorithm<String, String, String> algorithm = new ViterbiAlgorithm<>();
        Map<String, Double> initialProbabilities = new LinkedHashMap<>();
        initialProbabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initialProbabilities);

        Map<Transition<String>, Double> transitions = new LinkedHashMap<>();
        transitions.put(new Transition<>("A", "B"), 0.0);
        try {
            algorithm.nextStep("next", Arrays.asList("B"),
                    new LinkedHashMap<String, Double>(), transitions);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertTrue(true);
        }
    }
}
