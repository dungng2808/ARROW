package com.ctrip.soa.caravan.common.value.corrector;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.regex.Pattern;

import org.junit.Test;

public class PatternCorrector_ArrowTest {

    @Test
    public void testCorrect_Matches() throws Exception {
        Pattern pattern = Pattern.compile("abc");
        PatternCorrector corrector = new PatternCorrector(pattern);
        String result = corrector.correct("abc");
        assertSame("abc", result);
    }

    @Test
    public void testCorrect_DoesNotMatch() throws Exception {
        Pattern pattern = Pattern.compile("abc");
        PatternCorrector corrector = new PatternCorrector(pattern);
        String result = corrector.correct("def");
        assertNull(result);
    }

    @Test
    public void testCorrect_NullValue() throws Exception {
        Pattern pattern = Pattern.compile("abc");
        PatternCorrector corrector = new PatternCorrector(pattern);
        String result = corrector.correct(null);
        assertNull(result);
    }

    @Test
    public void testCorrect_EmptyValue() throws Exception {
        Pattern pattern = Pattern.compile("abc");
        PatternCorrector corrector = new PatternCorrector(pattern);
        String result = corrector.correct("");
        assertNull(result);
    }

    @Test
    public void testCorrect_WhitespaceValue() throws Exception {
        Pattern pattern = Pattern.compile("abc");
        PatternCorrector corrector = new PatternCorrector(pattern);
        String result = corrector.correct("   ");
        assertNull(result);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_NullPattern() throws Exception {
        new PatternCorrector(null);
    }
}
