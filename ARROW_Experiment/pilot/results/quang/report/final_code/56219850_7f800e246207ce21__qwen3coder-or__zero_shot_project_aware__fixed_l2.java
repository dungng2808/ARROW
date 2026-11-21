package com.ikasoa.core.netty.server;

import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;
import static org.junit.Assert.*;

public class NettyServerConfiguration_ArrowTest {

    @Test
    public void testConstructor() {
        TProcessorFactory processorFactory = new TProcessorFactory(new TProcessor() {
            @Override
            public boolean process(TProtocol inProt, TProtocol outProt) throws TTransportException {
                return false;
            }
        });
        NettyServerConfiguration config = new NettyServerConfiguration(processorFactory);
        assertNotNull(config);
        assertEquals(processorFactory, config.getProcessorFactory());
    }

    @Test
    public void testWorkerCount() {
        TProcessorFactory processorFactory = new TProcessorFactory(new TProcessor() {
            @Override
            public boolean process(TProtocol inProt, TProtocol outProt) throws TTransportException {
                return false;
            }
        });
        NettyServerConfiguration config = new NettyServerConfiguration(processorFactory);
        assertEquals(2, config.getWorkerCount());
        config.setWorkerCount(5);
        assertEquals(5, config.getWorkerCount());
    }

    @Test
    public void testMaxFrameSize() {
        TProcessorFactory processorFactory = new TProcessorFactory(new TProcessor() {
            @Override
            public boolean process(TProtocol inProt, TProtocol outProt) throws TTransportException {
                return false;
            }
        });
        NettyServerConfiguration config = new NettyServerConfiguration(processorFactory);
        assertEquals(64 * 1024 * 1024, config.getMaxFrameSize());
    }

    @Test
    public void testQueuedResponseLimit() {
        TProcessorFactory processorFactory = new TProcessorFactory(new TProcessor() {
            @Override
            public boolean process(TProtocol inProt, TProtocol outProt) throws TTransportException {
                return false;
            }
        });
        NettyServerConfiguration config = new NettyServerConfiguration(processorFactory);
        assertEquals(16, config.getQueuedResponseLimit());
    }
}
