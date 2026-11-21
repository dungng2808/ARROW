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
    public void deCapitalizeChangesFirstCharacterToLowercase() {
        assertEquals("hello", StringHelper.deCapitalize("Hello"));
    }

    @Test
    public void isNullOrEmptyRecognizesNullAndEmptyStrings() {
        assertTrue(StringHelper.isNullOrEmpty(null));
        assertTrue(StringHelper.isNullOrEmpty(""));
        assertFalse(StringHelper.isNullOrEmpty("text"));
    }

    @Test
    public void cleanForLoggingReplacesLineBreaksAndTabs() {
        assertEquals("a_nrt_b_nrt_c_nrt_d",
                StringHelper.cleanForLogging("a\nb\rc\td"));
    }

    @Test
    public void cleanForLoggingStringifiesObjectsAndNull() {
        assertEquals("value_nrt_next", StringHelper.cleanForLogging((Object) "value\nnext"));
        assertEquals("null", StringHelper.cleanForLogging((Object) null));
    }

    @Test
    public void escapeForStringConstantDoublesSingleQuotes() {
        assertEquals("it''s ''quoted''", StringHelper.escapeForStringConstant("it's 'quoted'"));
    }

    @Test
    public void urlEncodeEncodesUsingUtf8() {
        assertEquals("hello+world%21", StringHelper.urlEncode("hello world!"));
    }

    @Test
    public void urlDecodeDecodesUsingUtf8() {
        assertEquals("hello world!", StringHelper.urlDecode("hello+world%21"));
    }

    @Test
    public void urlEncodeCanLeaveSlashesUnencoded() {
        assertEquals("a/b+c", StringHelper.urlEncode("a/b c", true));
    }

    @Test
    public void urlEncodeEncodesSlashesWhenRequested() {
        assertEquals("a%2Fb+c", StringHelper.urlEncode("a/b c", false));
    }

    @Test
    public void urlEncodeNotSlashesPreservesPathSeparators() {
        assertEquals("a/b/c", StringHelper.urlEncodeNotSlashes("a/b/c"));
    }
}
