package com.ctrip.soa.caravan.common.value.corrector;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.regex.Pattern;

import org.junit.Test;

public class PatternCorrector_ArrowTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullPattern() {
        new PatternCorrector(null);
    }

    @Test
    public void testCorrectWithNullValue() {
        Pattern pattern = mock(Pattern.class);
        PatternCorrector corrector = new PatternCorrector(pattern);
        assertSame(null, corrector.correct(null));
    }

    @Test
    public void testCorrectWithEmptyValue() {
        Pattern pattern = mock(Pattern.class);
        PatternCorrector corrector = new PatternCorrector(pattern);
        assertSame(null, corrector.correct(""));
    }

    @Test
    public void testCorrectWithNonMatchingValue() {
        Pattern pattern = Pattern.compile("^[0-9]+$");
        PatternCorrector corrector = new PatternCorrector(pattern);
        assertSame(null, corrector.correct("abc"));
    }

    @Test
    public void testCorrectWithMatchingValue() {
        Pattern pattern = Pattern.compile("^[0-9]+$");
        PatternCorrector corrector = new PatternCorrector(pattern);
        assertSame("123", corrector.correct("123"));
    }

    @Test
    public void testCorrectWithMatchingValueAndSpecialChars() {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$");
        PatternCorrector corrector = new PatternCorrector(pattern);
        assertSame("test_123", corrector.correct("test_123"));
    }

    @Test
    public void testCorrectWithNullMatcherMatches() {
        Pattern pattern = mock(Pattern.class);
        Matcher matcher = mock(Matcher.class);
        when(pattern.matcher("test")).thenReturn(matcher);
        when(matcher.matches()).thenReturn(false);
        PatternCorrector corrector = new PatternCorrector(pattern);
        assertSame(null, corrector.correct("test"));
    }

    @Test
    public void testCorrectWithTrueMatcherMatches() {
        Pattern pattern = mock(Pattern.class);
        Matcher matcher = mock(Matcher.class);
        when(pattern.matcher("test")).thenReturn(matcher);
        when(matcher.matches()).thenReturn(true);
        PatternCorrector corrector = new PatternCorrector(pattern);
        assertSame("test", corrector.correct("test"));
    }
}
