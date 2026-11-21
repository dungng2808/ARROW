package org.apache.qpid.proton.codec;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class StringType_ArrowTest {

    private StringType stringType;
    private EncoderImpl encoder;
    private DecoderImpl decoder;

    @Before
    public void setUp() {
        encoder = mock(EncoderImpl.class);
        decoder = mock(DecoderImpl.class);
        stringType = new StringType(encoder, decoder);
    }

    @Test
    public void testGetTypeClass() {
        assertEquals(String.class, stringType.getTypeClass());
    }

    @Test
    public void testGetCanonicalEncoding() {
        StringType.StringEncoding canonicalEncoding = stringType.getCanonicalEncoding();
        assertNotNull(canonicalEncoding);
        assertTrue(canonicalEncoding instanceof StringType.AllStringEncoding);
    }

    @Test
    public void testGetAllEncodings() {
        Collection<StringType.StringEncoding> allEncodings = stringType.getAllEncodings();
        assertNotNull(allEncodings);
        assertEquals(2, allEncodings.size());
    }

    @Test
    public void testCalculateUTF8Length_ASCIIOnly() {
        String asciiString = "Hello World";
        int length = StringType.calculateUTF8Length(asciiString);
        assertEquals(asciiString.length(), length);
    }

    @Test
    public void testCalculateUTF8Length_WithUnicode() {
        String unicodeString = "Hello 世界";
        int length = StringType.calculateUTF8Length(unicodeString);
        // H=1, e=1, l=1, l=1, o=1, space=1, 世=3, 界=3
        assertEquals(10, length);
    }

    @Test
    public void testCalculateUTF8Length_EmptyString() {
        String emptyString = "";
        int length = StringType.calculateUTF8Length(emptyString);
        assertEquals(0, length);
    }

    @Test
    public void testExtendedCalculateUTF8Length() {
        String testString = "Hello 世界";
        int length = StringType.extendedCalculateUTF8Length(testString, 6, testString.length(), 6);
        // 世=3, 界=3
        assertEquals(6, length);
    }

    @Test
    public void testGetEncoding_ShortString() {
        String shortString = "Short";
        StringType.StringEncoding encoding = stringType.getEncoding(shortString);
        assertNotNull(encoding);
        assertTrue(encoding instanceof StringType.ShortStringEncoding);
    }

    @Test
    public void testGetEncoding_LongString() {
        StringBuilder longStringBuilder = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longStringBuilder.append('A');
        }
        String longString = longStringBuilder.toString();
        StringType.StringEncoding encoding = stringType.getEncoding(longString);
        assertNotNull(encoding);
        assertTrue(encoding instanceof StringType.AllStringEncoding);
    }

    @Test
    public void testGetEncoding_NullString() {
        StringType.StringEncoding encoding = stringType.getEncoding(null);
        assertNotNull(encoding);
    }

    @Test
    public void testGetEncoding_EmptyString() {
        StringType.StringEncoding encoding = stringType.getEncoding("");
        assertNotNull(encoding);
        assertTrue(encoding instanceof StringType.ShortStringEncoding);
    }
}
