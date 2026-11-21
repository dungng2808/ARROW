package com.github.ssedano.hash;

import org.junit.Test;
import static org.junit.Assert.*;

public class JumpConsistentHash_ArrowTest {

    @Test
    public void testJumpConsistentHashWithObjectAndBuckets() {
        String obj = "test";
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
    public void testJumpConsistentHashWithLargeBuckets() {
        long key = 987654321L;
        int buckets = 1000;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithSameKeyDifferentBuckets() {
        long key = 55555L;
        int buckets1 = 10;
        int buckets2 = 20;
        int result1 = JumpConsistentHash.jumpConsistentHash(key, buckets1);
        int result2 = JumpConsistentHash.jumpConsistentHash(key, buckets2);
        assertTrue(result1 >= 0 && result1 < buckets1);
        assertTrue(result2 >= 0 && result2 < buckets2);
    }

    @Test
    public void testJumpConsistentHashWithNegativeKey() {
        long key = -987654321L;
        int buckets = 5;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithMaxLongKey() {
        long key = Long.MAX_VALUE;
        int buckets = 100;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithMinLongKey() {
        long key = Long.MIN_VALUE;
        int buckets = 50;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithZeroKey() {
        long key = 0L;
        int buckets = 3;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }

    @Test
    public void testJumpConsistentHashWithOneBucket() {
        long key = 12345L;
        int buckets = 1;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertEquals(0, result);
    }

    @Test
    public void testJumpConsistentHashWithTwoBuckets() {
        long key = 12345L;
        int buckets = 2;
        int result = JumpConsistentHash.jumpConsistentHash(key, buckets);
        assertTrue(result >= 0 && result < buckets);
    }
}
