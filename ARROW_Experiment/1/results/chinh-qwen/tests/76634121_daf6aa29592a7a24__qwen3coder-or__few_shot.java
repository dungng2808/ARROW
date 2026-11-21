package org.apache.qpid.proton.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
        assertSame(canonicalEncoding, ((StringType.AllStringEncoding) canonicalEncoding).getType());
    }

    @Test
    public void testGetAllEncodings() {
        Collection<StringType.StringEncoding> allEncodings = stringType.getAllEncodings();
        assertEquals(2, allEncodings.size());
        assertEquals(Arrays.asList(
                ((StringType) stringType)._shortStringEncoding,
                ((StringType) stringType)._stringEncoding),
                allEncodings);
    }

    @Test
    public void testCalculateUTF8Length_ASCIIOnly() {
        String asciiString = "Hello World";
        int length = StringType.calculateUTF8Length(asciiString);
        assertEquals(asciiString.length(), length);
    }

    @Test
    public void testCalculateUTF8Length_ExtendedChars() {
        String extendedString = "Hello 世界";
        int length = StringType.calculateUTF8Length(extendedString);
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
        assertEquals(EncodingCodes.STR8, encoding.getEncodingCode());
    }

    @Test
    public void testGetEncoding_LongString() {
        String longString = "This is a very long string that exceeds 255 characters to test the STR32 encoding code. " +
                           "It should trigger the long string encoding path in the implementation.";
        StringType.StringEncoding encoding = stringType.getEncoding(longString);
        assertEquals(EncodingCodes.STR32, encoding.getEncodingCode());
    }
}
