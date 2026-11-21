package com.github.ssedano.hash;

import org.junit.Test;
import static org.junit.Assert.*;

public class JumpConsistentHash_ArrowTest {

    @Test
    public void testJumpConsistentHashWithObjectAndBuckets() {
        String obj = "test";
        int buckets = 5;
        int result = JumpConsistentHash.jumpConsistentHash(obj, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithKeyAndBuckets() {
        long key = 12345L;
        int buckets = 10;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithZeroBuckets() {
        long key = 12345L;
        int buckets = 0;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertEquals(0, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJumpConsistentHashWithNegativeBuckets() {
        long key = 12345L;
        int buckets = -1;
        JumpConsistentHash.jumpConsistentHash(key, buckets);
    }

    @Test
    public void testJumpConsistentHashWithLargeKey() {
        long key = Long.MAX_VALUE;
        int buckets = 3;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithSmallKey() {
        long key = Long.MIN_VALUE;
        int buckets = 4;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithSameKeyDifferentBuckets() {
        long key = 98765L;
        int buckets1 = 3;
        int buckets2 = 5;
        int result1 = JumpConsistentHash.jumpConsistentHash(key, buckets1);
        int result2 = JumpConsistentHash.jumpConsistentHash(key, buckets2);
        assertTrue(result1 >= 0 && result1 < buckets1);
        assertTrue(result2 >= 0 && result2 < buckets2);
    }

    @Test
    public void testJumpConsistentHashWithSameBucketsDifferentKeys() {
        int buckets = 7;
        long key1 = 111L;
        long key2 = 222L;
        int result1 = JumpConsistentHash.jumpConsistentHash(key1, buckets);
        int result2 = JumpConsistentHash.jumpConsistentHash(key2, buckets);
        assertTrue(result1 >= 0 && result1 < buckets);
        assertTrue(result2 >= 0 && result2 < buckets);
    }

    @Test
    public void testJumpConsistentHashWithOneBucket() {
        long key = 555L;
        int buckets = 1;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertEquals(0, result);
    }

    @Test
    public void testJumpConsistentHashWithLargeBuckets() {
        long key = 777L;
        int buckets = 1000;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithNullObject() {
        Object obj = null;
        int buckets = 5;
        int result = JumpConsistentHash.jumpConsistentHash(obj, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithStringObject() {
        String obj = "hello world";
        int buckets = 8;
        int result = JumpConsistentHash.jumpConsistentHash(obj, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithIntegerObject() {
        Integer obj = 42;
        int buckets = 6;
        int result = JumpConsistentHash.jumpConsistentHash(obj, buckets);
        assertTrue(result >= 0 && result < buckets);
    }
}
