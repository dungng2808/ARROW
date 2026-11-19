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
    public void emptyAlgorithmReturnsEmptySequence() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();

        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
    }

    @Test
    public void initialProbabilitiesDetermineSingleStepSequence() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> states = new ArrayList<String>();
        states.add("A");
        states.add("B");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
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
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> states = new ArrayList<String>();
        states.add("A");
        states.add("B");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
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
    public void nextStepSelectsBestPathAndRetainsTransitionDescriptor() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> initialStates = new ArrayList<String>();
        initialStates.add("A");
        initialStates.add("B");
        Map<String, Double> initialProbabilities = new LinkedHashMap<String, Double>();
        initialProbabilities.put("A", -1.0);
        initialProbabilities.put("B", -2.0);
        algorithm.startWithInitialStateProbabilities(initialStates, initialProbabilities);

        List<String> nextStates = new ArrayList<String>();
        nextStates.add("C");
        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("C", -0.5);
        Map<Transition<String>, Double> transitions =
                new LinkedHashMap<Transition<String>, Double>();
        transitions.put(new Transition<String>("A", "C"), -2.0);
        transitions.put(new Transition<String>("B", "C"), -0.1);
        Map<Transition<String>, String> descriptors =
                new LinkedHashMap<Transition<String>, String>();
        descriptors.put(new Transition<String>("A", "C"), "from-A");
        descriptors.put(new Transition<String>("B", "C"), "from-B");

        algorithm.nextStep("second", nextStates, emissions, transitions, descriptors);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("C", sequence.get(1).state);
        assertEquals("second", sequence.get(1).observation);
        assertEquals("from-A", sequence.get(1).transitionDescriptor);
    }

    @Test
    public void missingTransitionCausesBreakAndKeepsPreviousSequence() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> initialStates = new ArrayList<String>();
        initialStates.add("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<String, Double>();
        initialProbabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(initialStates, initialProbabilities);

        List<String> nextStates = new ArrayList<String>();
        nextStates.add("B");
        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("B", 0.0);

        algorithm.nextStep("next", nextStates, emissions,
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
        List<String> states = new ArrayList<String>();
        states.add("A");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", Double.NEGATIVE_INFINITY);

        algorithm.startWithInitialStateProbabilities(states, probabilities);

        assertTrue(algorithm.isBroken());
        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
    }

    @Test
    public void recordedHistoryContainsInitialAndForwardMessages() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>(true);
        List<String> initialStates = new ArrayList<String>();
        initialStates.add("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<String, Double>();
        initialProbabilities.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(initialStates, initialProbabilities);

        List<String> nextStates = new ArrayList<String>();
        nextStates.add("B");
        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("B", -0.5);
        Map<Transition<String>, Double> transitions =
                new LinkedHashMap<Transition<String>, Double>();
        transitions.put(new Transition<String>("A", "B"), -2.0);
        algorithm.nextStep("next", nextStates, emissions, transitions);

        assertEquals(2, algorithm.messageHistory().size());
        assertEquals(Double.valueOf(-1.0), algorithm.messageHistory().get(0).get("A"));
        assertEquals(Double.valueOf(-3.5), algorithm.messageHistory().get(1).get("B"));
    }

    @Test
    public void historyIsNullWhenNotEnabled() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();

        assertNull(algorithm.messageHistory());
    }

    @Test(expected = IllegalStateException.class)
    public void nextStepBeforeInitializationIsRejected() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        algorithm.nextStep("observation", new ArrayList<String>(),
                new LinkedHashMap<String, Double>(),
                new LinkedHashMap<Transition<String>, Double>());
    }

    @Test(expected = NullPointerException.class)
    public void missingInitialProbabilityIsRejected() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> states = new ArrayList<String>();
        states.add("A");

        algorithm.startWithInitialStateProbabilities(states,
                new LinkedHashMap<String, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void initializationCannotBeRepeated() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> states = new ArrayList<String>();
        states.add("A");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(states, probabilities);

        algorithm.startWithInitialStateProbabilities(states, probabilities);
    }

    @Test(expected = IllegalStateException.class)
    public void nextStepAfterBreakIsRejected() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> initialStates = new ArrayList<String>();
        initialStates.add("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<String, Double>();
        initialProbabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(initialStates, initialProbabilities);

        List<String> nextStates = new ArrayList<String>();
        nextStates.add("B");
        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("B", 0.0);
        algorithm.nextStep("next", nextStates, emissions,
                new LinkedHashMap<Transition<String>, Double>());
        algorithm.nextStep("again", nextStates, emissions,
                new LinkedHashMap<Transition<String>, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void historyStringRequiresHistoryRecording() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();

        algorithm.messageHistoryString();
    }

    @Test
    public void historyStringFormatsRecordedMessages() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>(true);
        List<String> states = new ArrayList<String>();
        states.add("A");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(states, probabilities);

        assertEquals("Message history with log probabilies\n\n"
                + "Time step 0\n"
                + "A: -1.0\n\n", algorithm.messageHistoryString());
    }
}
