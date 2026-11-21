package de.learnlib.alex.modelchecking.entities;

import net.automatalib.words.Word;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class LtsCheckingResult_ArrowTest {

    @Test
    public void threeArgumentConstructorCreatesPassedResult() {
        LtsCheckingResult result = new LtsCheckingResult(null, 12L, 3L);

        assertTrue(result.getPrefix().isEmpty());
        assertTrue(result.getLoop().isEmpty());
        assertTrue(result.isPassed());
        assertEquals(Long.valueOf(12L), result.getLearnerResultId());
        assertEquals(Long.valueOf(3L), result.getLearnerResultStepNo());
    }

    @Test
    public void fiveArgumentConstructorStoresCounterexampleWords() {
        LtsCheckingResult result = new LtsCheckingResult(null,
                                                         12L,
                                                         3L,
                                                         Word.fromList(Arrays.asList("a", "b")),
                                                         Word.fromList(Collections.singletonList("c")));

        assertEquals(Arrays.asList("a", "b"), result.getPrefix());
        assertEquals(Collections.singletonList("c"), result.getLoop());
        assertFalse(result.isPassed());
    }

    @Test
    public void settersReplaceValues() {
        LtsCheckingResult result = new LtsCheckingResult(null, 1L, 2L);
        LtsFormula formula = null;
        java.util.List<String> prefix = Arrays.asList("x");
        java.util.List<String> loop = Arrays.asList("y");

        result.setFormula(formula);
        result.setLearnerResultId(8L);
        result.setLearnerResultStepNo(9L);
        result.setPrefix(prefix);
        result.setLoop(loop);

        assertSame(formula, result.getFormula());
        assertEquals(Long.valueOf(8L), result.getLearnerResultId());
        assertEquals(Long.valueOf(9L), result.getLearnerResultStepNo());
        assertSame(prefix, result.getPrefix());
        assertSame(loop, result.getLoop());
        assertFalse(result.isPassed());
    }

    @Test
    public void emptyPrefixAndLoopArePassed() {
        LtsCheckingResult result = new LtsCheckingResult(null, 1L, 2L);
        result.setPrefix(Collections.<String>emptyList());
        result.setLoop(Collections.<String>emptyList());

        assertTrue(result.isPassed());
    }

    @Test
    public void nonemptyPrefixWithEmptyLoopIsNotPassed() {
        LtsCheckingResult result = new LtsCheckingResult(null, 1L, 2L);
        result.setPrefix(Collections.singletonList("a"));
        result.setLoop(Collections.<String>emptyList());

        assertFalse(result.isPassed());
    }

    @Test
    public void emptyPrefixWithNonemptyLoopIsNotPassed() {
        LtsCheckingResult result = new LtsCheckingResult(null, 1L, 2L);
        result.setPrefix(Collections.<String>emptyList());
        result.setLoop(Collections.singletonList("a"));

        assertFalse(result.isPassed());
    }
}
