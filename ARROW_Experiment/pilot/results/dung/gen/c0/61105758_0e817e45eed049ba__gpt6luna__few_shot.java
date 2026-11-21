package com.hazelcast.hibernate.access;

import com.hazelcast.hibernate.region.HazelcastRegion;
import org.hibernate.cache.spi.access.SoftLock;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class ReadWriteAccessDelegate_ArrowTest {

    @Test
    public void insertReturnsFalse() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(null, new Properties());

        assertFalse(delegate.insert("key", "value", 1));
    }

    @Test
    public void updateReturnsFalse() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(null, new Properties());

        assertFalse(delegate.update("key", "value", 2, 1));
    }

    @Test
    public void removeAndRemoveAllAreNoOps() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(null, new Properties());

        delegate.remove("key");
        delegate.removeAll();
    }

    @Test
    public void lockItemReturnsNullWhenRegionIsNull() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(null, new Properties());

        assertNull(delegate.lockItem("key", 1));
    }

    @Test
    public void unlockItemWithNullLockDoesNotThrowWhenRegionIsNull() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(null, new Properties());

        delegate.unlockItem("key", (SoftLock) null);
    }
}
