package com.hazelcast.hibernate.access;

import com.hazelcast.hibernate.region.HazelcastRegion;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ReadWriteAccessDelegate_ArrowTest {

    @Test
    public void afterInsertReturnsFalseWhenCacheIsUnavailable() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        assertFalse(delegate.afterInsert("key", "value", 1));
    }

    @Test
    public void insertIsNoOp() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        assertFalse(delegate.insert("key", "value", 1));
    }

    @Test
    public void updateIsNoOp() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        assertFalse(delegate.update("key", "value", 2, 1));
    }

    @Test
    public void lockItemReturnsNullWhenCacheReturnsNull() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        assertNull(delegate.lockItem("key", 1));
    }

    @Test
    public void unlockItemAcceptsNullLock() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        delegate.unlockItem("key", null);
    }

    @Test
    public void removeIsNoOp() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        delegate.remove("key");
    }

    @Test
    public void removeAllIsNoOp() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        delegate.removeAll();
    }

    private ReadWriteAccessDelegate<HazelcastRegion> newDelegate() {
        return new ReadWriteAccessDelegate<HazelcastRegion>(mock(HazelcastRegion.class), new Properties());
    }
}
