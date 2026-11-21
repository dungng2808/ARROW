package org.apache.qpid.proton.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.Test;

public class StringType_ArrowTest {

    @Test
    public void reportsStringTypeClass() {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());

        assertEquals(String.class, type.getTypeClass());
    }

    @Test
    public void returnsCanonicalEncodingAndAllEncodings() {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());

        StringType.StringEncoding canonical = type.getCanonicalEncoding();
        Collection<StringType.StringEncoding> encodings = type.getAllEncodings();

        assertNotNull(canonical);
        assertEquals(2, encodings.size());
        assertTrue(encodings.contains(canonical));
    }

    @Test
    public void selectsShortEncodingForStringsUpTo255Utf8Bytes() {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());

        StringType.StringEncoding encoding = type.getEncoding("hello");

        assertSame(type.getAllEncodings().iterator().next(), encoding);
        assertEquals(EncodingCodes.STR8, encoding.getEncodingCode());
        assertSame(type, encoding.getType());
    }

    @Test
    public void selectsLongEncodingForStringsOver255Utf8Bytes() {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            value.append('a');
        }

        StringType.StringEncoding encoding = type.getEncoding(value.toString());

        assertSame(type.getCanonicalEncoding(), encoding);
        assertEquals(EncodingCodes.STR32, encoding.getEncodingCode());
        assertSame(type, encoding.getType());
    }

    @Test
    public void selectsEncodingUsingUtf8ByteLength() {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            value.append('\u00e9');
        }

        StringType.StringEncoding encoding = type.getEncoding(value.toString());

        assertEquals(EncodingCodes.STR8, encoding.getEncodingCode());
    }

    @Test
    public void shortEncodingOnlySupersetsItself() {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());
        StringType.StringEncoding shortEncoding = type.getEncoding("short");
        StringType.StringEncoding longEncoding = type.getCanonicalEncoding();

        assertTrue(shortEncoding.encodesSuperset(shortEncoding));
        assertTrue(!shortEncoding.encodesSuperset(longEncoding));
    }

    @Test
    public void longEncodingSupersetsEncodingsOfTheSameType() {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());
        StringType.StringEncoding longEncoding = type.getCanonicalEncoding();
        StringType.StringEncoding shortEncoding = type.getEncoding("short");

        assertTrue(longEncoding.encodesSuperset(shortEncoding));
        assertTrue(longEncoding.encodesSuperset(longEncoding));
    }
}
