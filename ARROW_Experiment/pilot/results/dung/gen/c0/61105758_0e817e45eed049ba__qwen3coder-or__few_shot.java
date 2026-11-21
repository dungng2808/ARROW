package com.hazelcast.hibernate.access;

import com.hazelcast.core.HazelcastException;
import com.hazelcast.hibernate.region.HazelcastRegion;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.SoftLock;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class ReadWriteAccessDelegate_ArrowTest {

    private HazelcastRegion mockRegion;
    private Properties props;
    private ReadWriteAccessDelegate<HazelcastRegion> delegate;

    @Before
    public void setUp() {
        mockRegion = mock(HazelcastRegion.class);
        props = new Properties();
        delegate = new ReadWriteAccessDelegate<>(mockRegion, props);
    }

    @Test
    public void testAfterInsertSuccess() throws CacheException {
        Object key = "testKey";
        Object value = "testValue";
        Object version = "testVersion";

        when(mockRegion.insert(key, value, version)).thenReturn(true);

        boolean result = delegate.afterInsert(key, value, version);

        assertFalse("afterInsert should return false when cache.insert returns true", result);
    }

    @Test
    public void testAfterInsertException() throws CacheException {
        Object key = "testKey";
        Object value = "testValue";
        Object version = "testVersion";

        when(mockRegion.insert(key, value, version)).thenThrow(new HazelcastException("Test exception"));

        boolean result = delegate.afterInsert(key, value, version);

        assertFalse("afterInsert should return false when HazelcastException is thrown", result);
    }

    @Test
    public void testAfterUpdateSuccess() throws CacheException {
        Object key = "testKey";
        Object value = "testValue";
        Object currentVersion = "currentVersion";
        Object previousVersion = "previousVersion";
        SoftLock lock = mock(SoftLock.class);

        when(mockRegion.update(key, value, currentVersion, lock)).thenReturn(true);

        boolean result = delegate.afterUpdate(key, value, currentVersion, previousVersion, lock);

        assertFalse("afterUpdate should return false when cache.update returns true", result);
    }

    @Test
    public void testAfterUpdateException() throws CacheException {
        Object key = "testKey";
        Object value = "testValue";
        Object currentVersion = "currentVersion";
        Object previousVersion = "previousVersion";
        SoftLock lock = mock(SoftLock.class);

        when(mockRegion.update(key, value, currentVersion, lock)).thenThrow(new HazelcastException("Test exception"));

        boolean result = delegate.afterUpdate(key, value, currentVersion, previousVersion, lock);

        assertFalse("afterUpdate should return false when HazelcastException is thrown", result);
    }

    @Test
    public void testInsert() throws CacheException {
        Object key = "testKey";
        Object value = "testValue";
        Object version = "testVersion";

        boolean result = delegate.insert(key, value, version);

        assertFalse("insert should always return false", result);
    }

    @Test
    public void testLockItem() throws CacheException {
        Object key = "testKey";
        Object version = "testVersion";
        SoftLock mockLock = mock(SoftLock.class);

        when(mockRegion.tryLock(key, version)).thenReturn(mockLock);

        SoftLock result = delegate.lockItem(key, version);

        assertSame("lockItem should return the lock from cache.tryLock", mockLock, result);
    }

    @Test
    public void testUnlockItem() throws CacheException {
        Object key = "testKey";
        SoftLock lock = mock(SoftLock.class);

        delegate.unlockItem(key, lock);

        verify(mockRegion).unlock(key, lock);
    }

    @Test
    public void testUpdate() throws CacheException {
        Object key = "testKey";
        Object value = "testValue";
        Object currentVersion = "currentVersion";
        Object previousVersion = "previousVersion";

        boolean result = delegate.update(key, value, currentVersion, previousVersion);

        assertFalse("update should always return false", result);
    }

    @Test
    public void testRemove() throws CacheException {
        Object key = "testKey";

        delegate.remove(key);

        // Should not throw any exception
    }

    @Test
    public void testRemoveAll() throws CacheException {
        delegate.removeAll();

        // Should not throw any exception
    }
}
