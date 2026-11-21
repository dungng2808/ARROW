package de.learnlib.alex.modelchecking.entities;

import net.automatalib.words.Word;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LtsCheckingResult_ArrowTest {

    @Test
    public void constructorWithNoCounterexampleCreatesPassedResult() {
        LtsCheckingResult result = new LtsCheckingResult(null, 1L, 2L);

        assertEquals(Collections.emptyList(), result.getPrefix());
        assertEquals(Collections.emptyList(), result.getLoop());
        assertTrue(result.isPassed());
    }

    @Test
    public void constructorStoresCounterexampleWords() {
        LtsCheckingResult result = new LtsCheckingResult(
                null, 1L, 2L, Word.fromSymbols("a", "b"), Word.fromSymbols("c"));

        assertEquals(Arrays.asList("a", "b"), result.getPrefix());
        assertEquals(Collections.singletonList("c"), result.getLoop());
        assertFalse(result.isPassed());
    }

    @Test
    public void settersUpdateResultFields() {
        LtsCheckingResult result = new LtsCheckingResult(null, 1L, 2L);
        result.setPrefix(Collections.singletonList("p"));
        result.setLoop(Collections.singletonList("l"));
        result.setLearnerResultId(3L);
        result.setLearnerResultStepNo(4L);

        assertEquals(Collections.singletonList("p"), result.getPrefix());
        assertEquals(Collections.singletonList("l"), result.getLoop());
        assertEquals(Long.valueOf(3L), result.getLearnerResultId());
        assertEquals(Long.valueOf(4L), result.getLearnerResultStepNo());
        assertFalse(result.isPassed());
    }

    @Test
    public void emptyListsIndicatePassedResult() {
        LtsCheckingResult result = new LtsCheckingResult(null, 1L, 2L);
        result.setPrefix(Collections.emptyList());
        result.setLoop(Collections.emptyList());

        assertTrue(result.isPassed());
    }
}
