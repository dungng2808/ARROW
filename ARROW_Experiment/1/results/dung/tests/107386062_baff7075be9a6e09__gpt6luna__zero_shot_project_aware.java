package com.ctrip.soa.caravan.common.value.corrector;

import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PatternCorrector_ArrowTest {

    @Test
    public void correctReturnsValueWhenItMatchesPattern() {
        PatternCorrector corrector = new PatternCorrector(Pattern.compile("[a-z]+"));

        assertEquals("caravan", corrector.correct("caravan"));
    }

    @Test
    public void correctReturnsNullWhenValueDoesNotMatchPattern() {
        PatternCorrector corrector = new PatternCorrector(Pattern.compile("[a-z]+"));

        assertNull(corrector.correct("Caravan"));
    }

    @Test
    public void correctReturnsNullForEmptyValue() {
        PatternCorrector corrector = new PatternCorrector(Pattern.compile("[a-z]+"));

        assertNull(corrector.correct(""));
    }

    @Test
    public void correctReturnsNullForNullValue() {
        PatternCorrector corrector = new PatternCorrector(Pattern.compile("[a-z]+"));

        assertNull(corrector.correct(null));
    }
}
