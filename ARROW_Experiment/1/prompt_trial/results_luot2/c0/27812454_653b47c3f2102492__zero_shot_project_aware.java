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
        List<String> candidates = new ArrayList<String>();
        candidates.add("A");
        Map<String, Double> probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("A", -0.5);

        algorithm.startWithInitialObservation("first", candidates, probabilities);

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
        List<String> initialCandidates = new ArrayList<String>();
        initialCandidates.add("A");
        initialCandidates.add("B");
        Map<String, Double> initialProbabilities = new LinkedHashMap<String, Double>();
        initialProbabilities.put("A", -0.1);
        initialProbabilities.put("B", -0.5);
        algorithm.startWithInitialObservation("o1", initialCandidates, initialProbabilities);

        List<String> nextCandidates = new ArrayList<String>();
        nextCandidates.add("C");
        Map<String, Double> emissions = new LinkedHashMap<String, Double>();
        emissions.put("C", -0.2);
        Map<Transition<String>, Double> transitions =
                new LinkedHashMap<Transition<String>, Double>();
        transitions.put(new Transition<String>("A", "C"), -0.4);
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
        assertEquals("C", sequence.get(1).state);
        assertEquals("o2", sequence.get(1).observation);
        assertEquals("from-A", sequence.get(1).transitionDescriptor);
    }

    @Test
    public void recordedMessageHistoryContainsInitialAndNextMessages() {
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
        transitions.put(new Transition<String>("A", "B"), -0.25);
        algorithm.nextStep(null, nextCandidates, emissions, transitions);

        List<Map<String, Double>> history = algorithm.messageHistory();
        assertEquals(2, history.size());
        assertEquals(Double.valueOf(-1.0), history.get(0).get("A"));
        assertEquals(Double.valueOf(-1.75), history.get(1).get("B"));
        assertEquals("Message history with log probabilies\n\n"
                + "Time step 0\nA: -1.0\n\n"
                + "Time step 1\nB: -1.75\n\n", algorithm.messageHistoryString());
    }

    @Test
    public void initialZeroProbabilityBreaksAndLeavesSequenceEmpty() {
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
    public void missingTransitionBreaksAtNextStepAndKeepsPreviousSequence() {
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
        Map<Transition<String>, Double> transitions =
                new LinkedHashMap<Transition<String>, Double>();

        algorithm.nextStep(null, nextCandidates, emissions, transitions);

        assertTrue(algorithm.isBroken());
        List<SequenceState<String, String, String>> sequence =
                algorithm.computeMostLikelySequence();
        assertEquals(1, sequence.size());
        assertEquals("A", sequence.get(0).state);
    }

    @Test(expected = IllegalStateException.class)
    public void nextStepBeforeInitializationIsRejected() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        algorithm.nextStep(null, new ArrayList<String>(),
                new LinkedHashMap<String, Double>(),
                new LinkedHashMap<Transition<String>, Double>());
    }

    @Test(expected = IllegalStateException.class)
    public void initializationCannotBeRepeated() {
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
    public void missingInitialProbabilityIsRejected() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> candidates = new ArrayList<String>();
        candidates.add("A");
        algorithm.startWithInitialStateProbabilities(candidates,
                new LinkedHashMap<String, Double>());
    }

    @Test(expected = NullPointerException.class)
    public void missingEmissionProbabilityIsRejected() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        List<String> candidates = new ArrayList<String>();
        candidates.add("A");
        Map<String, Double> initialProbabilities = new LinkedHashMap<String, Double>();
        initialProbabilities.put("A", 0.0);
        algorithm.startWithInitialStateProbabilities(candidates, initialProbabilities);

        List<String> nextCandidates = new ArrayList<String>();
        nextCandidates.add("B");
        Map<Transition<String>, Double> transitions =
                new LinkedHashMap<Transition<String>, Double>();
        transitions.put(new Transition<String>("A", "B"), 0.0);
        algorithm.nextStep(null, nextCandidates, new LinkedHashMap<String, Double>(), transitions);
    }

    @Test(expected = IllegalStateException.class)
    public void messageHistoryStringRequiresHistoryRecording() {
        ViterbiAlgorithm<String, String, String> algorithm =
                new ViterbiAlgorithm<String, String, String>();
        algorithm.messageHistoryString();
    }
}
