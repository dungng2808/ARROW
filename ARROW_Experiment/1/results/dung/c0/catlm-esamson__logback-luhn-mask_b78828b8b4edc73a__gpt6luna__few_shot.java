package ph.samson.logback.luhn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LuhnMaskingConverter_ArrowTest {

    @Test
    public void maskValidCardNumber() {
        assertEquals("MASKED********1111", LuhnMaskingConverter.mask("4111111111111111"));
    }

    @Test
    public void maskValidCardNumberWithSeparatorsAndSurroundingText() {
        assertEquals("Card MASKED********1111 approved",
                LuhnMaskingConverter.mask("Card 4111-1111-1111-1111 approved"));
    }

    @Test
    public void leaveInvalidCardNumberUnchanged() {
        assertEquals("4111111111111112", LuhnMaskingConverter.mask("4111111111111112"));
    }

    @Test
    public void leaveMessageWithTooFewDigitsUnchanged() {
        assertEquals("Account 123456789012", LuhnMaskingConverter.mask("Account 123456789012"));
    }

    @Test
    public void nullMessageHasNoEnoughDigits() {
        assertFalse(LuhnMaskingConverter.hasEnoughDigits(null));
    }

    @Test
    public void detectEnoughDigits() {
        assertTrue(LuhnMaskingConverter.hasEnoughDigits("4111111111111"));
    }

    @Test
    public void luhnCheckValidNumber() {
        assertTrue(LuhnMaskingConverter.luhnCheck("4111111111111111"));
    }

    @Test
    public void luhnCheckInvalidNumber() {
        assertFalse(LuhnMaskingConverter.luhnCheck("4111111111111112"));
    }

    @Test
    public void stripSeparators() {
        assertEquals("4111111111111111",
                LuhnMaskingConverter.stripSeparators("4111-1111 1111-1111"));
    }

    @Test
    public void stripSeparatorsReturnsUnchangedStringWhenNoSeparators() {
        String number = "4111111111111111";
        assertTrue(number == LuhnMaskingConverter.stripSeparators(number));
    }

    @Test
    public void maskStringForShortMaskedSection() {
        assertEquals("MASKED", LuhnMaskingConverter.maskString("1234567890", "1234"));
    }

    @Test
    public void maskStringForLongMaskedSection() {
        assertEquals("**MASKED**", LuhnMaskingConverter.maskString("1234567890123456", "1234"));
    }

    @Test
    public void buildMaskPadsAroundLabel() {
        assertEquals("**MASKED**", LuhnMaskingConverter.buildMask(10));
    }

    @Test
    public void buildMaskForLengthShorterThanLabel() {
        assertEquals("MASKED", LuhnMaskingConverter.buildMask(3));
    }
}
