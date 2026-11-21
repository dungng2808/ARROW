package net.kylindb.coder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.kylindb.domain.RowKey;

public class RowKeyCoder_ArrowTest {

    @Test
    public void encodeProducesSaltPointIdAndBaseTimeBytes() {
        byte[] encoded = RowKeyCoder.encode(1L, 2);

        assertArrayEquals(new byte[] {
                0, 0, 0, 0, 0, 0, 0, 0, 1,
                0, 0, 0, 2
        }, encoded);
    }

    @Test
    public void decodeReturnsPointIdAndBaseTime() {
        byte[] encoded = RowKeyCoder.encode(123456789L, 987654321);

        RowKey decoded = RowKeyCoder.decode(encoded);

        assertEquals(Long.valueOf(123456789L), decoded.getPointId());
        assertEquals(Integer.valueOf(987654321), decoded.getBaseTime());
    }

    @Test
    public void getSaltReturnsHashBucketByte() {
        byte[] salt = RowKeyCoder.getSalt(new byte[] { 1, 2, 3 });

        assertArrayEquals(new byte[] { 33 }, salt);
    }

    @Test
    public void getSaltHandlesNegativeHashModulo() {
        byte[] salt = RowKeyCoder.getSalt(new byte[] { 0, 0, 0, 0, 0, 0, 0, 1 });

        assertArrayEquals(new byte[] { 1 }, salt);
    }
}
