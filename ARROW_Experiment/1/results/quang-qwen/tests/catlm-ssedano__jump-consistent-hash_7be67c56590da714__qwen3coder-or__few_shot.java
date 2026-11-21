package com.github.ssedano.hash;

import org.junit.Test;
import static org.junit.Assert.*;

public class JumpConsistentHash_ArrowTest {

    @Test
    public void testJumpConsistentHashWithObjectAndBuckets() {
        Object obj = "test";
        int buckets = 10;
        int result = JumpConsistentHash.jumpConsistentHash(obj, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithKeyAndBuckets() {
        long key = 12345L;
        int buckets = 5;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJumpConsistentHashWithNegativeBuckets() {
        JumpConsistentHash.jumpConsistentHash(123L, -1);
    }

    @Test
    public void testJumpConsistentHashWithZeroBuckets() {
        int result = JumpConsistentHash.jumpConsistentHash(123L, 0);
        assertEquals(-1, result);
    }

    @Test
    public void testJumpConsistentHashWithOneBucket() {
        int result = JumpConsistentHash.jumpConsistentHash(123L, 1);
        assertEquals(0, result);
    }

    @Test
    public void testJumpConsistentHashWithMultipleCallsSameKey() {
        long key = 98765L;
        int buckets = 3;
        int result1 = JumpConsistentHash.jumpConsistentHash(key, buckets);
        int result2 = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertEquals(result1, result2);
    }

    @Test
    public void testJumpConsistentHashWithDifferentKeysSameBuckets() {
        int buckets = 4;
        int result1 = JumpConsistentHash.jumpConsistentHash(1L, buckets);
        int result2 = JumpConsistentHash.jumpConsistentHash(2L, buckets);
        // Results may be equal or different, but both should be within range
        assertTrue(result1 >= 0 && result1 < buckets);
        assertTrue(result2 >= 0 && result2 < buckets);
    }

    @Test
    public void testJumpConsistentHashWithLargeBuckets() {
        long key = 456L;
        int buckets = 1000;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithMaxLongKey() {
        long key = Long.MAX_VALUE;
        int buckets = 10;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithMinLongKey() {
        long key = Long.MIN_VALUE;
        int buckets = 10;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }
}
