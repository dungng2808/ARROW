package de.learnlib.alex.modelchecking.entities;

import net.automatalib.words.Word;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class LtsCheckingResult_ArrowTest {

    @Test
    public void testConstructorWithAllParameters() {
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
    public void testConstructorWithThreeParameters() {
        LtsFormula formula = new LtsFormula();
        Long learnerResultId = 1L;
        Long learnerResultStepNo = 2L;

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

        // Test getters
        assertNull(result.getFormula());
        assertNull(result.getLearnerResultId());
        assertNull(result.getLearnerResultStepNo());
        assertNotNull(result.getPrefix());
        assertNotNull(result.getLoop());

        // Test setters
        LtsFormula formula = new LtsFormula();
        Long learnerResultId = 3L;
        Long learnerResultStepNo = 4L;
        List<String> prefix = Arrays.asList("a", "b");
        List<String> loop = Arrays.asList("c", "d");

        result.setFormula(formula);
        result.setLearnerResultId(learnerResultId);
        result.setLearnerResultStepNo(learnerResultStepNo);
        result.setPrefix(prefix);
        result.setLoop(loop);

        assertEquals(formula, result.getFormula());
        assertEquals(learnerResultId, result.getLearnerResultId());
        assertEquals(learnerResultStepNo, result.getLearnerResultStepNo());
        assertEquals(prefix, result.getPrefix());
        assertEquals(loop, result.getLoop());
    }

    @Test
    public void testIsPassedWhenEmpty() {
        LtsCheckingResult result = new LtsCheckingResult(new LtsFormula(), 1L, 2L);
        assertTrue(result.isPassed());
    }

    @Test
    public void testIsPassedWhenPrefixNotEmpty() {
        LtsCheckingResult result = new LtsCheckingResult(new LtsFormula(), 1L, 2L);
        result.setPrefix(Arrays.asList("a", "b"));
        assertFalse(result.isPassed());
    }

    @Test
    public void testIsPassedWhenLoopNotEmpty() {
        LtsCheckingResult result = new LtsCheckingResult(new LtsFormula(), 1L, 2L);
        result.setLoop(Arrays.asList("a", "b"));
        assertFalse(result.isPassed());
    }

    @Test
    public void testIsPassedWhenBothNotEmpty() {
        LtsCheckingResult result = new LtsCheckingResult(new LtsFormula(), 1L, 2L);
        result.setPrefix(Arrays.asList("a", "b"));
        result.setLoop(Arrays.asList("c", "d"));
        assertFalse(result.isPassed());
    }

    @Test
    public void testIsPassedWithEmptyLists() {
        LtsCheckingResult result = new LtsCheckingResult(new LtsFormula(), 1L, 2L);
        result.setPrefix(Collections.emptyList());
        result.setLoop(Collections.emptyList());
        assertTrue(result.isPassed());
    }
}
