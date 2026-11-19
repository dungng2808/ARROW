package ph.samson.logback.luhn;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LuhnMaskingConverter_ArrowTest {

    @Test
    public void convertMasksValidCardNumber() {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Card: 4111111111111111");

        LuhnMaskingConverter converter = new LuhnMaskingConverter();

        assertEquals("Card: MASKED1111", converter.convert(event));
    }

    @Test
    public void convertLeavesInvalidCardNumberUnchanged() {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Card: 4111111111111112");

        LuhnMaskingConverter converter = new LuhnMaskingConverter();

        assertEquals("Card: 4111111111111112", converter.convert(event));
    }

    @Test
    public void convertLeavesNullFormattedMessageUnchanged() {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(null);

        LuhnMaskingConverter converter = new LuhnMaskingConverter();

        assertEquals(null, converter.convert(event));
    }

    @Test
    public void maskLeavesMessageWithTooFewDigitsUnchanged() {
        assertEquals("411111111111", LuhnMaskingConverter.mask("411111111111"));
    }

    @Test
    public void maskHandlesValidNumberWithSeparators() {
        assertEquals("MASKED1111", LuhnMaskingConverter.mask("4111-1111-1111-1111"));
    }

    @Test
    public void maskHandlesMultipleNumbersSeparatedByPunctuation() {
        assertEquals("MASKED1111, MASKED1111",
                LuhnMaskingConverter.mask("4111111111111111, 4111111111111111"));
    }

    @Test
    public void hasEnoughDigitsReturnsFalseForNull() {
        assertFalse(LuhnMaskingConverter.hasEnoughDigits(null));
    }

    @Test
    public void hasEnoughDigitsReturnsFalseBelowMinimum() {
        assertFalse(LuhnMaskingConverter.hasEnoughDigits("411111111111"));
    }

    @Test
    public void hasEnoughDigitsReturnsTrueAtMinimum() {
        assertTrue(LuhnMaskingConverter.hasEnoughDigits("4111111111111"));
    }

    @Test
    public void hasEnoughDigitsResetsAtNonSeparator() {
        assertFalse(LuhnMaskingConverter.hasEnoughDigits("4111111111111x111111111111"));
    }

    @Test
    public void luhnCheckAcceptsValidNumber() {
        assertTrue(LuhnMaskingConverter.luhnCheck("4111111111111111"));
    }

    @Test
    public void luhnCheckRejectsInvalidNumber() {
        assertFalse(LuhnMaskingConverter.luhnCheck("4111111111111112"));
    }

    @Test
    public void stripSeparatorsRemovesSpacesAndHyphens() {
        assertEquals("4111111111111111",
                LuhnMaskingConverter.stripSeparators("4111-1111 1111-1111"));
    }

    @Test
    public void stripSeparatorsReturnsOriginalContentWhenNoSeparators() {
        assertEquals("12345", LuhnMaskingConverter.stripSeparators("12345"));
    }

    @Test
    public void maskStringUsesMaskForShortMaskedLength() {
        assertEquals("MASKED", LuhnMaskingConverter.maskString("123456789", "123456789"));
    }

    @Test
    public void maskStringBuildsLongMask() {
        assertEquals("********MASKED****",
                LuhnMaskingConverter.maskString("12345678901234567890", "1234"));
    }

    @Test
    public void buildMaskPadsAroundLabel() {
        assertEquals("**MASKED*", LuhnMaskingConverter.buildMask(11));
    }

    @Test
    public void buildMaskUsesLabelWhenRequestedLengthIsShort() {
        assertEquals("MASKED", LuhnMaskingConverter.buildMask(3));
    }
}
