package com.yuzhouwan.bigdata.kafka.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KafkaPartitioner_ArrowTest {

    @Test
    public void getPartitionReturnsZeroWhenThereIsAtMostOnePartition() {
        assertEquals(0, KafkaPartitioner.getPartition("123", 1));
        assertEquals(0, KafkaPartitioner.getPartition("123", 0));
    }

    @Test
    public void getPartitionUsesNumericKeyModuloPartitionCount() {
        assertEquals(2, KafkaPartitioner.getPartition("17", 5));
        assertEquals(3, KafkaPartitioner.getPartition("-17", 5));
    }

    @Test
    public void getPartitionUsesHashCodeForNonNumericKey() {
        String key = "abc";
        assertEquals(Math.abs(key.hashCode() % 7), KafkaPartitioner.getPartition(key, 7));
    }

    @Test
    public void getPartitionRandomlyAssignsNullKeyWithinRange() {
        int partition = KafkaPartitioner.getPartition(null, 4);
        assertTrue(partition >= 0 && partition < 4);
    }

    @Test
    public void partitionDelegatesToGetPartition() {
        KafkaPartitioner partitioner = new KafkaPartitioner();
        assertEquals(2, partitioner.partition("12", 5));
    }
}
