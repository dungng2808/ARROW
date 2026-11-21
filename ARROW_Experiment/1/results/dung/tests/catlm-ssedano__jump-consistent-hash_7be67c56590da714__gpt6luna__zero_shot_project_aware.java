package com.github.ssedano.hash;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JumpConsistentHash_ArrowTest {

    @Test
    public void zeroBucketsReturnsMinusOne() {
        assertEquals(-1, JumpConsistentHash.jumpConsistentHash(123L, 0));
    }

    @Test
    public void oneBucketReturnsZero() {
        assertEquals(0, JumpConsistentHash.jumpConsistentHash(123L, 1));
    }

    @Test
    public void knownKeyAndBucketCountsReturnExpectedBuckets() {
        assertEquals(0, JumpConsistentHash.jumpConsistentHash(0L, 2));
        assertEquals(1, JumpConsistentHash.jumpConsistentHash(1L, 2));
        assertEquals(2, JumpConsistentHash.jumpConsistentHash(0L, 3));
    }

    @Test
    public void objectOverloadUsesObjectHashCode() {
        Object key = new Object() {
            @Override
            public int hashCode() {
                return 1;
            }
        };

        assertEquals(1, JumpConsistentHash.jumpConsistentHash(key, 2));
    }

    @Test
    public void negativeKeyProducesBucketInRange() {
        int bucket = JumpConsistentHash.jumpConsistentHash(-1L, 10);

        assertEquals(0, bucket);
    }

    @Test
    public void negativeBucketCountThrowsIllegalArgumentException() {
        try {
            JumpConsistentHash.jumpConsistentHash(0L, -1);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}
