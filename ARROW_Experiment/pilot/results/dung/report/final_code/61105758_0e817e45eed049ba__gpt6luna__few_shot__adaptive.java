package com.hazelcast.hibernate.access;

import com.hazelcast.hibernate.region.HazelcastRegion;
import org.hibernate.cache.spi.access.SoftLock;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class ReadWriteAccessDelegate_ArrowTest {

    @Test
    public void insertReturnsFalse() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        assertFalse(delegate.insert("key", "value", 1));
    }

    @Test
    public void updateReturnsFalse() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        assertFalse(delegate.update("key", "value", 2, 1));
    }

    @Test
    public void removeAndRemoveAllAreNoOps() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        delegate.remove("key");
        delegate.removeAll();
    }

    @Test
    public void lockItemReturnsNullWhenRegionCacheReturnsNull() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        assertNull(delegate.lockItem("key", 1));
    }

    @Test
    public void unlockItemWithNullLockDoesNotThrow() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        delegate.unlockItem("key", (SoftLock) null);
    }
}
