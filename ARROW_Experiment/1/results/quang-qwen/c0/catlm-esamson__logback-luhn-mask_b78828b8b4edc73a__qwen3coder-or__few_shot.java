package ph.samson.logback.luhn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LuhnMaskingConverter_ArrowTest {

    @Test
    public void testConvertNull() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        // Mock ILoggingEvent to return null message
        assertNull(converter.convert(null));
    }

    @Test
    public void testMaskNull() {
        assertEquals(null, LuhnMaskingConverter.mask(null));
    }

    @Test
    public void testMaskEmptyString() {
        assertEquals("", LuhnMaskingConverter.mask(""));
    }

    @Test
    public void testMaskNoDigits() {
        assertEquals("hello world", LuhnMaskingConverter.mask("hello world"));
    }

    @Test
    public void testMaskNotEnoughDigits() {
        assertEquals("123456789012", LuhnMaskingConverter.mask("123456789012"));
    }

    @Test
    public void testMaskValidLuhnNumber() {
        assertEquals("card number is 1234567890123456", LuhnMaskingConverter.mask("card number is 1234567890123456"));
    }

    @Test
    public void testMaskValidLuhnNumberWithSpaces() {
        assertEquals("card number is 1234 5678 9012 3456", LuhnMaskingConverter.mask("card number is 1234 5678 9012 3456"));
    }

    @Test
    public void testMaskValidLuhnNumberWithHyphens() {
        assertEquals("card number is 1234-5678-9012-3456", LuhnMaskingConverter.mask("card number is 1234-5678-9012-3456"));
    }

    @Test
    public void testMaskValidLuhnNumberMixedSeparators() {
        assertEquals("card number is 1234-5678 9012-3456", LuhnMaskingConverter.mask("card number is 1234-5678 9012-3456"));
    }

    @Test
    public void testMaskInvalidLuhnNumber() {
        assertEquals("card number is 1234567890123457", LuhnMaskingConverter.mask("card number is 1234567890123457"));
    }

    @Test
    public void testHasEnoughDigitsNull() {
        assertEquals(false, LuhnMaskingConverter.hasEnoughDigits(null));
    }

    @Test
    public void testHasEnoughDigitsEmpty() {
        assertEquals(false, LuhnMaskingConverter.hasEnoughDigits(""));
    }

    @Test
    public void testHasEnoughDigitsNotEnough() {
        assertEquals(false, LuhnMaskingConverter.hasEnoughDigits("123456789012"));
    }

    @Test
    public void testHasEnoughDigitsExactlyMin() {
        assertEquals(true, LuhnMaskingConverter.hasEnoughDigits("1234567890123"));
    }

    @Test
    public void testHasEnoughDigitsMoreThanMin() {
        assertEquals(true, LuhnMaskingConverter.hasEnoughDigits("12345678901234"));
    }

    @Test
    public void testLuhnCheckValid() {
        assertEquals(true, LuhnMaskingConverter.luhnCheck("4000000000000002"));
    }

    @Test
    public void testLuhnCheckInvalid() {
        assertEquals(false, LuhnMaskingConverter.luhnCheck("4000000000000003"));
    }

    @Test
    public void testStripSeparatorsNoSeparators() {
        assertEquals("1234567890123456", LuhnMaskingConverter.stripSeparators("1234567890123456"));
    }

    @Test
    public void testStripSeparatorsWithSpaces() {
        assertEquals("1234567890123456", LuhnMaskingConverter.stripSeparators("1234 5678 9012 3456"));
    }

    @Test
    public void testStripSeparatorsWithHyphens() {
        assertEquals("1234567890123456", LuhnMaskingConverter.stripSeparators("1234-5678-9012-3456"));
    }

    @Test
    public void testStripSeparatorsMixed() {
        assertEquals("1234567890123456", LuhnMaskingConverter.stripSeparators("1234-5678 9012-3456"));
    }

    @Test
    public void testMaskStringShort() {
        assertEquals("***MASKED", LuhnMaskingConverter.maskString("1234567890123456", "3456"));
    }

    @Test
    public void testMaskStringLong() {
        assertEquals("**********MASKED", LuhnMaskingConverter.maskString("12345678901234567890", "3456"));
    }

    @Test
    public void testBuildMaskShort() {
        assertEquals("***MASKED", LuhnMaskingConverter.buildMask(7));
    }

    @Test
    public void testBuildMaskLong() {
        assertEquals("**********MASKED", LuhnMaskingConverter.buildMask(12));
    }

    @Test
    public void testBuildMaskExactLabelLength() {
        assertEquals("MASKED", LuhnMaskingConverter.buildMask(6));
    }

    @Test
    public void testIsDigit() {
        assertEquals(true, LuhnMaskingConverter.isDigit('5'));
        assertEquals(false, LuhnMaskingConverter.isDigit('a'));
    }
}
