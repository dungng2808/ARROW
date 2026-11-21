package com.hazelcast.hibernate.access;

import com.hazelcast.hibernate.region.HazelcastRegion;
import org.hibernate.cache.spi.access.SoftLock;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;

public class ReadWriteAccessDelegate_ArrowTest {

    @Test
    public void insertReturnsFalse() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(null, new Properties());

        assertFalse(delegate.insert("key", "value", "version"));
    }

    @Test
    public void updateReturnsFalse() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(null, new Properties());

        assertFalse(delegate.update("key", "value", "current", "previous"));
    }

    @Test
    public void removeDoesNotThrow() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(null, new Properties());

        delegate.remove("key");
    }

    @Test
    public void removeAllDoesNotThrow() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(null, new Properties());

        delegate.removeAll();
    }
}
