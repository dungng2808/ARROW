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
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();

        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
        assertFalse(algorithm.isBroken());
        assertNull(algorithm.messageHistory());
    }

    @Test
    public void initialProbabilitiesProduceSingleStateSequence() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", -1.0);
        probabilities.put("B", -2.0);

        algorithm.startWithInitialStateProbabilities(Arrays.asList("A", "B"), probabilities);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertNull(sequence.get(0).observation);
        assertNull(sequence.get(0).transitionDescriptor);
        assertFalse(algorithm.isBroken());
    }

    @Test
    public void initialObservationIsIncludedInSequence() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", -0.5);

        algorithm.startWithInitialObservation("first", Arrays.asList("A"), probabilities);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(1, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("first", sequence.get(0).observation);
    }

    @Test
    public void nextStepSelectsMostLikelyPathAndStoresDescriptor() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        Map<String, Double> initial = new LinkedHashMap<String, Double>();
        initial.put("A", -0.1);
        initial.put("B", -0.2);
        algorithm.startWithInitialObservation("o1", Arrays.asList("A", "B"), initial);

        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("C", -0.1);
        Map<Transition<String>, Double> transitions =
                new LinkedHashMap<Transition<String>, Double>();
        transitions.put(new Transition<String>("A", "C"), -0.1);
        transitions.put(new Transition<String>("B", "C"), -2.0);
        Map<Transition<String>, String> descriptors =
                new LinkedHashMap<Transition<String>, String>();
        descriptors.put(new Transition<String>("A", "C"), "A-to-C");
        descriptors.put(new Transition<String>("B", "C"), "B-to-C");

        algorithm.nextStep("o2", Arrays.asList("C"), emissions, transitions, descriptors);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("o1", sequence.get(0).observation);
        assertEquals("C", sequence.get(1).state);
        assertEquals("o2", sequence.get(1).observation);
        assertEquals("A-to-C", sequence.get(1).transitionDescriptor);
    }

    @Test
    public void missingTransitionCausesBreakAndKeepsPreviousSequence() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        Map<String, Double> initial = new LinkedHashMap<String, Double>();
        initial.put("A", 0.0);
        algorithm.startWithInitialObservation("o1", Arrays.asList("A"), initial);

        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("B", 0.0);
        algorithm.nextStep("o2", Arrays.asList("B"), emissions,
                new LinkedHashMap<Transition<String>, Double>());

        assertTrue(algorithm.isBroken());
        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(1, sequence.size());
        assertEquals("A", sequence.get(0).state);
    }

    @Test
    public void allNegativeInfinityInitialProbabilitiesBreakImmediately() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", Double.NEGATIVE_INFINITY);

        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), probabilities);

        assertTrue(algorithm.isBroken());
        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
    }

    @Test
    public void messageHistoryRecordsInitialAndForwardMessages() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>(true);
        Map<String, Double> initial = new LinkedHashMap<String, Double>();
        initial.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initial);

        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("B", -2.0);
        Map<Transition<String>, Double> transitions =
                new LinkedHashMap<Transition<String>, Double>();
        transitions.put(new Transition<String>("A", "B"), -3.0);
        algorithm.nextStep("o2", Arrays.asList("B"), emissions, transitions);

        List<Map<String, Double>> history = algorithm.messageHistory();
        assertEquals(2, history.size());
        assertEquals(Double.valueOf(-1.0), history.get(0).get("A"));
        assertEquals(Double.valueOf(-6.0), history.get(1).get("B"));
        assertEquals("Message history with log probabilies\n\n"
                + "Time step 0\nA: -1.0\n\n"
                + "Time step 1\nB: -6.0\n\n", algorithm.messageHistoryString());
    }

    @Test
    public void messageHistoryStringThrowsWhenHistoryIsNotKept() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        try {
            algorithm.messageHistoryString();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Message history was not recorded.", expected.getMessage());
        }
    }

    @Test
    public void missingInitialProbabilityThrowsNullPointerException() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        try {
            algorithm.startWithInitialStateProbabilities(Arrays.asList("A"),
                    new LinkedHashMap<String, Double>());
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertEquals("No initial probability for A", expected.getMessage());
        }
    }

    @Test
    public void startingTwiceThrowsIllegalStateException() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
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
    public void nextStepBeforeInitializationThrowsIllegalStateException() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        try {
            algorithm.nextStep("o", new ArrayList<String>(),
                    new LinkedHashMap<String, Double>(),
                    new LinkedHashMap<Transition<String>, Double>());
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("must be called first"));
        }
    }

    @Test
    public void nextStepAfterBreakThrowsIllegalStateException() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        Map<String, Double> initial = new LinkedHashMap<String, Double>();
        initial.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initial);

        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("B", 0.0);
        algorithm.nextStep("o2", Arrays.asList("B"), emissions,
                new LinkedHashMap<Transition<String>, Double>());

        try {
            algorithm.nextStep("o3", Arrays.asList("C"),
                    new LinkedHashMap<String, Double>(),
                    new LinkedHashMap<Transition<String>, Double>());
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Method must not be called after an HMM break.", expected.getMessage());
        }
    }
}
