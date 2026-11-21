package org.metafacture.biblio.iso2709;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.metafacture.framework.FormatException;

public class Record_ArrowTest {

    @Test
    public void rejectsNullRecordData() {
        try {
            new Record(null);
            fail("Expected an exception");
        } catch (RuntimeException expected) {
            // Require.notNull rejects null.
        }
    }

    @Test
    public void rejectsRecordDataThatIsTooShort() {
        try {
            new Record(new byte[0]);
            fail("Expected an exception");
        } catch (FormatException expected) {
            assertEquals("record is too short", expected.getMessage());
        }
    }

    @Test
    public void rejectsRecordWithInvalidBaseAddress() {
        byte[] data = new byte[25];
        try {
            new Record(data);
            fail("Expected an exception");
        } catch (FormatException expected) {
            assertEquals("base address is out of range", expected.getMessage());
        }
    }

    @Test
    public void defaultsToUtf8Charset() {
        Record record = new Record(minimalRecord());
        assertSame(StandardCharsets.UTF_8, record.getCharset());
    }

    @Test
    public void setsAndReturnsCharset() {
        Record record = new Record(minimalRecord());
        Charset charset = StandardCharsets.ISO_8859_1;
        record.setCharset(charset);
        assertSame(charset, record.getCharset());
    }

    @Test
    public void returnsNullWhenRecordIdentifierIsMissing() {
        Record record = new Record(minimalRecord());
        assertNull(record.getRecordId());
    }

    @Test
    public void returnsLabelValues() {
        Record record = new Record(minimalRecord());
        assertEquals(RecordFormat.BIBLIOGRAPHIC, record.getRecordFormat());
        assertEquals('n', record.getRecordStatus());
        assertArrayEquals(new char[] {'0', '0', '0', '0'}, record.getImplCodes());
        assertArrayEquals(new char[] {' ', ' '}, record.getSystemChars());
        assertEquals(' ', record.getReservedChar());
    }

    private static byte[] minimalRecord() {
        byte[] data = new byte[26];
        put(data, 0, "00026");
        data[5] = 'n';
        data[6] = 'a';
        data[7] = ' ';
        data[8] = ' ';
        put(data, 10, "00000");
        put(data, 12, "00025");
        data[17] = ' ';
        data[18] = ' ';
        data[19] = '0';
        data[20] = '4';
        data[21] = '0';
        data[22] = '0';
        put(data, 23, "00");
        data[25] = 0x1d;
        return data;
    }

    private static void put(byte[] target, int offset, String value) {
        for (int i = 0; i < value.length(); ++i) {
            target[offset + i] = (byte) value.charAt(i);
        }
    }
}
