package com.ctrip.soa.caravan.common.value.corrector;

import java.util.regex.Pattern;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PatternCorrector_ArrowTest {

    @Test
    public void correctReturnsValueWhenPatternMatchesEntireInput() {
        PatternCorrector corrector = new PatternCorrector(Pattern.compile("[a-z]+"));
        assertEquals("caravan", corrector.correct("caravan"));
    }

    @Test
    public void correctReturnsNullWhenPatternDoesNotMatchEntireInput() {
        PatternCorrector corrector = new PatternCorrector(Pattern.compile("[a-z]+"));
        assertNull(corrector.correct("caravan123"));
    }

    @Test
    public void correctReturnsNullForEmptyInput() {
        PatternCorrector corrector = new PatternCorrector(Pattern.compile("[a-z]+"));
        assertNull(corrector.correct(""));
    }

    @Test
    public void correctReturnsNullForNullInput() {
        PatternCorrector corrector = new PatternCorrector(Pattern.compile("[a-z]+"));
        assertNull(corrector.correct(null));
    }
}
