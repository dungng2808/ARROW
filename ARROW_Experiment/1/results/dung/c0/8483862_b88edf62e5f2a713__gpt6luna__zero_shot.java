package org.metafacture.biblio.iso2709;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

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
            fail("Expected a format exception");
        } catch (FormatException expected) {
            assertEquals("record is too short", expected.getMessage());
        }
    }

    @Test
    public void rejectsNullCharset() {
        Record record = new Record(minimalRecord());
        try {
            record.setCharset(null);
            fail("Expected an exception");
        } catch (RuntimeException expected) {
            // Require.notNull rejects null.
        }
    }

    @Test
    public void defaultsToUtf8AndAllowsChangingCharset() {
        Record record = new Record(minimalRecord());
        assertSame(StandardCharsets.UTF_8, record.getCharset());

        record.setCharset(StandardCharsets.ISO_8859_1);
        assertSame(StandardCharsets.ISO_8859_1, record.getCharset());
    }

    @Test
    public void exposesLabelValues() {
        Record record = new Record(minimalRecord());

        assertEquals(RecordFormat.MARC21, record.getRecordFormat());
        assertEquals('n', record.getRecordStatus());
        assertArrayEquals(new char[] {'0', '0', '0'}, record.getImplCodes());
        assertArrayEquals(new char[] {'a', 'a'}, record.getSystemChars());
        assertEquals(' ', record.getReservedChar());
    }

    @Test
    public void returnsNullWhenRecordIdentifierIsMissing() {
        Record record = new Record(minimalRecord());
        assertNull(record.getRecordId());
    }

    private static byte[] minimalRecord() {
        byte[] record = new byte[26];
        put(record, 0, "00026");
        record[5] = 'n';
        record[6] = ' ';
        record[7] = 'a';
        record[8] = 'a';
        record[9] = '0';
        record[10] = '0';
        record[11] = '0';
        record[12] = ' ';
        record[13] = ' ';
        put(record, 14, "00000");
        put(record, 19, "00000");
        record[24] = ' ';
        record[25] = 0x1d;
        return record;
    }

    private static void put(byte[] target, int offset, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(bytes, 0, target, offset, bytes.length);
    }
}
