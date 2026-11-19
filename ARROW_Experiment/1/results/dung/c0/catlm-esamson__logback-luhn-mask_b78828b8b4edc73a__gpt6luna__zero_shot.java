package ph.samson.logback.luhn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LuhnMaskingConverter_ArrowTest {

    @Test
    public void maskValidNumber() {
        assertEquals("MASKED****1111", LuhnMaskingConverter.mask("4111111111111111"));
    }

    @Test
    public void maskValidNumberWithSeparatorsAndSurroundingText() {
        assertEquals("Card: MASKED**** 1111!", LuhnMaskingConverter.mask("Card: 4111-1111-1111-1111!"));
    }

    @Test
    public void leavesInvalidNumberUnchanged() {
        assertEquals("4111111111111112", LuhnMaskingConverter.mask("4111111111111112"));
    }

    @Test
    public void leavesShortNumberUnchanged() {
        assertEquals("123456789012", LuhnMaskingConverter.mask("123456789012"));
    }

    @Test
    public void maskReturnsNullForNullInput() {
        assertEquals(null, LuhnMaskingConverter.mask(null));
    }

    @Test
    public void hasEnoughDigitsRecognizesThirteenDigits() {
        assertTrue(LuhnMaskingConverter.hasEnoughDigits("1234567890123"));
    }

    @Test
    public void hasEnoughDigitsAllowsSpacesAndHyphens() {
        assertTrue(LuhnMaskingConverter.hasEnoughDigits("1234-5678 9012-3"));
    }

    @Test
    public void hasEnoughDigitsResetsAtOtherCharacters() {
        assertFalse(LuhnMaskingConverter.hasEnoughDigits("1234567x890123"));
    }

    @Test
    public void hasEnoughDigitsReturnsFalseForNull() {
        assertFalse(LuhnMaskingConverter.hasEnoughDigits(null));
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
    public void maskStringUsesLabelAndPaddingForLongMask() {
        assertEquals("**MASKED*", LuhnMaskingConverter.maskString("1234567890", "0"));
    }

    @Test
    public void buildMaskPadsAroundLabel() {
        assertEquals("**MASKED*", LuhnMaskingConverter.buildMask(9));
    }

    @Test
    public void buildMaskUsesLabelWhenRequestedLengthIsShort() {
        assertEquals("MASKED", LuhnMaskingConverter.buildMask(3));
    }

    @Test
    public void convertMasksFormattedLoggingMessage() {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ch.qos.logback.classic.spi.ILoggingEvent event =
                new ch.qos.logback.classic.spi.ILoggingEvent() {
                    @Override
                    public String getFormattedMessage() {
                        return "4111111111111111";
                    }

                    @Override
                    public String getMessage() {
                        return null;
                    }

                    @Override
                    public Object[] getArgumentArray() {
                        return null;
                    }

                    @Override
                    public ch.qos.logback.classic.Logger getLogger() {
                        return null;
                    }

                    @Override
                    public ch.qos.logback.classic.Level getLevel() {
                        return null;
                    }

                    @Override
                    public String getLoggerName() {
                        return null;
                    }

                    @Override
                    public ch.qos.logback.classic.spi.LoggerContextVO getLoggerContextVO() {
                        return null;
                    }

                    @Override
                    public ch.qos.logback.classic.spi.IThrowableProxy getThrowableProxy() {
                        return null;
                    }

                    @Override
                    public StackTraceElement[] getCallerData() {
                        return null;
                    }

                    @Override
                    public boolean hasCallerData() {
                        return false;
                    }

                    @Override
                    public org.slf4j.Marker getMarker() {
                        return null;
                    }

                    @Override
                    public java.util.Map<String, String> getMDCPropertyMap() {
                        return null;
                    }

                    @Override
                    public java.util.Map<String, String> getMdc() {
                        return null;
                    }

                    @Override
                    public long getTimeStamp() {
                        return 0;
                    }

                    @Override
                    public void prepareForDeferredProcessing() {
                    }
                };

        assertEquals("MASKED****1111", converter.convert(event));
    }
}
