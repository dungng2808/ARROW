package com.yuzhouwan.bigdata.kafka.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KafkaPartitioner_ArrowTest {

    @Test
    public void getPartitionReturnsZeroWhenThereIsAtMostOnePartition() {
        assertEquals(0, KafkaPartitioner.getPartition("key", 1));
        assertEquals(0, KafkaPartitioner.getPartition("key", 0));
    }

    @Test
    public void getPartitionUsesNumericKeyModuloPartitionCount() {
        assertEquals(2, KafkaPartitioner.getPartition("8", 3));
        assertEquals(1, KafkaPartitioner.getPartition("-8", 3));
    }

    @Test
    public void getPartitionUsesHashCodeForNonNumericKey() {
        String key = "abc";
        assertEquals(Math.abs(key.hashCode() % 7), KafkaPartitioner.getPartition(key, 7));
    }

    @Test
    public void partitionDelegatesToGetPartition() {
        KafkaPartitioner partitioner = new KafkaPartitioner();
        assertEquals(2, partitioner.partition("8", 3));
    }
}
