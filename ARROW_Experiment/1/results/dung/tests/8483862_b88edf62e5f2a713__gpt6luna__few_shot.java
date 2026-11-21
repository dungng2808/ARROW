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
            fail("Expected an exception for null record data");
        } catch (Exception expected) {
            // Require.notNull rejects null.
        }
    }

    @Test
    public void rejectsRecordDataThatIsTooShort() {
        try {
            new Record(new byte[0]);
            fail("Expected a format exception for short record data");
        } catch (FormatException expected) {
            assertEquals("record is too short", expected.getMessage());
        }
    }

    @Test
    public void readsLabelValuesAndDefaultsToUtf8() {
        Record record = new Record(recordData("00000nam a2200000   4500"));
        assertEquals(RecordFormat.BIBLIOGRAPHIC, record.getRecordFormat());
        assertEquals('n', record.getRecordStatus());
        assertArrayEquals(new char[] {' ', ' ', ' ', ' ', ' ', ' '}, record.getImplCodes());
        assertArrayEquals(new char[] {' ', ' ', ' '}, record.getSystemChars());
        assertEquals(' ', record.getReservedChar());
        assertSame(StandardCharsets.UTF_8, record.getCharset());
    }

    @Test
    public void changesCharset() {
        Record record = new Record(recordData("00000nam a2200000   4500"));
        record.setCharset(StandardCharsets.ISO_8859_1);
        assertSame(StandardCharsets.ISO_8859_1, record.getCharset());
    }

    @Test
    public void returnsNullWhenRecordIdentifierIsMissing() {
        Record record = new Record(recordData("00000nam a2200000   4500"));
        assertNull(record.getRecordId());
    }

    private byte[] recordData(String label) {
        byte[] data = new byte[25];
        byte[] labelBytes = label.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(labelBytes, 0, data, 0, labelBytes.length);
        data[24] = 0x1d;
        return data;
    }
}
