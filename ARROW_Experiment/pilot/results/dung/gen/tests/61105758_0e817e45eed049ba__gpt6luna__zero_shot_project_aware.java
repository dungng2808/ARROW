package com.hazelcast.hibernate.access;

import com.hazelcast.core.HazelcastException;
import com.hazelcast.hibernate.region.HazelcastRegion;
import org.hibernate.cache.spi.access.SoftLock;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReadWriteAccessDelegate_ArrowTest {

    @Test
    public void afterInsertReturnsCacheResult() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        assertFalse(delegate.afterInsert("key", "value", 1));
    }

    @Test
    public void afterInsertReturnsFalseWhenCacheThrowsHazelcastException() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        assertFalse(delegate.afterInsert("key", "value", 1));
    }

    @Test
    public void insertIsNoOp() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        assertFalse(delegate.insert("key", "value", 1));
    }

    @Test
    public void updateIsNoOp() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        assertFalse(delegate.update("key", "value", 2, 1));
    }

    @Test
    public void lockItemReturnsNullWhenCacheReturnsNull() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        assertNull(delegate.lockItem("key", 1));
    }

    @Test
    public void unlockItemAcceptsNullLock() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        delegate.unlockItem("key", null);
    }

    @Test
    public void removeIsNoOp() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        delegate.remove("key");
    }

    @Test
    public void removeAllIsNoOp() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        delegate.removeAll();
    }
}
