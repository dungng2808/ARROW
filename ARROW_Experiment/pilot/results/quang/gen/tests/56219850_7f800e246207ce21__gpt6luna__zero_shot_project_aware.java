package com.ikasoa.core.netty.server;

import org.apache.thrift.TProcessorFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NettyServerConfiguration_ArrowTest {

    @Test
    public void defaultValuesAreConfigured() {
        NettyServerConfiguration configuration = new NettyServerConfiguration(null);

        assertEquals(2, configuration.getWorkerCount());
        assertEquals(64 * 1024 * 1024, configuration.getMaxFrameSize());
        assertEquals(16, configuration.getQueuedResponseLimit());
    }

    @Test
    public void workerCountCanBeChanged() {
        NettyServerConfiguration configuration = new NettyServerConfiguration(null);

        configuration.setWorkerCount(5);

        assertEquals(5, configuration.getWorkerCount());
    }
}
