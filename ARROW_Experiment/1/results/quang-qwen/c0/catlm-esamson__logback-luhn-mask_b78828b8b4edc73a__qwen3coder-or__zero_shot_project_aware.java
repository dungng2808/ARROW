package ph.samson.logback.luhn;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LuhnMaskingConverter_ArrowTest {

    @Test
    public void testConvertNullEvent() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(null);
        assertEquals(null, converter.convert(event));
    }

    @Test
    public void testConvertEmptyString() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("");
        assertEquals("", converter.convert(event));
    }

    @Test
    public void testConvertNoDigits() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Hello World");
        assertEquals("Hello World", converter.convert(event));
    }

    @Test
    public void testConvertNotEnoughDigits() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("12345");
        assertEquals("12345", converter.convert(event));
    }

    @Test
    public void testConvertValidCreditCardNumber() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("My card number is 4000000000000002");
        assertEquals("My card number is MASKED", converter.convert(event));
    }

    @Test
    public void testConvertValidCreditCardWithSpaces() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Card: 4000 0000 0000 0002");
        assertEquals("Card: MASKED", converter.convert(event));
    }

    @Test
    public void testConvertValidCreditCardWithDashes() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Card: 4000-0000-0000-0002");
        assertEquals("Card: MASKED", converter.convert(event));
    }

    @Test
    public void testConvertMultipleCreditCards() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Card1: 4000000000000002 Card2: 5555555555554444");
        assertEquals("Card1: MASKED Card2: MASKED", converter.convert(event));
    }

    @Test
    public void testConvertMixedContent() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Contact me at 123-456-7890 or 4000000000000002");
        assertEquals("Contact me at 123-456-7890 or MASKED", converter.convert(event));
    }

    @Test
    public void testHasEnoughDigitsNull() {
        assertFalse(LuhnMaskingConverter.hasEnoughDigits(null));
    }

    @Test
    public void testHasEnoughDigitsEmpty() {
        assertFalse(LuhnMaskingConverter.hasEnoughDigits(""));
    }

    @Test
    public void testHasEnoughDigitsNotEnough() {
        assertFalse(LuhnMaskingConverter.hasEnoughDigits("12345"));
    }

    @Test
    public void testHasEnoughDigitsJustEnough() {
        assertTrue(LuhnMaskingConverter.hasEnoughDigits("1234567890123"));
    }

    @Test
    public void testHasEnoughDigitsWithNonDigits() {
        assertTrue(LuhnMaskingConverter.hasEnoughDigits("1234567890123abc"));
    }

    @Test
    public void testHasEnoughDigitsWithSeparators() {
        assertTrue(LuhnMaskingConverter.hasEnoughDigits("1234 5678 9012 3"));
    }

    @Test
    public void testLuhnCheckValid() {
        assertTrue(LuhnMaskingConverter.luhnCheck("4000000000000002"));
    }

    @Test
    public void testLuhnCheckInvalid() {
        assertFalse(LuhnMaskingConverter.luhnCheck("1234567890123456"));
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
    public void testStripSeparatorsWithDashes() {
        assertEquals("1234567890123456", LuhnMaskingConverter.stripSeparators("1234-5678-9012-3456"));
    }

    @Test
    public void testMaskStringShort() {
        assertEquals("***MASKED", LuhnMaskingConverter.maskString("1234567890123456", "3456"));
    }

    @Test
    public void testMaskStringLong() {
        assertEquals("**********MASKED", LuhnMaskingConverter.maskString("12345678901234567890", "1234"));
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
    public void testIsDigit() {
        assertTrue(LuhnMaskingConverter.isDigit('0'));
        assertTrue(LuhnMaskingConverter.isDigit('9'));
        assertFalse(LuhnMaskingConverter.isDigit('a'));
        assertFalse(LuhnMaskingConverter.isDigit(' '));
    }

    @Test
    public void testMaskNoDigits() {
        assertEquals("Hello World", LuhnMaskingConverter.mask("Hello World"));
    }

    @Test
    public void testMaskNotEnoughDigits() {
        assertEquals("12345", LuhnMaskingConverter.mask("12345"));
    }

    @Test
    public void testMaskValidCreditCard() {
        assertEquals("My card number is MASKED", LuhnMaskingConverter.mask("My card number is 4000000000000002"));
    }

    @Test
    public void testMaskValidCreditCardWithSpaces() {
        assertEquals("Card: MASKED", LuhnMaskingConverter.mask("Card: 4000 0000 0000 0002"));
    }

    @Test
    public void testMaskValidCreditCardWithDashes() {
        assertEquals("Card: MASKED", LuhnMaskingConverter.mask("Card: 4000-0000-0000-0002"));
    }

    @Test
    public void testMaskMultipleCreditCards() {
        assertEquals("Card1: MASKED Card2: MASKED", LuhnMaskingConverter.mask("Card1: 4000000000000002 Card2: 5555555555554444"));
    }

    @Test
    public void testMaskMixedContent() {
        assertEquals("Contact me at 123-456-7890 or MASKED", LuhnMaskingConverter.mask("Contact me at 123-456-7890 or 4000000000000002"));
    }
}
