package io.datakernel.datastream.processor;

import io.datakernel.datastream.*;
import io.datakernel.datastream.processor.StreamBuffer;
import org.junit.Test;

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

		// Add 3 items to fill the buffer
		buffer.getInput().getAcknowledgement().whenResult(() -> {});
		buffer.getInput().accept("item1");
		buffer.getInput().accept("item2");
		buffer.getInput().accept("item3");

		assertTrue(buffer.isSaturated());
	}

	@Test
	public void testIsExhausted() {
		StreamBuffer<String> buffer = StreamBuffer.create(2, 10);
		assertTrue(buffer.isExhausted());

		// Add 1 item
		buffer.getInput().getAcknowledgement().whenResult(() -> {});
		buffer.getInput().accept("item1");

		assertFalse(buffer.isExhausted());
	}

	@Test
	public void testIsEmpty() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 10);
		assertTrue(buffer.isEmpty());

		buffer.getInput().getAcknowledgement().whenResult(() -> {});
		buffer.getInput().accept("item1");

		assertFalse(buffer.isEmpty());
	}

	@Test
	public void testSize() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 10);
		assertEquals(0, buffer.size());

		buffer.getInput().getAcknowledgement().whenResult(() -> {});
		buffer.getInput().accept("item1");
		assertEquals(1, buffer.size());

		buffer.getInput().accept("item2");
		assertEquals(2, buffer.size());
	}

	@Test
	public void testSyncWhenSaturated() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 2);
		buffer.getInput().getAcknowledgement().whenResult(() -> {});

		// Fill up the buffer
		buffer.getInput().accept("item1");
		buffer.getInput().accept("item2");

		assertTrue(buffer.isSaturated());
	}

	@Test
	public void testSyncWhenNotSaturatedAndNotExhausted() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 10);
		buffer.getInput().getAcknowledgement().whenResult(() -> {});

		// Add one item
		buffer.getInput().accept("item1");

		assertFalse(buffer.isSaturated());
		assertFalse(buffer.isExhausted());
	}

	@Test
	public void testFlush() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 10);
		buffer.getInput().getAcknowledgement().whenResult(() -> {});

		// Add items to buffer
		buffer.getInput().accept("item1");
		buffer.getInput().accept("item2");

		// Set up output
		final StringBuilder result = new StringBuilder();
		buffer.getOutput().setAcknowledgeCallback(() -> {});
		buffer.getOutput().setDataAcceptor(item -> result.append(item));

		// Flush the buffer
		buffer.getOutput().resume();

		assertEquals("item1item2", result.toString());
	}

	@Test
	public void testOnEndOfStream() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 10);
		buffer.getInput().getAcknowledgement().whenResult(() -> {});

		// Add items to buffer
		buffer.getInput().accept("item1");
		buffer.getInput().accept("item2");

		// End of stream
		buffer.getInput().endOfStream();

		// Set up output
		final StringBuilder result = new StringBuilder();
		buffer.getOutput().setAcknowledgeCallback(() -> {});
		buffer.getOutput().setDataAcceptor(item -> result.append(item));

		// Resume to flush
		buffer.getOutput().resume();

		assertEquals("item1item2", result.toString());
	}
}
