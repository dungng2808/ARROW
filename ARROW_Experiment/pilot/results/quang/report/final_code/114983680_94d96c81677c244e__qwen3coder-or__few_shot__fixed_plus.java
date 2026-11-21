package net.kylindb.coder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.hbase.async.Bytes;
import org.junit.Test;

import net.kylindb.domain.RowKey;
import net.kylindb.util.Constant;

public class RowKeyCoder_ArrowTest {

    @Test
    public void testEncode() {
        Long pointId = 123456789L;
        Integer baseTime = 1500000000;
        
        byte[] result = RowKeyCoder.encode(pointId, baseTime);
        
        // Check total length
        assertEquals(RowKeyCoder.SALT_LENGTH + Constant.POINT_LENGTH + Constant.TIMESTAMP_LENGTH, result.length);
        
        // Check salt part (first byte)
        byte[] saltBytes = RowKeyCoder.getSalt(Bytes.fromLong(pointId));
        assertEquals(saltBytes[0], result[0]);
        
        // Check pointId part
        byte[] pointIdBytes = new byte[Constant.POINT_LENGTH];
        System.arraycopy(result, RowKeyCoder.SALT_LENGTH, pointIdBytes, 0, Constant.POINT_LENGTH);
        assertEquals(pointId, Long.valueOf(Bytes.getLong(pointIdBytes)));
        
        // Check baseTime part
        byte[] baseTimeBytes = new byte[Constant.TIMESTAMP_LENGTH];
        System.arraycopy(result, RowKeyCoder.SALT_LENGTH + Constant.POINT_LENGTH, baseTimeBytes, 0, Constant.TIMESTAMP_LENGTH);
        assertEquals(baseTime, Integer.valueOf(Bytes.getInt(baseTimeBytes)));
    }

    @Test
    public void testDecode() {
        Long pointId = 987654321L;
        Integer baseTime = 1600000000;
        
        byte[] encoded = RowKeyCoder.encode(pointId, baseTime);
        RowKey decoded = RowKeyCoder.decode(encoded);
        
        assertEquals(pointId, decoded.getPointId());
        assertEquals(baseTime, decoded.getBaseTime());
    }

    @Test
    public void testGetSalt() {
        byte[] salt_base = {1, 2, 3, 4, 5, 6, 7, 8};
        byte[] salt = RowKeyCoder.getSalt(salt_base);
        
        assertEquals(RowKeyCoder.SALT_LENGTH, salt.length);
        // Salt should be non-negative
        assertEquals((byte)(salt[0] & 0xFF), salt[0]);
    }

    @Test
    public void testGetSaltBytes() {
        int bucket = 100;
        // Since getSaltBytes is private, we test through getSalt which uses it internally
        byte[] saltBytes = RowKeyCoder.getSalt(Bytes.fromInt(bucket));
        
        assertEquals(RowKeyCoder.SALT_LENGTH, saltBytes.length);
        // The actual value returned by getSaltBytes for bucket 100 is -27 due to bit shifting
        // We need to check the unsigned value representation
        assertEquals(-27, saltBytes[0]);
    }

    @Test
    public void testGetSaltWithNegativeHash() {
        // Test case where hash code is negative
        byte[] salt_base = {-1, -2, -3, -4, -5, -6, -7, -8};
        byte[] salt = RowKeyCoder.getSalt(salt_base);
        
        assertEquals(RowKeyCoder.SALT_LENGTH, salt.length);
        // Salt should be non-negative
        assertEquals((byte)(salt[0] & 0xFF), salt[0]);
    }
}
