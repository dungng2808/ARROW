package de.learnlib.alex.modelchecking.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import net.automatalib.words.Word;
import org.junit.Test;

public class LtsCheckingResult_ArrowTest {

    @Test
    public void testConstructorWithWords() {
        LtsFormula formula = new LtsFormula();
        Long learnerResultId = 1L;
        Long learnerResultStepNo = 2L;
        Word<String> prefix = Word.fromList(Arrays.asList("a", "b"));
        Word<String> loop = Word.fromList(Arrays.asList("c", "d"));

        LtsCheckingResult result = new LtsCheckingResult(formula, learnerResultId, learnerResultStepNo, prefix, loop);

        assertEquals(formula, result.getFormula());
        assertEquals(learnerResultId, result.getLearnerResultId());
        assertEquals(learnerResultStepNo, result.getLearnerResultStepNo());
        assertEquals(prefix.asList(), result.getPrefix());
        assertEquals(loop.asList(), result.getLoop());
    }

    @Test
    public void testConstructorWithoutWords() {
        LtsFormula formula = new LtsFormula();
        Long learnerResultId = 3L;
        Long learnerResultStepNo = 4L;

        LtsCheckingResult result = new LtsCheckingResult(formula, learnerResultId, learnerResultStepNo);

        assertEquals(formula, result.getFormula());
        assertEquals(learnerResultId, result.getLearnerResultId());
        assertEquals(learnerResultStepNo, result.getLearnerResultStepNo());
        assertTrue(result.getPrefix().isEmpty());
        assertTrue(result.getLoop().isEmpty());
    }

    @Test
    public void testGettersAndSetters() {
        LtsCheckingResult result = new LtsCheckingResult(new LtsFormula(), 1L, 2L);

        // Test setters
        List<String> prefix = Arrays.asList("a", "b");
        List<String> loop = Arrays.asList("c", "d");
        LtsFormula formula = new LtsFormula();

        result.setPrefix(prefix);
        result.setLoop(loop);
        result.setFormula(formula);
        result.setLearnerResultId(3L);
        result.setLearnerResultStepNo(4L);

        // Test getters
        assertEquals(prefix, result.getPrefix());
        assertEquals(loop, result.getLoop());
        assertEquals(formula, result.getFormula());
        assertEquals(3L, result.getLearnerResultId().longValue());
        assertEquals(4L, result.getLearnerResultStepNo().longValue());
    }

    @Test
    public void testIsPassedWhenEmpty() {
        LtsCheckingResult result = new LtsCheckingResult(new LtsFormula(), 1L, 2L);
        assertTrue(result.isPassed());
    }

    @Test
    public void testIsPassedWhenNotEmpty() {
        Word<String> prefix = Word.fromList(Arrays.asList("a"));
        Word<String> loop = Word.epsilon();
        LtsCheckingResult result = new LtsCheckingResult(new LtsFormula(), 1L, 2L, prefix, loop);
        assertTrue(!result.isPassed());

        prefix = Word.epsilon();
        loop = Word.fromList(Arrays.asList("a"));
        result = new LtsCheckingResult(new LtsFormula(), 1L, 2L, prefix, loop);
        assertTrue(!result.isPassed());

        prefix = Word.fromList(Arrays.asList("a"));
        loop = Word.fromList(Arrays.asList("b"));
        result = new LtsCheckingResult(new LtsFormula(), 1L, 2L, prefix, loop);
        assertTrue(!result.isPassed());
    }
}
