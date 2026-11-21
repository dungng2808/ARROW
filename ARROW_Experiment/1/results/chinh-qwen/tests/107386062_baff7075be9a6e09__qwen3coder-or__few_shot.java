package com.ctrip.soa.caravan.common.value.corrector;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.regex.Pattern;

import org.junit.Test;

public class PatternCorrector_ArrowTest {

    @Test(expected = IllegalArgumentException.class)
    public void test_constructor_with_null_pattern() {
        new PatternCorrector(null);
    }

    @Test
    public void test_correct_with_null_value() {
        PatternCorrector corrector = new PatternCorrector(Pattern.compile(".*"));
        assertNull(corrector.correct(null));
    }

    @Test
    public void test_correct_with_empty_value() {
        PatternCorrector corrector = new PatternCorrector(Pattern.compile(".*"));
        assertNull(corrector.correct(""));
    }

    @Test
    public void test_correct_with_matching_value() {
        PatternCorrector corrector = new PatternCorrector(Pattern.compile("\\d+"));
        String result = corrector.correct("123");
        assertSame("123", result);
    }

    @Test
    public void test_correct_with_non_matching_value() {
        PatternCorrector corrector = new PatternCorrector(Pattern.compile("\\d+"));
        assertNull(corrector.correct("abc"));
    }
}
