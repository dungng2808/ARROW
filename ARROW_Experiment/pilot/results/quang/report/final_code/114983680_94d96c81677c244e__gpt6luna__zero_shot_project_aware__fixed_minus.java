package net.kylindb.coder;

import net.kylindb.domain.RowKey;
import org.hbase.async.Bytes;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RowKeyCoder_ArrowTest {

    @Test
    public void encodeProducesSaltPointIdAndBaseTimeBytes() {
        Long pointId = 1L;
        Integer baseTime = 2;

        byte[] expected = new byte[13];
        expected[0] = RowKeyCoder.getSalt(Bytes.fromLong(pointId))[0];
        System.arraycopy(Bytes.fromLong(pointId), 0, expected, 1, 8);
        System.arraycopy(Bytes.fromInt(baseTime), 0, expected, 9, 4);

        assertArrayEquals(expected, RowKeyCoder.encode(pointId, baseTime));
    }

    @Test
    public void decodeReturnsPointIdAndBaseTime() {
        byte[] encoded = RowKeyCoder.encode(123456789L, 987654321);

        RowKey decoded = RowKeyCoder.decode(encoded);

        assertEquals(Long.valueOf(123456789L), decoded.getPointId());
        assertEquals(Integer.valueOf(987654321), decoded.getBaseTime());
    }

    @Test
    public void getSaltReturnsHashBucketAsSingleByte() {
        byte[] input = Bytes.fromLong(1L);
        int modulo = java.util.Arrays.hashCode(input) % RowKeyCoder.BUCKETS_SIZE;
        int bucket = modulo < 0 ? -modulo : modulo;

        assertArrayEquals(new byte[] {(byte) bucket}, RowKeyCoder.getSalt(input));
    }

    @Test
    public void getSaltHandlesNegativeHashModulo() {
        byte[] input = Bytes.fromLong(0L);
        int modulo = java.util.Arrays.hashCode(input) % RowKeyCoder.BUCKETS_SIZE;
        if (modulo >= 0) {
            input = Bytes.fromLong(2L);
            modulo = java.util.Arrays.hashCode(input) % RowKeyCoder.BUCKETS_SIZE;
        }
        int expected = modulo < 0 ? modulo * -1 : modulo;

        assertArrayEquals(new byte[] {(byte) expected}, RowKeyCoder.getSalt(input));
    }
}
