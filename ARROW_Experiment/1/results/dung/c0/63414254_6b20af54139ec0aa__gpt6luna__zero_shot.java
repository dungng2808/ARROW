package de.fraunhofer.iosb.ilt.frostserver.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringHelper_ArrowTest {

    @Test
    public void capitalizeChangesFirstCharacterToUppercase() {
        assertEquals("Hello", StringHelper.capitalize("hello"));
    }

    @Test
    public void capitalizeLeavesRemainingCharactersUnchanged() {
        assertEquals("hELLO", StringHelper.capitalize("hELLO"));
    }

    @Test
    public void deCapitalizeChangesFirstCharacterToLowercase() {
        assertEquals("hello", StringHelper.deCapitalize("Hello"));
    }

    @Test
    public void deCapitalizeLeavesRemainingCharactersUnchanged() {
        assertEquals("hELLO", StringHelper.deCapitalize("HELLO"));
    }

    @Test
    public void isNullOrEmptyReturnsTrueForNull() {
        assertTrue(StringHelper.isNullOrEmpty(null));
    }

    @Test
    public void isNullOrEmptyReturnsTrueForEmptyString() {
        assertTrue(StringHelper.isNullOrEmpty(""));
    }

    @Test
    public void isNullOrEmptyReturnsFalseForNonEmptyString() {
        assertFalse(StringHelper.isNullOrEmpty("text"));
    }

    @Test
    public void cleanForLoggingReplacesLineBreaksCarriageReturnsAndTabs() {
        assertEquals("a_nrt_b_nrt_c_nrt_d",
                StringHelper.cleanForLogging("a\nb\rc\td"));
    }

    @Test
    public void cleanForLoggingStringReturnsNullLiteralForNull() {
        assertEquals("null", StringHelper.cleanForLogging((String) null));
    }

    @Test
    public void cleanForLoggingObjectUsesItsStringRepresentation() {
        assertEquals("a_nrt_b", StringHelper.cleanForLogging((Object) "a\nb"));
    }

    @Test
    public void cleanForLoggingObjectReturnsNullLiteralForNull() {
        assertEquals("null", StringHelper.cleanForLogging((Object) null));
    }

    @Test
    public void escapeForStringConstantDoublesSingleQuotes() {
        assertEquals("it''s ''quoted''", StringHelper.escapeForStringConstant("it's 'quoted'"));
    }

    @Test
    public void urlEncodeEncodesSpacesAndReservedCharacters() {
        assertEquals("a+b%2Fc", StringHelper.urlEncode("a b/c"));
    }

    @Test
    public void urlDecodeDecodesEncodedTextAndPlusAsSpace() {
        assertEquals("a b/c", StringHelper.urlDecode("a+b%2Fc"));
    }

    @Test
    public void urlEncodeWithNotSlashesFalseEncodesSlashes() {
        assertEquals("a%2Fb", StringHelper.urlEncode("a/b", false));
    }

    @Test
    public void urlEncodeWithNotSlashesTruePreservesSlashes() {
        assertEquals("a/b+c", StringHelper.urlEncode("a/b c", true));
    }

    @Test
    public void urlEncodeNotSlashesPreservesSeparatingSlashes() {
        assertEquals("a/b/c", StringHelper.urlEncodeNotSlashes("a/b/c"));
    }

    @Test
    public void urlEncodeNotSlashesEncodesEachPathSegment() {
        assertEquals("a+b/c%2Bd", StringHelper.urlEncodeNotSlashes("a b/c+d"));
    }
}
