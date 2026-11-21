package com.hazelcast.hibernate.access;

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
    private SoftLock mockSoftLock;

    private ReadWriteAccessDelegate<HazelcastRegion> delegate;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        delegate = new ReadWriteAccessDelegate<HazelcastRegion>(mockRegion, mockProps);
    }

    @Test
    public void testAfterInsert_Success() throws CacheException {
        Object key = "key";
        Object value = "value";
        Object version = "version";

        AccessDelegate mockCache = mock(AccessDelegate.class);
        when(mockRegion.getCache()).thenReturn(mockCache);
        doReturn(true).when(mockCache).insert(key, value, version);

        boolean result = delegate.afterInsert(key, value, version);

        assertTrue(result);
    }

    @Test
    public void testAfterInsert_HazelcastException() throws CacheException {
        Object key = "key";
        Object value = "value";
        Object version = "version";

        AccessDelegate mockCache = mock(AccessDelegate.class);
        when(mockRegion.getCache()).thenReturn(mockCache);
        doThrow(new com.hazelcast.core.HazelcastException("test")).when(mockCache).insert(key, value, version);

        boolean result = delegate.afterInsert(key, value, version);

        assertFalse(result);
    }

    @Test
    public void testAfterUpdate_Success() throws CacheException {
        Object key = "key";
        Object value = "value";
        Object currentVersion = "currentVersion";
        Object previousVersion = "previousVersion";

        AccessDelegate mockCache = mock(AccessDelegate.class);
        when(mockRegion.getCache()).thenReturn(mockCache);
        doReturn(true).when(mockCache).update(key, value, currentVersion, mockSoftLock);

        boolean result = delegate.afterUpdate(key, value, currentVersion, previousVersion, mockSoftLock);

        assertTrue(result);
    }

    @Test
    public void testAfterUpdate_HazelcastException() throws CacheException {
        Object key = "key";
        Object value = "value";
        Object currentVersion = "currentVersion";
        Object previousVersion = "previousVersion";

        AccessDelegate mockCache = mock(AccessDelegate.class);
        when(mockRegion.getCache()).thenReturn(mockCache);
        doThrow(new com.hazelcast.core.HazelcastException("test")).when(mockCache).update(key, value, currentVersion, mockSoftLock);

        boolean result = delegate.afterUpdate(key, value, currentVersion, previousVersion, mockSoftLock);

        assertFalse(result);
    }

    @Test
    public void testInsert() throws CacheException {
        Object key = "key";
        Object value = "value";
        Object version = "version";

        boolean result = delegate.insert(key, value, version);

        assertFalse(result);
    }

    @Test
    public void testLockItem() throws CacheException {
        Object key = "key";
        Object version = "version";

        AccessDelegate mockCache = mock(AccessDelegate.class);
        when(mockRegion.getCache()).thenReturn(mockCache);
        doReturn(mockSoftLock).when(mockCache).tryLock(key, version);

        SoftLock result = delegate.lockItem(key, version);

        assertSame(mockSoftLock, result);
    }

    @Test
    public void testUnlockItem() throws CacheException {
        Object key = "key";

        AccessDelegate mockCache = mock(AccessDelegate.class);
        when(mockRegion.getCache()).thenReturn(mockCache);
        doNothing().when(mockCache).unlock(key, mockSoftLock);

        delegate.unlockItem(key, mockSoftLock);

        verify(mockCache).unlock(key, mockSoftLock);
    }

    @Test
    public void testUpdate() throws CacheException {
        Object key = "key";
        Object value = "value";
        Object currentVersion = "currentVersion";
        Object previousVersion = "previousVersion";

        boolean result = delegate.update(key, value, currentVersion, previousVersion);

        assertFalse(result);
    }

    @Test
    public void testRemove() throws CacheException {
        Object key = "key";

        delegate.remove(key);

        // No exception should be thrown
    }

    @Test
    public void testRemoveAll() throws CacheException {
        delegate.removeAll();

        // No exception should be thrown
    }
}
