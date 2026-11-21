package io.datakernel.datastream.processor;

import io.datakernel.datastream.StreamConsumer;
import io.datakernel.datastream.StreamSupplier;
import io.datakernel.datastream.StreamDataAcceptor;
import io.datakernel.datastream.StreamEndOfStream;
import io.datakernel.datastream.StreamException;
import io.datakernel.datastream.StreamAcknowledgement;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class StreamBuffer_ArrowTest {

	@Test
	public void testCreate() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 10);
		assertNotNull(buffer);
	}

	@Test
	public void testIsSaturated() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 2);
		assertFalse(buffer.isSaturated());

		// Add items to fill buffer
		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming(null);
		input.accept("item1");
		input.accept("item2");

		assertTrue(buffer.isSaturated());
		assertFalse(buffer.isExhausted());
		assertFalse(buffer.isEmpty());
		assertEquals(2, buffer.size());
	}

	@Test
	public void testIsExhausted() {
		StreamBuffer<String> buffer = StreamBuffer.create(1, 10);
		assertTrue(buffer.isExhausted());

		// Add one item
		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming(null);
		input.accept("item1");

		assertFalse(buffer.isExhausted());
		assertFalse(buffer.isSaturated());
		assertFalse(buffer.isEmpty());
		assertEquals(1, buffer.size());
	}

	@Test
	public void testIsEmpty() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 10);
		assertTrue(buffer.isEmpty());
		assertEquals(0, buffer.size());

		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming(null);
		input.accept("item1");

		assertFalse(buffer.isEmpty());
		assertEquals(1, buffer.size());
	}

	@Test
	public void testSize() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 10);
		assertEquals(0, buffer.size());

		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming(null);
		input.accept("item1");
		input.accept("item2");

		assertEquals(2, buffer.size());
	}

	@Test
	public void testSyncWhenSaturated() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 2);
		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming(null);
		input.accept("item1");
		input.accept("item2");

		assertTrue(buffer.isSaturated());
	}

	@Test
	public void testSyncWhenNotSaturatedAndNotExhausted() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 2);
		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming(null);
		input.accept("item1");

		assertFalse(buffer.isSaturated());
		assertFalse(buffer.isExhausted());
		assertFalse(buffer.isEmpty());
		assertEquals(1, buffer.size());
	}

	@Test
	public void testSyncWhenExhausted() {
		StreamBuffer<String> buffer = StreamBuffer.create(1, 10);
		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming(null);
		input.accept("item1");

		assertFalse(buffer.isSaturated());
		assertFalse(buffer.isExhausted());
		assertFalse(buffer.isEmpty());
		assertEquals(1, buffer.size());
	}

	@Test
	public void testFlush() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 2);
		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming(null);
		input.accept("item1");
		input.accept("item2");

		AtomicInteger count = new AtomicInteger(0);
		AtomicBoolean endOfStream = new AtomicBoolean(false);
		StreamSupplier<String> output = buffer.getOutput();
		output.streamTo(new StreamDataAcceptor<String>() {
			@Override
			public void accept(String item) {
				count.incrementAndGet();
			}
		});

		output.startStreaming(new StreamEndOfStream() {
			@Override
			public void endOfStream() {
				endOfStream.set(true);
			}
		});
		
		assertEquals(2, count.get());
		assertTrue(endOfStream.get());
		assertTrue(buffer.isEmpty());
		assertEquals(0, buffer.size());
	}

	@Test
	public void testInputOnStarted() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 10);
		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming(null);
		assertTrue(buffer.isEmpty());
		assertEquals(0, buffer.size());
	}

	@Test
	public void testInputOnEndOfStream() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 10);
		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming(null);
		input.endOfStream();

		StreamSupplier<String> output = buffer.getOutput();
		AtomicBoolean endOfStream = new AtomicBoolean(false);
		output.startStreaming(new StreamEndOfStream() {
			@Override
			public void endOfStream() {
				endOfStream.set(true);
			}
		});
		
		assertTrue(endOfStream.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidBufferSize() {
		StreamBuffer.create(-1, 10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidMaxBufferSize() {
		StreamBuffer.create(0, 0);
	}
}
