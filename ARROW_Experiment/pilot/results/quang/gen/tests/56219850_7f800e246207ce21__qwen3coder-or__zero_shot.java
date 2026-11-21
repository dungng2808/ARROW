package com.ikasoa.core.netty.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.thrift.TProcessorFactory;
import org.junit.Test;

import com.ikasoa.core.thrift.server.ServerConfiguration;

public class NettyServerConfiguration_ArrowTest {

	@Test
	public void testConstructor() {
		TProcessorFactory processorFactory = new MockProcessorFactory();
		NettyServerConfiguration config = new NettyServerConfiguration(processorFactory);
		assertNotNull(config);
		assertEquals(processorFactory, config.getProcessorFactory());
	}

	@Test
	public void testWorkerCount() {
		TProcessorFactory processorFactory = new MockProcessorFactory();
		NettyServerConfiguration config = new NettyServerConfiguration(processorFactory);
		assertEquals(2, config.getWorkerCount());
		config.setWorkerCount(5);
		assertEquals(5, config.getWorkerCount());
	}

	@Test
	public void testMaxFrameSize() {
		TProcessorFactory processorFactory = new MockProcessorFactory();
		NettyServerConfiguration config = new NettyServerConfiguration(processorFactory);
		assertEquals(64 * 1024 * 1024, config.getMaxFrameSize());
	}

	@Test
	public void testQueuedResponseLimit() {
		TProcessorFactory processorFactory = new MockProcessorFactory();
		NettyServerConfiguration config = new NettyServerConfiguration(processorFactory);
		assertEquals(16, config.getQueuedResponseLimit());
	}

	private static class MockProcessorFactory implements TProcessorFactory {
		@Override
		public org.apache.thrift.TProcessor getProcessor(org.apache.thrift.transport.TTransport transport) {
			return null;
		}
	}
}
