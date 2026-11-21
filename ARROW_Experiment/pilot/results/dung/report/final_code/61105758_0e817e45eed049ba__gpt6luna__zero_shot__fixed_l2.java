package com.hazelcast.hibernate.access;

import com.hazelcast.hibernate.region.HazelcastRegion;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class ReadWriteAccessDelegate_ArrowTest {

    private ReadWriteAccessDelegate<HazelcastRegion> newDelegate() {
        return new ReadWriteAccessDelegate<HazelcastRegion>(mock(HazelcastRegion.class), new Properties());
    }

    @Test
    public void insertReturnsFalse() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        assertFalse(delegate.insert("key", "value", "version"));
    }

    @Test
    public void updateReturnsFalse() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        assertFalse(delegate.update("key", "value", "current", "previous"));
    }

    @Test
    public void removeDoesNotThrow() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        delegate.remove("key");
    }

    @Test
    public void removeAllDoesNotThrow() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate = newDelegate();

        delegate.removeAll();
    }
}
