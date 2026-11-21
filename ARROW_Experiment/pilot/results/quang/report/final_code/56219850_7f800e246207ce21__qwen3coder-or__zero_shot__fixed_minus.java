package com.ikasoa.core.netty.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.transport.TTransport;
import org.junit.Test;

import com.ikasoa.core.thrift.server.ServerConfiguration;

public class NettyServerConfiguration_ArrowTest {

	@Test
	public void testConstructor() {
		MockProcessorFactory processorFactory = new MockProcessorFactory();
		NettyServerConfiguration config = new NettyServerConfiguration(processorFactory);
		assertNotNull(config);
		assertEquals(processorFactory, config.getProcessorFactory());
	}

	@Test
	public void testWorkerCount() {
		MockProcessorFactory processorFactory = new MockProcessorFactory();
		NettyServerConfiguration config = new NettyServerConfiguration(processorFactory);
		assertEquals(2, config.getWorkerCount());
		config.setWorkerCount(5);
		assertEquals(5, config.getWorkerCount());
	}

	@Test
	public void testMaxFrameSize() {
		MockProcessorFactory processorFactory = new MockProcessorFactory();
		NettyServerConfiguration config = new NettyServerConfiguration(processorFactory);
		assertEquals(64 * 1024 * 1024, config.getMaxFrameSize());
	}

	@Test
	public void testQueuedResponseLimit() {
		MockProcessorFactory processorFactory = new MockProcessorFactory();
		NettyServerConfiguration config = new NettyServerConfiguration(processorFactory);
		assertEquals(16, config.getQueuedResponseLimit());
	}

	private static class MockProcessorFactory implements TProcessorFactory {
		@Override
		public TProcessor getProcessor(TTransport transport) {
			return null;
		}
		
		@Override
		public TProcessorFactory getProcessorFactory(TTransport transport) {
			return this;
		}
	}
}
