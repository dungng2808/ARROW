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

    @Test(expected = IllegalArgumentException.class)
    public void testJumpConsistentHashWithNegativeBuckets() {
        JumpConsistentHash.jumpConsistentHash(123L, -1);
    }

    @Test
    public void testJumpConsistentHashWithZeroBuckets() {
        int result = JumpConsistentHash.jumpConsistentHash(123L, 0);
        assertEquals(0, result);
    }

    @Test
    public void testJumpConsistentHashWithOneBucket() {
        int result = JumpConsistentHash.jumpConsistentHash(123L, 1);
        assertEquals(0, result);
    }

    @Test
    public void testJumpConsistentHashWithLargeBuckets() {
        long key = 987654321L;
        int buckets = 1000;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithNegativeKey() {
        long key = -987654321L;
        int buckets = 5;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithZeroKey() {
        long key = 0L;
        int buckets = 5;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithMaxLongKey() {
        long key = Long.MAX_VALUE;
        int buckets = 5;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithMinLongKey() {
        long key = Long.MIN_VALUE;
        int buckets = 5;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithSameKeyDifferentBuckets() {
        long key = 12345L;
        int result1 = JumpConsistentHash.jumpConsistentHash(key, 5);
        int result2 = JumpConsistentHash.jumpConsistentHash(key, 10);
        assertTrue(result1 >= 0 && result1 < 5);
        assertTrue(result2 >= 0 && result2 < 10);
    }

    @Test
    public void testJumpConsistentHashWithSameBucketsDifferentKeys() {
        int buckets = 5;
        int result1 = JumpConsistentHash.jumpConsistentHash(123L, buckets);
        int result2 = JumpConsistentHash.jumpConsistentHash(456L, buckets);
        assertTrue(result1 >= 0 && result1 < buckets);
        assertTrue(result2 >= 0 && result2 < buckets);
    }
}
