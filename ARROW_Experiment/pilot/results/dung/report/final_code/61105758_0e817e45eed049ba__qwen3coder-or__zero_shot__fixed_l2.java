package com.hazelcast.hibernate.access;

import com.hazelcast.core.HazelcastException;
import com.hazelcast.hibernate.region.HazelcastRegion;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.SoftLock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReadWriteAccessDelegate_ArrowTest {

    @Mock
    private HazelcastRegion mockRegion;

    @Mock
    private Properties mockProps;

    @Mock
    private SoftLock mockLock;

    private ReadWriteAccessDelegate<HazelcastRegion> delegate;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        delegate = new ReadWriteAccessDelegate<HazelcastRegion>(mockRegion, mockProps);
    }

    @Test
    public void testAfterInsert_Success() throws CacheException {
        Object key = "testKey";
        Object value = "testValue";
        Object version = "testVersion";

        when(mockRegion.getCache()).thenReturn(mockRegion);
        doReturn(true).when(mockRegion).insert(key, value, version);

        boolean result = delegate.afterInsert(key, value, version);

        assertTrue(result);
    }

    @Test
    public void testAfterInsert_HazelcastException() throws CacheException {
        Object key = "testKey";
        Object value = "testValue";
        Object version = "testVersion";

        when(mockRegion.getCache()).thenReturn(mockRegion);
        doThrow(new HazelcastException("Test exception")).when(mockRegion).insert(key, value, version);

        boolean result = delegate.afterInsert(key, value, version);

        assertFalse(result);
    }

    @Test
    public void testAfterUpdate_Success() throws CacheException {
        Object key = "testKey";
        Object value = "testValue";
        Object currentVersion = "currentVersion";
        Object previousVersion = "previousVersion";

        when(mockRegion.getCache()).thenReturn(mockRegion);
        doReturn(true).when(mockRegion).update(key, value, currentVersion, mockLock);

        boolean result = delegate.afterUpdate(key, value, currentVersion, previousVersion, mockLock);

        assertTrue(result);
    }

    @Test
    public void testAfterUpdate_HazelcastException() throws CacheException {
        Object key = "testKey";
        Object value = "testValue";
        Object currentVersion = "currentVersion";
        Object previousVersion = "previousVersion";

        when(mockRegion.getCache()).thenReturn(mockRegion);
        doThrow(new HazelcastException("Test exception")).when(mockRegion).update(key, value, currentVersion, mockLock);

        boolean result = delegate.afterUpdate(key, value, currentVersion, previousVersion, mockLock);

        assertFalse(result);
    }

    @Test
    public void testInsert() throws CacheException {
        Object key = "testKey";
        Object value = "testValue";
        Object version = "testVersion";

        boolean result = delegate.insert(key, value, version);

        assertFalse(result);
    }

    @Test
    public void testLockItem() throws CacheException {
        Object key = "testKey";
        Object version = "testVersion";

        when(mockRegion.getCache()).thenReturn(mockRegion);
        doReturn(mockLock).when(mockRegion).tryLock(key, version);

        SoftLock result = delegate.lockItem(key, version);

        assertSame(mockLock, result);
    }

    @Test
    public void testUnlockItem() throws CacheException {
        Object key = "testKey";

        when(mockRegion.getCache()).thenReturn(mockRegion);
        delegate.unlockItem(key, mockLock);

        verify(mockRegion).unlock(key, mockLock);
    }

    @Test
    public void testUpdate() throws CacheException {
        Object key = "testKey";
        Object value = "testValue";
        Object currentVersion = "currentVersion";
        Object previousVersion = "previousVersion";

        boolean result = delegate.update(key, value, currentVersion, previousVersion);

        assertFalse(result);
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
