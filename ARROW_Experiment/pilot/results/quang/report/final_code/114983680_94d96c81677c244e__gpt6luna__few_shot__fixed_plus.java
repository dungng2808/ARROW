package net.kylindb.coder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import net.kylindb.domain.RowKey;
import org.junit.Test;

public class RowKeyCoder_ArrowTest {

    @Test
    public void encodeProducesSaltPointIdAndBaseTimeBytes() {
        long pointId = 1L;
        int baseTime = 2;
        byte[] expected = new byte[] {
            (byte) 254, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2
        };

        assertArrayEquals(expected, RowKeyCoder.encode(pointId, baseTime));
    }

    @Test
    public void decodeReturnsPointIdAndBaseTime() {
        byte[] bytes = RowKeyCoder.encode(123456789L, 987654321);

        RowKey rowKey = RowKeyCoder.decode(bytes);

        assertEquals(Long.valueOf(123456789L), rowKey.getPointId());
        assertEquals(Integer.valueOf(987654321), rowKey.getBaseTime());
    }

    @Test
    public void getSaltReturnsHashBucketAsSingleByte() {
        byte[] saltBase = new byte[] {1, 2, 3};
        int bucket = Math.abs(java.util.Arrays.hashCode(saltBase) % RowKeyCoder.BUCKETS_SIZE);

        assertArrayEquals(new byte[] {(byte) bucket}, RowKeyCoder.getSalt(saltBase));
    }

    @Test
    public void getSaltHandlesNegativeModulo() {
        byte[] saltBase = new byte[] {0, 0, 0, 0, 0, 0, 0, 1};
        int modulo = java.util.Arrays.hashCode(saltBase) % RowKeyCoder.BUCKETS_SIZE;
        int bucket = modulo < 0 ? modulo * -1 : modulo;

        assertArrayEquals(new byte[] {(byte) bucket}, RowKeyCoder.getSalt(saltBase));
    }
}
