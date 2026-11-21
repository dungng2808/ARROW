package com.ikasoa.core.netty.server;

import org.apache.thrift.TProcessorFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NettyServerConfiguration_ArrowTest {

    @Test
    public void testConstructor() {
        TProcessorFactory processorFactory = new TProcessorFactory(null);
        NettyServerConfiguration configuration = new NettyServerConfiguration(processorFactory);
        assertNotNull(configuration);
        assertEquals(processorFactory, configuration.getProcessorFactory());
    }

    @Test
    public void testWorkerCount() {
        TProcessorFactory processorFactory = new TProcessorFactory(null);
        NettyServerConfiguration configuration = new NettyServerConfiguration(processorFactory);
        assertEquals(2, configuration.getWorkerCount());
        
        configuration.setWorkerCount(5);
        assertEquals(5, configuration.getWorkerCount());
    }

    @Test
    public void testMaxFrameSize() {
        TProcessorFactory processorFactory = new TProcessorFactory(null);
        NettyServerConfiguration configuration = new NettyServerConfiguration(processorFactory);
        assertEquals(64 * 1024 * 1024, configuration.getMaxFrameSize());
    }

    @Test
    public void testQueuedResponseLimit() {
        TProcessorFactory processorFactory = new TProcessorFactory(null);
        NettyServerConfiguration configuration = new NettyServerConfiguration(processorFactory);
        assertEquals(16, configuration.getQueuedResponseLimit());
    }
}
