package com.yuzhouwan.bigdata.kafka.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class KafkaPartitioner_ArrowTest {

    @Test
    public void testGetPartitionWithNumPartitionsLessThanOne() {
        int result = KafkaPartitioner.getPartition("test", 0);
        assertEquals(0, result);
    }

    @Test
    public void testGetPartitionWithNullKey() {
        int result = KafkaPartitioner.getPartition(null, 3);
        assertTrue(result >= 0 && result < 3);
    }

    @Test
    public void testGetPartitionWithEmptyKey() {
        int result = KafkaPartitioner.getPartition("", 3);
        assertTrue(result >= 0 && result < 3);
    }

    @Test
    public void testGetPartitionWithLongKey() {
        StringBuilder longKey = new StringBuilder();
        for (int i = 0; i < 1001; i++) {
            longKey.append("a");
        }
        int result = KafkaPartitioner.getPartition(longKey.toString(), 3);
        assertTrue(result >= 0 && result < 3);
    }

    @Test
    public void testGetPartitionWithNumericKey() {
        int result = KafkaPartitioner.getPartition("123", 5);
        assertEquals(3, result);
    }

    @Test
    public void testGetPartitionWithNonNumericKey() {
        int result = KafkaPartitioner.getPartition("abc", 3);
        assertTrue(result >= 0 && result < 3);
    }

    @Test
    public void testGetPartitionWithNumericKeyNegative() {
        int result = KafkaPartitioner.getPartition("-123", 5);
        assertEquals(2, result);
    }

    @Test
    public void testPartitionMethod() {
        KafkaPartitioner partitioner = new KafkaPartitioner();
        int result = partitioner.partition("test", 3);
        assertTrue(result >= 0 && result < 3);
    }
}
