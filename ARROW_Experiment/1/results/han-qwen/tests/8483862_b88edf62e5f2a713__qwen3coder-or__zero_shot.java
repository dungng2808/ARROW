package org.metafacture.biblio.iso2709;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.metafacture.framework.FormatException;

public class Record_ArrowTest {

    @Test
    public void testGetRecordFormat() {
        final byte[] recordData = createValidRecordData();
        final Record record = new Record(recordData);
        assertEquals(RecordFormat.DEFAULT, record.getRecordFormat());
    }

    @Test
    public void testGetRecordStatus() {
        final byte[] recordData = createValidRecordData();
        final Record record = new Record(recordData);
        assertEquals(' ', record.getRecordStatus());
    }

    @Test
    public void testGetImplCodes() {
        final byte[] recordData = createValidRecordData();
        final Record record = new Record(recordData);
        assertArrayEquals(new char[] { ' ', ' ', ' ' }, record.getImplCodes());
    }

    @Test
    public void testGetSystemChars() {
        final byte[] recordData = createValidRecordData();
        final Record record = new Record(recordData);
        assertArrayEquals(new char[] { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
                record.getSystemChars());
    }

    @Test
    public void testGetReservedChar() {
        final byte[] recordData = createValidRecordData();
        final Record record = new Record(recordData);
        assertEquals(' ', record.getReservedChar());
    }

    @Test
    public void testSetAndGetCharset() {
        final byte[] recordData = createValidRecordData();
        final Record record = new Record(recordData);
        record.setCharset(StandardCharsets.US_ASCII);
        assertEquals(StandardCharsets.US_ASCII, record.getCharset());
    }

    @Test
    public void testGetRecordIdWithRecordIdField() {
        final byte[] recordData = createRecordWithDataFieldAndRecordId();
        final Record record = new Record(recordData);
        assertEquals("12345", record.getRecordId());
    }

    @Test
    public void testGetRecordIdWithoutRecordIdField() {
        final byte[] recordData = createValidRecordData();
        final Record record = new Record(recordData);
        assertNull(record.getRecordId());
    }

    @Test
    public void testProcessFields() {
        final byte[] recordData = createRecordWithDataFieldAndRecordId();
        final Record record = new Record(recordData);
        final TestFieldHandler handler = new TestFieldHandler();
        record.processFields(handler);
        assertTrue(handler.isCalled());
    }

    @Test(expected = FormatException.class)
    public void testConstructorWithShortRecordData() {
        final byte[] recordData = new byte[10];
        new Record(recordData);
    }

    @Test(expected = FormatException.class)
    public void testConstructorWithInvalidBaseAddress() {
        final byte[] recordData = createRecordWithInvalidBaseAddress();
        new Record(recordData);
    }

    private byte[] createValidRecordData() {
        // Create a minimal valid record with base address 24
        final byte[] recordData = new byte[100];
        recordData[0] = 0x30; // Record length (00024)
        recordData[1] = 0x30;
        recordData[2] = 0x30;
        recordData[3] = 0x32;
        recordData[4] = 0x34; // Base address (00024)
        recordData[5] = 0x30;
        recordData[6] = 0x30;
        recordData[7] = 0x30; // Record format (000)
        recordData[8] = 0x30;
        recordData[9] = 0x30; // Record status ( )
        recordData[10] = 0x20; // Implementation codes (   )
        recordData[11] = 0x20;
        recordData[12] = 0x20;
        recordData[13] = 0x20; // System chars (        )
        recordData[14] = 0x20;
        recordData[15] = 0x20;
        recordData[16] = 0x20;
        recordData[17] = 0x20;
        recordData[18] = 0x20;
        recordData[19] = 0x20;
        recordData[20] = 0x20; // Reserved char ( )
        recordData[21] = 0x20; // Field separator ( )
        recordData[22] = 0x20; // Identifier marker ( )
        recordData[23] = 0x20; // End of header
        return recordData;
    }

    private byte[] createRecordWithDataFieldAndRecordId() {
        // Create a record with a record ID field (001) and a data field (002)
        final byte[] recordData = new byte[100];
        recordData[0] = 0x30; // Record length (00040)
        recordData[1] = 0x30;
        recordData[2] = 0x30;
        recordData[3] = 0x34;
        recordData[4] = 0x30; // Base address (00040)
        recordData[5] = 0x30;
        recordData[6] = 0x30;
        recordData[7] = 0x30; // Record format (000)
        recordData[8] = 0x30;
        recordData[9] = 0x30; // Record status ( )
        recordData[10] = 0x20; // Implementation codes (   )
        recordData[11] = 0x20;
        recordData[12] = 0x20;
        recordData[13] = 0x20; // System chars (        )
        recordData[14] = 0x20;
        recordData[15] = 0x20;
        recordData[16] = 0x20;
        recordData[17] = 0x20;
        recordData[18] = 0x20;
        recordData[19] = 0x20;
        recordData[20] = 0x20; // Reserved char ( )
        recordData[21] = 0x20; // Field separator ( )
        recordData[22] = 0x20; // Identifier marker ( )
        recordData[23] = 0x20; // End of header

        // Add record ID field (001)
        recordData[24] = 0x30; // Tag (001)
        recordData[25] = 0x30;
        recordData[26] = 0x31;
        recordData[27] = 0x20; // Indicator length (1)
        recordData[28] = 0x20; // Identifier length (1)
        recordData[29] = 0x31; // Data (12345)
        recordData[30] = 0x32;
        recordData[31] = 0x33;
        recordData[32] = 0x34;
        recordData[33] = 0x35;
        recordData[34] = 0x20; // Field separator ( )

        // Add data field (002)
        recordData[35] = 0x30; // Tag (002)
        recordData[36] = 0x30;
        recordData[37] = 0x32;
        recordData[38] = 0x20; // Indicator length (1)
        recordData[39] = 0x20; // Identifier length (1)
        recordData[40] = 0x20; // Data ( )
        recordData[41] = 0x20; // Field separator ( )

        return recordData;
    }

    private byte[] createRecordWithInvalidBaseAddress() {
        // Create a record with invalid base address (too large)
        final byte[] recordData = new byte[100];
        recordData[0] = 0x30; // Record length (00024)
        recordData[1] = 0x30;
        recordData[2] = 0x30;
        recordData[3] = 0x32;
        recordData[4] = 0x39; // Base address (00029) - too large
        recordData[5] = 0x30;
        recordData[6] = 0x30;
        recordData[7] = 0x30; // Record format (000)
        recordData[8] = 0x30;
        recordData[9] = 0x30; // Record status ( )
        recordData[10] = 0x20; // Implementation codes (   )
        recordData[11] = 0x20;
        recordData[12] = 0x20;
        recordData[13] = 0x20; // System chars (        )
        recordData[14] = 0x20;
        recordData[15] = 0x20;
        recordData[16] = 0x20;
        recordData[17] = 0x20;
        recordData[18] = 0x20;
        recordData[19] = 0x20;
        recordData[20] = 0x20; // Reserved char ( )
        recordData[21] = 0x20; // Field separator ( )
        recordData[22] = 0x20; // Identifier marker ( )
        recordData[23] = 0x20; // End of header
        return recordData;
    }

    private static class TestFieldHandler implements FieldHandler {
        private boolean called = false;

        @Override
        public void startDataField(final char[] tag, final char[] implDefinedPart, final char[] indicators) {
            called = true;
        }

        @Override
        public void endDataField() {
        }

        @Override
        public void data(final char[] identifier, final String data) {
        }

        @Override
        public void referenceField(final char[] tag, final char[] implDefinedPart, final String value) {
        }

        @Override
        public void additionalImplDefinedPart(final char[] implDefinedPart) {
        }

        public boolean isCalled() {
            return called;
        }
    }
}
