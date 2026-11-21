package com.ikasoa.core.netty.server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NettyServerConfiguration_ArrowTest {

    @Test
    public void defaultsToExpectedWorkerCount() {
        NettyServerConfiguration configuration = new NettyServerConfiguration(null);

        assertEquals(2, configuration.getWorkerCount());
    }

    @Test
    public void exposesExpectedMaximumFrameSize() {
        NettyServerConfiguration configuration = new NettyServerConfiguration(null);

        assertEquals(64 * 1024 * 1024, configuration.getMaxFrameSize());
    }

    @Test
    public void exposesExpectedQueuedResponseLimit() {
        NettyServerConfiguration configuration = new NettyServerConfiguration(null);

        assertEquals(16, configuration.getQueuedResponseLimit());
    }

    @Test
    public void workerCountCanBeChanged() {
        NettyServerConfiguration configuration = new NettyServerConfiguration(null);

        configuration.setWorkerCount(5);

        assertEquals(5, configuration.getWorkerCount());
    }

    @Test
    public void acceptsNullProcessorFactory() {
        NettyServerConfiguration configuration = new NettyServerConfiguration(null);

        assertEquals(2, configuration.getWorkerCount());
    }
}
