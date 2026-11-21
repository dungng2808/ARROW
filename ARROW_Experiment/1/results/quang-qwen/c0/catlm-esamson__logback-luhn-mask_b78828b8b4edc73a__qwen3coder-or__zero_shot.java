package ph.samson.logback.luhn;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LuhnMaskingConverter_ArrowTest {

    @Test
    public void testConvert_NullEvent() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = null;
        assertNull(converter.convert(event));
    }

    @Test
    public void testConvert_NullFormattedMessage() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(null);
        assertEquals(null, converter.convert(event));
    }

    @Test
    public void testConvert_EmptyFormattedMessage() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("");
        assertEquals("", converter.convert(event));
    }

    @Test
    public void testConvert_NoDigits() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Hello World");
        assertEquals("Hello World", converter.convert(event));
    }

    @Test
    public void testConvert_NotEnoughDigits() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("123456789012");
        assertEquals("123456789012", converter.convert(event));
    }

    @Test
    public void testConvert_ValidCreditCardNumber() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("My card number is 4000000000000002");
        assertEquals("My card number is MASKED", converter.convert(event));
    }

    @Test
    public void testConvert_MultipleCreditCards() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Card1: 4000000000000002, Card2: 5555555555554444");
        assertEquals("Card1: MASKED, Card2: MASKED", converter.convert(event));
    }

    @Test
    public void testConvert_CreditCardWithSpaces() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Card: 4000 0000 0000 0002");
        assertEquals("Card: MASKED", converter.convert(event));
    }

    @Test
    public void testConvert_CreditCardWithDashes() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Card: 4000-0000-0000-0002");
        assertEquals("Card: MASKED", converter.convert(event));
    }

    @Test
    public void testConvert_CreditCardWithMixedSeparators() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Card: 4000-0000 0000-0002");
        assertEquals("Card: MASKED", converter.convert(event));
    }

    @Test
    public void testConvert_InvalidLuhnNumber() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Invalid card: 1234567890123456");
        assertEquals("Invalid card: 1234567890123456", converter.convert(event));
    }

    @Test
    public void testConvert_NumberNotEndingInFourDigits() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Some text 1234567890123456789012345678901234567890");
        assertEquals("Some text 1234567890123456789012345678901234567890", converter.convert(event));
    }

    @Test
    public void testHasEnoughDigits_NullInput() {
        assertFalse(LuhnMaskingConverter.hasEnoughDigits(null));
    }

    @Test
    public void testHasEnoughDigits_EmptyString() {
        assertFalse(LuhnMaskingConverter.hasEnoughDigits(""));
    }

    @Test
    public void testHasEnoughDigits_NotEnoughDigits() {
        assertFalse(LuhnMaskingConverter.hasEnoughDigits("123456789012"));
    }

    @Test
    public void testHasEnoughDigits_EnoughDigits() {
        assertTrue(LuhnMaskingConverter.hasEnoughDigits("1234567890123"));
    }

    @Test
    public void testHasEnoughDigits_DigitsWithSeparators() {
        assertTrue(LuhnMaskingConverter.hasEnoughDigits("1234 5678 9012 3456"));
    }

    @Test
    public void testLuhnCheck_ValidNumber() {
        assertTrue(LuhnMaskingConverter.luhnCheck("4000000000000002"));
    }

    @Test
    public void testLuhnCheck_InvalidNumber() {
        assertFalse(LuhnMaskingConverter.luhnCheck("1234567890123456"));
    }

    @Test
    public void testStripSeparators_NoSeparators() {
        assertEquals("1234567890123456", LuhnMaskingConverter.stripSeparators("1234567890123456"));
    }

    @Test
    public void testStripSeparators_WithSpaces() {
        assertEquals("1234567890123456", LuhnMaskingConverter.stripSeparators("1234 5678 9012 3456"));
    }

    @Test
    public void testStripSeparators_WithDashes() {
        assertEquals("1234567890123456", LuhnMaskingConverter.stripSeparators("1234-5678-9012-3456"));
    }

    @Test
    public void testStripSeparators_MixedSeparators() {
        assertEquals("1234567890123456", LuhnMaskingConverter.stripSeparators("1234-5678 9012-3456"));
    }

    @Test
    public void testMaskString_ShortMask() {
        assertEquals("***MASKED", LuhnMaskingConverter.maskString("1234567890123456", "3456"));
    }

    @Test
    public void testMaskString_LongMask() {
        assertEquals("****************MASKED", LuhnMaskingConverter.maskString("12345678901234567890", "7890"));
    }

    @Test
    public void testBuildMask_SmallLength() {
        assertEquals("***MASKED", LuhnMaskingConverter.buildMask(7));
    }

    @Test
    public void testBuildMask_LargeLength() {
        assertEquals("************MASKED", LuhnMaskingConverter.buildMask(12));
    }

    @Test
    public void testIsDigit_ValidDigits() {
        assertTrue(LuhnMaskingConverter.isDigit('0'));
        assertTrue(LuhnMaskingConverter.isDigit('5'));
        assertTrue(LuhnMaskingConverter.isDigit('9'));
    }

    @Test
    public void testIsDigit_InvalidChars() {
        assertFalse(LuhnMaskingConverter.isDigit('a'));
        assertFalse(LuhnMaskingConverter.isDigit(' '));
        assertFalse(LuhnMaskingConverter.isDigit('-'));
    }
}
