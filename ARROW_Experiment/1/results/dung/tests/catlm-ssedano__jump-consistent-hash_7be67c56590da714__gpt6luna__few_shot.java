package com.github.ssedano.hash;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void resultIsWithinBucketRange() {
        int buckets = 10;
        int result = JumpConsistentHash.jumpConsistentHash(123456789L, buckets);
        assertTrue(result >= 0);
        assertTrue(result < buckets);
    }

    @Test
    public void objectOverloadUsesObjectHashCode() {
        String key = "example";
        assertEquals(
                JumpConsistentHash.jumpConsistentHash((long) key.hashCode(), 20),
                JumpConsistentHash.jumpConsistentHash((Object) key, 20));
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeBucketCountThrows() {
        JumpConsistentHash.jumpConsistentHash(1L, -1);
    }

    @Test(expected = NullPointerException.class)
    public void nullObjectThrows() {
        JumpConsistentHash.jumpConsistentHash((Object) null, 1);
    }
}
