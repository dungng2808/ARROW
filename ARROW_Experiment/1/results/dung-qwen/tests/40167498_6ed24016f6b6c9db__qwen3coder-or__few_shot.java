package com.yuzhouwan.bigdata.kafka.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class KafkaPartitioner_ArrowTest {

    @Test
    public void testGetPartitionWithNullKey() {
        int partition = KafkaPartitioner.getPartition(null, 3);
        assertTrue(partition >= 0 && partition < 3);
    }

    @Test
    public void testGetPartitionWithEmptyKey() {
        int partition = KafkaPartitioner.getPartition("", 3);
        assertTrue(partition >= 0 && partition < 3);
    }

    @Test
    public void testGetPartitionWithLongKey() {
        StringBuilder longKey = new StringBuilder();
        for (int i = 0; i < 1001; i++) {
            longKey.append("a");
        }
        int partition = KafkaPartitioner.getPartition(longKey.toString(), 3);
        assertTrue(partition >= 0 && partition < 3);
    }

    @Test
    public void testGetPartitionWithNumericKey() {
        int partition = KafkaPartitioner.getPartition("123", 5);
        assertEquals(3, partition);
    }

    @Test
    public void testGetPartitionWithNonNumericKey() {
        int partition = KafkaPartitioner.getPartition("abc", 3);
        assertTrue(partition >= 0 && partition < 3);
    }

    @Test
    public void testGetPartitionWithSinglePartition() {
        int partition = KafkaPartitioner.getPartition("any_key", 1);
        assertEquals(0, partition);
    }

    @Test
    public void testGetPartitionWithZeroPartitions() {
        int partition = KafkaPartitioner.getPartition("any_key", 0);
        assertEquals(0, partition);
    }

    @Test
    public void testPartitionMethod() {
        KafkaPartitioner partitioner = new KafkaPartitioner();
        int partition = partitioner.partition("test_key", 3);
        assertTrue(partition >= 0 && partition < 3);
    }
}
