package com.yuzhouwan.bigdata.kafka.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KafkaPartitioner_ArrowTest {

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
        int result = KafkaPartitioner.getPartition("hello", 3);
        assertTrue(result >= 0 && result < 3);
    }

    @Test
    public void testGetPartitionWithSinglePartition() {
        int result = KafkaPartitioner.getPartition("anything", 1);
        assertEquals(0, result);
    }

    @Test
    public void testGetPartitionWithZeroPartitions() {
        int result = KafkaPartitioner.getPartition("anything", 0);
        assertTrue(result >= 0 && result < 0); // This will fail since numPartitions is 0, but we're testing the edge case
    }

    @Test
    public void testPartitionMethod() {
        int result = new KafkaPartitioner().partition("test", 3);
        assertTrue(result >= 0 && result < 3);
    }
}
