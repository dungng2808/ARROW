package com.bmw.hmm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
    public void initialProbabilitiesProduceSequenceInCandidateOrder() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> candidates = new ArrayList<String>();
        candidates.add("A");
        candidates.add("B");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", -2.0);
        probabilities.put("B", -1.0);

        algorithm.startWithInitialStateProbabilities(candidates, probabilities);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertNull(sequence.get(0).observation);
        assertNull(sequence.get(0).transitionDescriptor);
        assertEquals("B", sequence.get(1).state);
        assertNull(sequence.get(1).observation);
        assertNull(sequence.get(1).transitionDescriptor);
    }

    @Test
    public void initialObservationIsIncludedInSequence() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> candidates = new ArrayList<String>();
        candidates.add("A");
        candidates.add("B");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", -1.0);
        probabilities.put("B", -3.0);

        algorithm.startWithInitialObservation("first", candidates, probabilities);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("first", sequence.get(0).observation);
        assertEquals("B", sequence.get(1).state);
        assertEquals("first", sequence.get(1).observation);
    }

    @Test
    public void nextStepSelectsBestPathAndStoresTransitionDescriptor() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> initialCandidates = new ArrayList<String>();
        initialCandidates.add("A");
        initialCandidates.add("B");
        Map<String, Double> initialProbabilities = new LinkedHashMap<String, Double>();
        initialProbabilities.put("A", -1.0);
        initialProbabilities.put("B", -2.0);
        algorithm.startWithInitialObservation("o1", initialCandidates, initialProbabilities);

        List<String> nextCandidates = new ArrayList<String>();
        nextCandidates.add("C");
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

        algorithm.nextStep("o2", nextCandidates, emissions, transitions, descriptors);

        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(2, sequence.size());
        assertEquals("A", sequence.get(0).state);
        assertEquals("B", sequence.get(1).state);
        assertEquals("o2", sequence.get(1).observation);
        assertEquals("from-B", sequence.get(1).transitionDescriptor);
        assertFalse(algorithm.isBroken());
    }

    @Test
    public void missingTransitionCausesBreakAndKeepsPreviousSequence() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> initialCandidates = new ArrayList<String>();
        initialCandidates.add("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<String, Double>();
        initialProbabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(initialCandidates, initialProbabilities);

        List<String> nextCandidates = new ArrayList<String>();
        nextCandidates.add("B");
        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("B", 0.0);
        algorithm.nextStep("next", nextCandidates, emissions,
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
        List<String> candidates = new ArrayList<String>();
        candidates.add("A");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", Double.NEGATIVE_INFINITY);

        algorithm.startWithInitialStateProbabilities(candidates, probabilities);

        assertTrue(algorithm.isBroken());
        assertTrue(algorithm.computeMostLikelySequence().isEmpty());
    }

    @Test
    public void recordedHistoryContainsInitialAndNextMessages() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>(true);
        List<String> initialCandidates = new ArrayList<String>();
        initialCandidates.add("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<String, Double>();
        initialProbabilities.put("A", -1.0);
        algorithm.startWithInitialStateProbabilities(initialCandidates, initialProbabilities);

        List<String> nextCandidates = new ArrayList<String>();
        nextCandidates.add("B");
        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("B", -0.5);
        Map<Transition<String>, Double> transitions =
                new LinkedHashMap<Transition<String>, Double>();
        transitions.put(new Transition<String>("A", "B"), -2.0);
        algorithm.nextStep("next", nextCandidates, emissions, transitions);

        List<Map<String, Double>> history = algorithm.messageHistory();
        assertEquals(2, history.size());
        assertEquals(Double.valueOf(-1.0), history.get(0).get("A"));
        assertEquals(Double.valueOf(-3.5), history.get(1).get("B"));
        assertEquals("Message history with log probabilies\n\n"
                + "Time step 0\nA: -1.0\n\n"
                + "Time step 1\nB: -3.5\n\n", algorithm.messageHistoryString());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotAdvanceBeforeInitialization() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        algorithm.nextStep("next", new ArrayList<String>(),
                new LinkedHashMap<String, Double>(),
                new LinkedHashMap<Transition<String>, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotInitializeTwice() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> candidates = new ArrayList<String>();
        candidates.add("A");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(candidates, probabilities);
        algorithm.startWithInitialStateProbabilities(candidates, probabilities);
    }

    @Test(expected = NullPointerException.class)
    public void missingInitialProbabilityThrows() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> candidates = new ArrayList<String>();
        candidates.add("A");
        algorithm.startWithInitialStateProbabilities(candidates,
                new LinkedHashMap<String, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotAdvanceAfterBreak() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> initialCandidates = new ArrayList<String>();
        initialCandidates.add("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<String, Double>();
        initialProbabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(initialCandidates, initialProbabilities);

        List<String> nextCandidates = new ArrayList<String>();
        nextCandidates.add("B");
        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("B", 0.0);
        algorithm.nextStep("next", nextCandidates, emissions,
                new LinkedHashMap<Transition<String>, Double>());
        algorithm.nextStep("later", nextCandidates, emissions,
                new LinkedHashMap<Transition<String>, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void messageHistoryStringRequiresRecording() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        algorithm.messageHistoryString();
    }
}
