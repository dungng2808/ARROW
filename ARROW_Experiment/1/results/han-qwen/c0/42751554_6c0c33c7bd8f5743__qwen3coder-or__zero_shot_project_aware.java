package de.learnlib.alex.modelchecking.entities;

import net.automatalib.words.Word;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

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
        assertEquals(new LtsFormula(), result.getFormula());
        assertEquals(1L, result.getLearnerResultId().longValue());
        assertEquals(2L, result.getLearnerResultStepNo().longValue());
        assertNotNull(result.getPrefix());
        assertNotNull(result.getLoop());

        // Test setters
        LtsFormula newFormula = new LtsFormula();
        Long newLearnerResultId = 3L;
        Long newLearnerResultStepNo = 4L;
        List<String> newPrefix = Arrays.asList("x", "y");
        List<String> newLoop = Arrays.asList("z");

        result.setFormula(newFormula);
        result.setLearnerResultId(newLearnerResultId);
        result.setLearnerResultStepNo(newLearnerResultStepNo);
        result.setPrefix(newPrefix);
        result.setLoop(newLoop);

        assertEquals(newFormula, result.getFormula());
        assertEquals(newLearnerResultId, result.getLearnerResultId());
        assertEquals(newLearnerResultStepNo, result.getLearnerResultStepNo());
        assertEquals(newPrefix, result.getPrefix());
        assertEquals(newLoop, result.getLoop());
    }

    @Test
    public void testIsPassedWhenEmpty() {
        LtsCheckingResult result = new LtsCheckingResult(new LtsFormula(), 1L, 2L);
        assertTrue(result.isPassed());
    }

    @Test
    public void testIsPassedWhenPrefixNotEmpty() {
        LtsCheckingResult result = new LtsCheckingResult(new LtsFormula(), 1L, 2L);
        result.setPrefix(Arrays.asList("a"));
        assertFalse(result.isPassed());
    }

    @Test
    public void testIsPassedWhenLoopNotEmpty() {
        LtsCheckingResult result = new LtsCheckingResult(new LtsFormula(), 1L, 2L);
        result.setLoop(Arrays.asList("a"));
        assertFalse(result.isPassed());
    }

    @Test
    public void testIsPassedWhenBothNotEmpty() {
        LtsCheckingResult result = new LtsCheckingResult(new LtsFormula(), 1L, 2L);
        result.setPrefix(Arrays.asList("a"));
        result.setLoop(Arrays.asList("b"));
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
