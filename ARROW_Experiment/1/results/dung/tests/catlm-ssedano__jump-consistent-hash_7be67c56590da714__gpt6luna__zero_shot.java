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
    public void knownKeyAndBucketCountReturnExpectedBucket() {
        assertEquals(6, JumpConsistentHash.jumpConsistentHash(0L, 10));
    }

    @Test
    public void objectOverloadUsesObjectHashCode() {
        Integer key = Integer.valueOf(123);
        assertEquals(JumpConsistentHash.jumpConsistentHash(123L, 10),
                JumpConsistentHash.jumpConsistentHash((Object) key, 10));
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
