package io.datakernel.datastream.processor;

import io.datakernel.datastream.StreamConsumer;
import io.datakernel.datastream.StreamSupplier;
import io.datakernel.datastream.StreamDataAcceptor;
import io.datakernel.datastream.StreamEndOfStream;
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
		StreamBuffer<String> buffer = StreamBuffer.create(0, 3);
		assertFalse(buffer.isSaturated());

		// Add items to fill buffer
		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming();
		input.accept("item1");
		input.accept("item2");
		input.accept("item3");

		assertTrue(buffer.isSaturated());
		assertFalse(buffer.isExhausted());
		assertFalse(buffer.isEmpty());
		assertEquals(3, buffer.size());
	}

	@Test
	public void testIsExhausted() {
		StreamBuffer<String> buffer = StreamBuffer.create(2, 10);
		assertTrue(buffer.isExhausted());

		// Add items to fill buffer
		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming();
		input.accept("item1");
		input.accept("item2");

		assertFalse(buffer.isExhausted());
		assertFalse(buffer.isSaturated());
		assertFalse(buffer.isEmpty());
		assertEquals(2, buffer.size());
	}

	@Test
	public void testIsEmpty() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 10);
		assertTrue(buffer.isEmpty());
		assertEquals(0, buffer.size());

		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming();
		input.accept("item1");

		assertFalse(buffer.isEmpty());
		assertEquals(1, buffer.size());
	}

	@Test
	public void testSize() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 10);
		assertEquals(0, buffer.size());

		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming();
		input.accept("item1");
		input.accept("item2");

		assertEquals(2, buffer.size());
	}

	@Test
	public void testBufferingAndFlushing() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 3);
		AtomicBoolean flushed = new AtomicBoolean(false);
		AtomicInteger count = new AtomicInteger(0);

		StreamSupplier<String> output = buffer.getOutput();
		output.streamTo(new StreamDataAcceptor<String>() {
			@Override
			public void accept(String item) {
				count.incrementAndGet();
				if (count.get() == 3) {
					flushed.set(true);
				}
			}
		});

		StreamConsumer<String> input = buffer.getInput();
		input.startStreaming();

		// Add items to trigger flush
		input.accept("item1");
		input.accept("item2");
		input.accept("item3");

		assertTrue(flushed.get());
		assertEquals(3, count.get());
	}

	@Test
	public void testSyncBehavior() {
		StreamBuffer<String> buffer = StreamBuffer.create(1, 3);
		StreamConsumer<String> input = buffer.getInput();
		StreamSupplier<String> output = buffer.getOutput();

		// Initially should be ready to resume
		assertTrue(buffer.isExhausted());
		assertFalse(buffer.isSaturated());

		input.startStreaming();
		input.accept("item1");
		input.accept("item2");

		// Should be saturated now
		assertTrue(buffer.isSaturated());
		assertFalse(buffer.isExhausted());

		// Add more items to trigger suspension
		input.accept("item3");

		// Now we should be suspended
		assertFalse(buffer.isExhausted());
		assertTrue(buffer.isSaturated());
		assertEquals(3, buffer.size());
	}
}
