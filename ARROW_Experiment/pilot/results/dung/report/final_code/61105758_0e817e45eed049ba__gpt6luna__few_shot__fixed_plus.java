package com.hazelcast.hibernate.access;

import com.hazelcast.hibernate.region.HazelcastRegion;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class ReadWriteAccessDelegate_ArrowTest {

    @Test
    public void insertReturnsFalse() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(mock(HazelcastRegion.class), new Properties());

        assertFalse(delegate.insert("key", "value", 1));
    }

    @Test
    public void updateReturnsFalse() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(mock(HazelcastRegion.class), new Properties());

        assertFalse(delegate.update("key", "value", 2, 1));
    }

    @Test
    public void removeAndRemoveAllAreNoOps() {
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(mock(HazelcastRegion.class), new Properties());

        delegate.remove("key");
        delegate.removeAll();
    }
}
