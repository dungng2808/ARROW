package net.kylindb.coder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.hbase.async.Bytes;
import org.junit.Test;

import net.kylindb.domain.RowKey;
import net.kylindb.util.Constant;

public class RowKeyCoder_ArrowTest {

    @Test
    public void testEncode() {
        Long pointId = 123456789L;
        Integer baseTime = 987654321;
        
        byte[] result = RowKeyCoder.encode(pointId, baseTime);
        
        assertNotNull(result);
        assertEquals(RowKeyCoder.SALT_LENGTH + Constant.POINT_LENGTH + Constant.TIMESTAMP_LENGTH, result.length);
        
        // Check salt at beginning
        byte[] salt = new byte[RowKeyCoder.SALT_LENGTH];
        System.arraycopy(result, 0, salt, 0, RowKeyCoder.SALT_LENGTH);
        assertNotNull(salt);
        
        // Check pointId in middle
        byte[] pointIdBytes = new byte[Constant.POINT_LENGTH];
        System.arraycopy(result, RowKeyCoder.SALT_LENGTH, pointIdBytes, 0, Constant.POINT_LENGTH);
        long decodedPointId = Bytes.getLong(pointIdBytes);
        assertEquals(pointId, decodedPointId);
        
        // Check baseTime at end
        byte[] baseTimeBytes = new byte[Constant.TIMESTAMP_LENGTH];
        System.arraycopy(result, RowKeyCoder.SALT_LENGTH + Constant.POINT_LENGTH, baseTimeBytes, 0, Constant.TIMESTAMP_LENGTH);
        int decodedBaseTime = Bytes.getInt(baseTimeBytes);
        assertEquals(baseTime, decodedBaseTime);
    }

    @Test
    public void testDecode() {
        // Create a sample row key manually
        Long pointId = 987654321L;
        Integer baseTime = 123456789;
        
        byte[] encoded = RowKeyCoder.encode(pointId, baseTime);
        RowKey result = RowKeyCoder.decode(encoded);
        
        assertNotNull(result);
        assertEquals(pointId, result.getPointId());
        assertEquals(baseTime, result.getBaseTime());
    }

    @Test
    public void testGetSalt() {
        byte[] salt_base = {1, 2, 3, 4, 5, 6, 7, 8};
        byte[] result = RowKeyCoder.getSalt(salt_base);
        
        assertNotNull(result);
        assertEquals(RowKeyCoder.SALT_LENGTH, result.length);
    }

    @Test
    public void testGetSaltBytes() {
        int bucket = 100;
        byte[] result = RowKeyCoder.getSaltBytes(bucket);
        
        assertNotNull(result);
        assertEquals(RowKeyCoder.SALT_LENGTH, result.length);
        
        // Verify that the salt bytes represent the bucket value correctly
        int value = 0;
        for (int i = 0; i < result.length; i++) {
            value = (value << 8) | (result[i] & 0xFF);
        }
        assertEquals(bucket, value);
    }
}
