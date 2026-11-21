package org.metafacture.biblio.iso2709;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Test;
import org.metafacture.framework.FormatException;

public class Record_ArrowTest {

    @Test
    public void shouldCreateRecordWithValidData() {
        byte[] recordData = createSampleRecordData();
        Record record = new Record(recordData);
        assertEquals(RecordFormat.ISO2709, record.getRecordFormat());
        assertEquals('c', record.getRecordStatus());
    }

    @Test(expected = FormatException.class)
    public void shouldThrowFormatExceptionForTooShortRecord() {
        byte[] shortRecord = new byte[]{0x30, 0x30, 0x30};
        new Record(shortRecord);
    }

    @Test(expected = FormatException.class)
    public void shouldThrowFormatExceptionForInvalidBaseAddress() {
        byte[] invalidBaseAddressRecord = createRecordWithInvalidBaseAddress();
        new Record(invalidBaseAddressRecord);
    }

    @Test
    public void shouldReturnNullForMissingRecordId() {
        byte[] recordWithoutId = createRecordWithoutId();
        Record record = new Record(recordWithoutId);
        assertNull(record.getRecordId());
    }

    @Test
    public void shouldReturnRecordIdWhenPresent() {
        byte[] recordWithData = createRecordWithData();
        Record record = new Record(recordWithData);
        assertEquals("123456789", record.getRecordId());
    }

    @Test
    public void shouldSetAndGetCharset() {
        byte[] recordData = createSampleRecordData();
        Record record = new Record(recordData);
        assertEquals(StandardCharsets.UTF_8, record.getCharset());
        record.setCharset(StandardCharsets.US_ASCII);
        assertEquals(StandardCharsets.US_ASCII, record.getCharset());
    }

    @Test
    public void shouldProcessFields() {
        byte[] recordData = createRecordWithData();
        Record record = new Record(recordData);
        TestFieldHandler handler = new TestFieldHandler();
        record.processFields(handler);
        assertTrue(handler.hasProcessedFields());
    }

    private byte[] createSampleRecordData() {
        // Create a minimal valid record with base address pointing to data area
        byte[] data = new byte[25];
        data[0] = 0x30; // Length: 00025
        data[1] = 0x30;
        data[2] = 0x30;
        data[3] = 0x30;
        data[4] = 0x32; // Base address: 00002
        data[5] = 0x35;
        data[6] = 0x30; // Record status: c
        data[7] = 0x30; // Implementation defined characters
        data[8] = 0x30;
        data[9] = 0x30;
        data[10] = 0x30;
        data[11] = 0x30;
        data[12] = 0x30;
        data[13] = 0x30;
        data[14] = 0x30;
        data[15] = 0x30;
        data[16] = 0x30;
        data[17] = 0x30;
        data[18] = 0x30;
        data[19] = 0x30;
        data[20] = 0x30;
        data[21] = 0x30;
        data[22] = 0x30;
        data[23] = 0x30;
        data[24] = 0x30;
        return data;
    }

    private byte[] createRecordWithInvalidBaseAddress() {
        // Create a record with base address beyond buffer length
        byte[] data = new byte[25];
        data[0] = 0x30; // Length: 00025
        data[1] = 0x30;
        data[2] = 0x30;
        data[3] = 0x30;
        data[4] = 0x39; // Base address: 00009 (invalid)
        data[5] = 0x39;
        data[6] = 0x30; // Record status: c
        data[7] = 0x30; // Implementation defined characters
        data[8] = 0x30;
        data[9] = 0x30;
        data[10] = 0x30;
        data[11] = 0x30;
        data[12] = 0x30;
        data[13] = 0x30;
        data[14] = 0x30;
        data[15] = 0x30;
        data[16] = 0x30;
        data[17] = 0x30;
        data[18] = 0x30;
        data[19] = 0x30;
        data[20] = 0x30;
        data[21] = 0x30;
        data[22] = 0x30;
        data[23] = 0x30;
        data[24] = 0x30;
        return data;
    }

    private byte[] createRecordWithoutId() {
        // Create a record with no record ID field
        byte[] data = new byte[30];
        data[0] = 0x30; // Length: 00030
        data[1] = 0x30;
        data[2] = 0x30;
        data[3] = 0x30;
        data[4] = 0x30; // Base address: 00000
        data[5] = 0x30;
        data[6] = 0x30; // Record status: c
        data[7] = 0x30; // Implementation defined characters
        data[8] = 0x30;
        data[9] = 0x30;
        data[10] = 0x30;
        data[11] = 0x30;
        data[12] = 0x30;
        data[13] = 0x30;
        data[14] = 0x30;
        data[15] = 0x30;
        data[16] = 0x30;
        data[17] = 0x30;
        data[18] = 0x30;
        data[19] = 0x30;
        data[20] = 0x30;
        data[21] = 0x30;
        data[22] = 0x30;
        data[23] = 0x30;
        data[24] = 0x30;
        data[25] = 0x30;
        data[26] = 0x30;
        data[27] = 0x30;
        data[28] = 0x30;
        data[29] = 0x30;
        return data;
    }

    private byte[] createRecordWithData() {
        // Create a record with a record ID field (tag 001)
        byte[] data = new byte[40];
        data[0] = 0x30; // Length: 00040
        data[1] = 0x30;
        data[2] = 0x30;
        data[3] = 0x30;
        data[4] = 0x30; // Base address: 00000
        data[5] = 0x30;
        data[6] = 0x30; // Record status: c
        data[7] = 0x30; // Implementation defined characters
        data[8] = 0x30;
        data[9] = 0x30;
        data[10] = 0x30;
        data[11] = 0x30;
        data[12] = 0x30;
        data[13] = 0x30;
        data[14] = 0x30;
        data[15] = 0x30;
        data[16] = 0x30;
        data[17] = 0x30;
        data[18] = 0x30;
        data[19] = 0x30;
        data[20] = 0x30;
        data[21] = 0x30;
        data[22] = 0x30;
        data[23] = 0x30;
        data[24] = 0x30;
        data[25] = 0x30;
        data[26] = 0x30;
        data[27] = 0x30;
        data[28] = 0x30;
        data[29] = 0x30;
        data[30] = 0x30;
        data[31] = 0x30;
        data[32] = 0x30;
        data[33] = 0x30;
        data[34] = 0x30;
        data[35] = 0x30;
        data[36] = 0x30;
        data[37] = 0x30;
        data[38] = 0x30;
        data[39] = 0x30;
        return data;
    }

    private static class TestFieldHandler implements FieldHandler {
        private boolean processedFields = false;

        @Override
        public void startDataField(String tag, char[] implDefinedPart, char[] indicators) {
            processedFields = true;
        }

        @Override
        public void data(char[] identifier, String data) {
            processedFields = true;
        }

        @Override
        public void endDataField() {
            processedFields = true;
        }

        @Override
        public void referenceField(String tag, char[] implDefinedPart, String value) {
            processedFields = true;
        }

        @Override
        public void additionalImplDefinedPart(char[] implDefinedPart) {
            processedFields = true;
        }

        public boolean hasProcessedFields() {
            return processedFields;
        }
    }
}
