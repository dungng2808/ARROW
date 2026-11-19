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
    public void emptyAlgorithmReturnsEmptySequence() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();

        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
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
        assertEquals(1, sequence.size());
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
        probabilities.put("A", -1.0);

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
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A", "B"), initial);

        Map<Transition<String>, Double> transitions =
                new LinkedHashMap<Transition<String>, Double>();
        transitions.put(new Transition<String>("A", "C"), -2.0);
        transitions.put(new Transition<String>("B", "C"), -0.1);
        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("C", -0.3);
        Map<Transition<String>, String> descriptors =
                new LinkedHashMap<Transition<String>, String>();
        descriptors.put(new Transition<String>("B", "C"), "route");

        algorithm.nextStep("second", Arrays.asList("C"), emissions, transitions, descriptors);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("B", sequence.get(0).state);
        assertEquals("C", sequence.get(1).state);
        assertEquals("second", sequence.get(1).observation);
        assertEquals("route", sequence.get(1).transitionDescriptor);
        assertFalse(algorithm.isBroken());
    }

    @Test
    public void missingTransitionsCauseBreakAndPreservePreviousSequence() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        Map<String, Double> initial = new LinkedHashMap<String, Double>();
        initial.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initial);

        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
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
    public void messageHistoryRecordsInitialAndForwardMessages() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>(true);
        Map<String, Double> initial = new LinkedHashMap<String, Double>();
        initial.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initial);

        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("B", -0.5);
        Map<Transition<String>, Double> transitions =
                new LinkedHashMap<Transition<String>, Double>();
        transitions.put(new Transition<String>("A", "B"), -0.25);
        algorithm.nextStep("next", Arrays.asList("B"), emissions, transitions);

        assertEquals(2, algorithm.messageHistory().size());
        assertEquals(Double.valueOf(-1.0), algorithm.messageHistory().get(0).get("A"));
        assertEquals(Double.valueOf(-1.75), algorithm.messageHistory().get(1).get("B"));
        assertEquals("Message history with log probabilies\n\n"
                + "Time step 0\nA: -1.0\n\n"
                + "Time step 1\nB: -1.75\n\n", algorithm.messageHistoryString());
    }

    @Test
    public void messageHistoryIsNullWhenNotEnabled() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();

        assertNull(algorithm.messageHistory());
    }

    @Test(expected = IllegalStateException.class)
    public void nextStepBeforeInitializationIsRejected() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        algorithm.nextStep("next", new ArrayList<String>(),
                new LinkedHashMap<String, Double>(),
                new LinkedHashMap<Transition<String>, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void initializationCannotBeRepeated() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), probabilities);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), probabilities);
    }

    @Test(expected = NullPointerException.class)
    public void missingInitialProbabilityIsRejected() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"),
                new LinkedHashMap<String, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void messageHistoryStringRequiresHistoryRecording() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        algorithm.messageHistoryString();
    }

    @Test(expected = IllegalStateException.class)
    public void nextStepAfterBreakIsRejected() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        Map<String, Double> initial = new LinkedHashMap<String, Double>();
        initial.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(Arrays.asList("A"), initial);

        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("B", 0.0);
        algorithm.nextStep("break", Arrays.asList("B"), emissions,
                new LinkedHashMap<Transition<String>, Double>());
        algorithm.nextStep("again", Arrays.asList("B"), emissions,
                new LinkedHashMap<Transition<String>, Double>());
    }
}
