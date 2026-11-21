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

        assertTrue(result.isPassed());
        assertEquals(Collections.emptyList(), result.getPrefix());
        assertEquals(Collections.emptyList(), result.getLoop());
        assertEquals(Long.valueOf(12L), result.getLearnerResultId());
        assertEquals(Long.valueOf(3L), result.getLearnerResultStepNo());
    }

    @Test
    public void fiveArgumentConstructorStoresCounterexampleWords() {
        Word<String> prefix = Word.fromSymbols("start", "next");
        Word<String> loop = Word.fromSymbols("repeat");
        LtsCheckingResult result = new LtsCheckingResult(null, 7L, 2L, prefix, loop);

        assertEquals(Arrays.asList("start", "next"), result.getPrefix());
        assertEquals(Collections.singletonList("repeat"), result.getLoop());
        assertFalse(result.isPassed());
    }

    @Test
    public void settersReplaceStoredValues() {
        LtsCheckingResult result = new LtsCheckingResult(null, 1L, 2L);
        LtsFormula formula = null;
        result.setPrefix(Collections.singletonList("p"));
        result.setLoop(Collections.singletonList("l"));
        result.setFormula(formula);
        result.setLearnerResultId(9L);
        result.setLearnerResultStepNo(8L);

        assertEquals(Collections.singletonList("p"), result.getPrefix());
        assertEquals(Collections.singletonList("l"), result.getLoop());
        assertSame(formula, result.getFormula());
        assertEquals(Long.valueOf(9L), result.getLearnerResultId());
        assertEquals(Long.valueOf(8L), result.getLearnerResultStepNo());
        assertFalse(result.isPassed());
    }

    @Test
    public void emptyPrefixAndNonemptyLoopIsNotPassed() {
        LtsCheckingResult result = new LtsCheckingResult(null, 1L, 1L);
        result.setLoop(Collections.singletonList("loop"));

        assertFalse(result.isPassed());
    }

    @Test
    public void nonemptyPrefixAndEmptyLoopIsNotPassed() {
        LtsCheckingResult result = new LtsCheckingResult(null, 1L, 1L);
        result.setPrefix(Collections.singletonList("prefix"));

        assertFalse(result.isPassed());
    }
}
