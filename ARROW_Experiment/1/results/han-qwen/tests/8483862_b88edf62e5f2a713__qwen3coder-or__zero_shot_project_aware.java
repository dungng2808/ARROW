package org.metafacture.biblio.iso2709;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.metafacture.framework.FormatException;

public class Record_ArrowTest {

    @Test
    public void testRecordCreationWithValidData() {
        final byte[] recordData = createValidRecordData();
        final Record record = new Record(recordData);
        assertEquals(RecordFormat.ISO2709, record.getRecordFormat());
        assertEquals('c', record.getRecordStatus());
        assertArrayEquals(new char[] { ' ', ' ', ' ' }, record.getImplCodes());
        assertArrayEquals(new char[] { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' }, record.getSystemChars());
        assertEquals(' ', record.getReservedChar());
    }

    @Test(expected = FormatException.class)
    public void testRecordCreationWithTooShortData() {
        final byte[] recordData = new byte[10];
        new Record(recordData);
    }

    @Test(expected = FormatException.class)
    public void testRecordCreationWithInvalidBaseAddress() {
        final byte[] recordData = createRecordDataWithInvalidBaseAddress();
        new Record(recordData);
    }

    @Test
    public void testGetRecordIdWithRecordIdField() {
        final byte[] recordData = createRecordWithDataFieldAndRecordId();
        final Record record = new Record(recordData);
        assertEquals("12345", record.getRecordId());
    }

    @Test
    public void testGetRecordIdWithoutRecordIdField() {
        final byte[] recordData = createRecordWithDataFieldOnly();
        final Record record = new Record(recordData);
        assertNull(record.getRecordId());
    }

    @Test
    public void testSetAndGetCharset() {
        final byte[] recordData = createValidRecordData();
        final Record record = new Record(recordData);
        final Charset customCharset = StandardCharsets.US_ASCII;
        record.setCharset(customCharset);
        assertEquals(customCharset, record.getCharset());
    }

    private byte[] createValidRecordData() {
        // Create a minimal valid ISO 2709 record
        final byte[] data = new byte[25];
        data[0] = 0x30; // Length: 00025
        data[1] = 0x30;
        data[2] = 0x30;
        data[3] = 0x30;
        data[4] = 0x32; // Base Address: 00002
        data[5] = 0x35;
        data[6] = 0x30; // Record Format: 0
        data[7] = 0x63; // Record Status: c
        data[8] = 0x20; // Implementation Codes: [space, space, space]
        data[9] = 0x20;
        data[10] = 0x20;
        data[11] = 0x20; // System Characters: [space, space, space, space, space, space, space, space]
        data[12] = 0x20;
        data[13] = 0x20;
        data[14] = 0x20;
        data[15] = 0x20;
        data[16] = 0x20;
        data[17] = 0x20;
        data[18] = 0x20;
        data[19] = 0x30; // Directory Entry: Tag 001, Length 005, Start 00005
        data[20] = 0x30;
        data[21] = 0x31;
        data[22] = 0x20;
        data[23] = 0x20;
        data[24] = 0x20;
        return data;
    }

    private byte[] createRecordDataWithInvalidBaseAddress() {
        // Create a record with invalid base address
        final byte[] data = new byte[25];
        data[0] = 0x30; // Length: 00025
        data[1] = 0x30;
        data[2] = 0x30;
        data[3] = 0x30;
        data[4] = 0x39; // Base Address: 00009 (invalid)
        data[5] = 0x39;
        data[6] = 0x30; // Record Format: 0
        data[7] = 0x63; // Record Status: c
        data[8] = 0x20; // Implementation Codes: [space, space, space]
        data[9] = 0x20;
        data[10] = 0x20;
        data[11] = 0x20; // System Characters: [space, space, space, space, space, space, space, space]
        data[12] = 0x20;
        data[13] = 0x20;
        data[14] = 0x20;
        data[15] = 0x20;
        data[16] = 0x20;
        data[17] = 0x20;
        data[18] = 0x20;
        data[19] = 0x30; // Directory Entry: Tag 001, Length 005, Start 00005
        data[20] = 0x30;
        data[21] = 0x31;
        data[22] = 0x20;
        data[23] = 0x20;
        data[24] = 0x20;
        return data;
    }

    private byte[] createRecordWithDataFieldAndRecordId() {
        // Create a record with a record ID field (001) and a data field (002)
        final byte[] data = new byte[35];
        data[0] = 0x30; // Length: 00035
        data[1] = 0x30;
        data[2] = 0x30;
        data[3] = 0x30;
        data[4] = 0x30; // Base Address: 00000
        data[5] = 0x30;
        data[6] = 0x30; // Record Format: 0
        data[7] = 0x63; // Record Status: c
        data[8] = 0x20; // Implementation Codes: [space, space, space]
        data[9] = 0x20;
        data[10] = 0x20;
        data[11] = 0x20; // System Characters: [space, space, space, space, space, space, space, space]
        data[12] = 0x20;
        data[13] = 0x20;
        data[14] = 0x20;
        data[15] = 0x20;
        data[16] = 0x20;
        data[17] = 0x20;
        data[18] = 0x20;
        data[19] = 0x30; // Directory Entry: Tag 001, Length 0005, Start 00005
        data[20] = 0x30;
        data[21] = 0x31;
        data[22] = 0x20;
        data[23] = 0x20;
        data[24] = 0x20;
        data[25] = 0x30; // Directory Entry: Tag 002, Length 0005, Start 00010
        data[26] = 0x30;
        data[27] = 0x32;
        data[28] = 0x20;
        data[29] = 0x20;
        data[30] = 0x20;
        data[31] = 0x31; // Data: 12345
        data[32] = 0x32;
        data[33] = 0x33;
        data[34] = 0x34;
        return data;
    }

    private byte[] createRecordWithDataFieldOnly() {
        // Create a record with only a data field (002), no record ID field
        final byte[] data = new byte[30];
        data[0] = 0x30; // Length: 00030
        data[1] = 0x30;
        data[2] = 0x30;
        data[3] = 0x30;
        data[4] = 0x30; // Base Address: 00000
        data[5] = 0x30;
        data[6] = 0x30; // Record Format: 0
        data[7] = 0x63; // Record Status: c
        data[8] = 0x20; // Implementation Codes: [space, space, space]
        data[9] = 0x20;
        data[10] = 0x20;
        data[11] = 0x20; // System Characters: [space, space, space, space, space, space, space, space]
        data[12] = 0x20;
        data[13] = 0x20;
        data[14] = 0x20;
        data[15] = 0x20;
        data[16] = 0x20;
        data[17] = 0x20;
        data[18] = 0x20;
        data[19] = 0x30; // Directory Entry: Tag 002, Length 0005, Start 00005
        data[20] = 0x30;
        data[21] = 0x32;
        data[22] = 0x20;
        data[23] = 0x20;
        data[24] = 0x20;
        data[25] = 0x31; // Data: 12345
        data[26] = 0x32;
        data[27] = 0x33;
        data[28] = 0x34;
        data[29] = 0x35;
        return data;
    }
}
