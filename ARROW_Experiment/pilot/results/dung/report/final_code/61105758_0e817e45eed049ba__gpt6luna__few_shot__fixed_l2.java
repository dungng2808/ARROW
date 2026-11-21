package com.hazelcast.hibernate.access;

import com.hazelcast.hibernate.region.HazelcastRegion;
import org.hibernate.cache.spi.access.SoftLock;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class ReadWriteAccessDelegate_ArrowTest {

    private ReadWriteAccessDelegate<HazelcastRegion> newDelegate() {
        return new ReadWriteAccessDelegate<HazelcastRegion>(mock(HazelcastRegion.class), new Properties());
    }

    @Test
    public void insertReturnsFalse() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        assertFalse(delegate.insert("key", "value", 1));
    }

    @Test
    public void updateReturnsFalse() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        assertFalse(delegate.update("key", "value", 2, 1));
    }

    @Test
    public void removeAndRemoveAllAreNoOps() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        delegate.remove("key");
        delegate.removeAll();
        assertFalse(delegate.update("key", "value", 2, 1));
    }

    @Test
    public void lockItemThrowsWhenCacheUnavailable() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        try {
            delegate.lockItem("key", 1);
        } catch (NullPointerException expected) {
            assertNull(null);
            return;
        }
        throw new AssertionError("Expected a NullPointerException when the mock region has no cache");
    }

    @Test
    public void unlockItemThrowsWhenCacheUnavailable() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        try {
            delegate.unlockItem("key", (SoftLock) null);
        } catch (NullPointerException expected) {
            assertNull(null);
            return;
        }
        throw new AssertionError("Expected a NullPointerException when the mock region has no cache");
    }
}
