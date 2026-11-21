package com.hazelcast.hibernate.access;

import com.hazelcast.hibernate.region.HazelcastRegion;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class ReadWriteAccessDelegate_ArrowTest {

    @Test
    public void afterInsertReturnsFalseWhenCacheIsUnavailable() {
        HazelcastRegion region = mock(HazelcastRegion.class);
        ReadWriteAccessDelegate<HazelcastRegion> delegate =
                new ReadWriteAccessDelegate<HazelcastRegion>(region, new Properties());

        assertFalse(delegate.insert("key", "value", 1));
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
