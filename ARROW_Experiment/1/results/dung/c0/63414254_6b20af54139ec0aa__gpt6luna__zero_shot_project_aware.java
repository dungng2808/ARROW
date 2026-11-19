package de.fraunhofer.iosb.ilt.frostserver.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringHelper_ArrowTest {

    @Test
    public void capitalizeUppercasesFirstCharacter() {
        assertEquals("Hello", StringHelper.capitalize("hello"));
    }

    @Test
    public void deCapitalizeLowercasesFirstCharacter() {
        assertEquals("hello", StringHelper.deCapitalize("Hello"));
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
    public void cleanForLoggingReturnsLiteralNullForNullString() {
        assertEquals("null", StringHelper.cleanForLogging((String) null));
    }

    @Test
    public void cleanForLoggingConvertsObjectToStringAndCleansIt() {
        assertEquals("a_nrt_b", StringHelper.cleanForLogging((Object) "a\nb"));
    }

    @Test
    public void cleanForLoggingConvertsNullObjectToLiteralNull() {
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
    public void urlDecodeDecodesEncodedText() {
        assertEquals("a b/c", StringHelper.urlDecode("a+b%2Fc"));
    }

    @Test
    public void urlEncodeWithNotSlashesFalseEncodesSlashes() {
        assertEquals("a%2Fb", StringHelper.urlEncode("a/b", false));
    }

    @Test
    public void urlEncodeWithNotSlashesTruePreservesSlashes() {
        assertEquals("a/b", StringHelper.urlEncode("a/b", true));
    }

    @Test
    public void urlEncodeNotSlashesEncodesEachPathSegment() {
        assertEquals("a+b/c%2Fd", StringHelper.urlEncodeNotSlashes("a b/c/d"));
    }
}
