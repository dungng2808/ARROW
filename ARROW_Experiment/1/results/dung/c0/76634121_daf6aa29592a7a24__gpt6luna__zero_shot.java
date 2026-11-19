package org.apache.qpid.proton.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

public class StringType_ArrowTest
{
    @Test
    public void reportsStringTypeClass()
    {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());

        assertEquals(String.class, type.getTypeClass());
    }

    @Test
    public void returnsCanonicalEncodingAndBothEncodings()
    {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());

        StringType.StringEncoding canonical = type.getCanonicalEncoding();
        Collection<StringType.StringEncoding> encodings = type.getAllEncodings();

        assertNotNull(canonical);
        assertEquals(2, encodings.size());
        assertTrue(encodings.contains(canonical));
        assertSame(canonical, encodings.toArray()[1]);
    }

    @Test
    public void selectsShortEncodingForEmptyAndAsciiStrings()
    {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());

        StringType.StringEncoding emptyEncoding = type.getEncoding("");
        StringType.StringEncoding asciiEncoding = type.getEncoding("hello");

        assertEquals(EncodingCodes.STR8, emptyEncoding.getEncodingCode());
        assertSame(emptyEncoding, asciiEncoding);
    }

    @Test
    public void selectsShortEncodingAtUtf8LengthBoundary()
    {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 255; i++)
        {
            value.append('a');
        }

        assertEquals(EncodingCodes.STR8, type.getEncoding(value.toString()).getEncodingCode());
    }

    @Test
    public void selectsLongEncodingWhenUtf8LengthExceedsBoundary()
    {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 256; i++)
        {
            value.append('a');
        }

        StringType.StringEncoding encoding = type.getEncoding(value.toString());

        assertEquals(EncodingCodes.STR32, encoding.getEncodingCode());
        assertSame(type.getCanonicalEncoding(), encoding);
    }

    @Test
    public void selectsEncodingUsingUtf8ByteLength()
    {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());

        assertEquals(EncodingCodes.STR8, type.getEncoding(repeat("\u0080", 127)).getEncodingCode());
        assertEquals(EncodingCodes.STR32, type.getEncoding(repeat("\u0080", 128)).getEncodingCode());
    }

    @Test
    public void shortEncodingOnlyEncodesItselfAsSuperset()
    {
        StringType type = new StringType(new EncoderImpl(), new DecoderImpl());
        StringType.StringEncoding shortEncoding = type.getEncoding("short");
        StringType.StringEncoding longEncoding = type.getCanonicalEncoding();

        assertTrue(shortEncoding.encodesSuperset(shortEncoding));
        assertFalse(shortEncoding.encodesSuperset(longEncoding));
        assertTrue(longEncoding.encodesSuperset(shortEncoding));
        assertTrue(longEncoding.encodesSuperset(longEncoding));
    }

    private static String repeat(String value, int count)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++)
        {
            result.append(value);
        }
        return result.toString();
    }
}
