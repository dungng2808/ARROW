package com.yuzhouwan.bigdata.kafka.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KafkaPartitioner_ArrowTest {

    @Test
    public void numericKeyUsesNumericRemainder() {
        assertEquals(2, KafkaPartitioner.getPartition("14", 4));
    }

    @Test
    public void negativeNumericKeyUsesAbsoluteRemainder() {
        assertEquals(2, KafkaPartitioner.getPartition("-14", 4));
    }

    @Test
    public void nonNumericKeyUsesHashCodeRemainder() {
        String key = "abc";
        assertEquals(Math.abs(key.hashCode() % 5), KafkaPartitioner.getPartition(key, 5));
    }

    @Test
    public void oneOrFewerPartitionsReturnsZero() {
        assertEquals(0, KafkaPartitioner.getPartition("key", 1));
        assertEquals(0, KafkaPartitioner.getPartition("key", 0));
    }

    @Test
    public void nullKeyReturnsValidRandomPartition() {
        int partition = KafkaPartitioner.getPartition(null, 7);
        assertTrue(partition >= 0 && partition < 7);
    }

    @Test
    public void emptyKeyReturnsValidRandomPartition() {
        int partition = KafkaPartitioner.getPartition("", 7);
        assertTrue(partition >= 0 && partition < 7);
    }

    @Test
    public void oversizedKeyReturnsValidRandomPartition() {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 1001; i++) {
            key.append('a');
        }
        int partition = KafkaPartitioner.getPartition(key.toString(), 7);
        assertTrue(partition >= 0 && partition < 7);
    }

    @Test
    public void instancePartitionDelegatesToStaticPartitioner() {
        KafkaPartitioner partitioner = new KafkaPartitioner();
        assertEquals(3, partitioner.partition("15", 4));
    }
}
