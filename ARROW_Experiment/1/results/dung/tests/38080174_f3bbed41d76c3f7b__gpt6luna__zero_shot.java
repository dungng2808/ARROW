package io.datakernel.datastream.processor;

import io.datakernel.datastream.StreamConsumer;
import io.datakernel.datastream.StreamSupplier;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StreamBuffer_ArrowTest {
	@Test
	public void newBufferIsEmptyAndExhausted() {
		StreamBuffer<String> buffer = StreamBuffer.create(1, 3);

		assertTrue(buffer.isEmpty());
		assertTrue(buffer.isExhausted());
		assertFalse(buffer.isSaturated());
		assertEquals(0, buffer.size());
	}

	@Test
	public void zeroMinimumMakesEmptyBufferNotExhausted() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 2);

		assertTrue(buffer.isEmpty());
		assertFalse(buffer.isExhausted());
		assertFalse(buffer.isSaturated());
		assertEquals(0, buffer.size());
	}

	@Test
	public void exposesInputAndOutput() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 2);

		StreamConsumer<String> input = buffer.getInput();
		StreamSupplier<String> output = buffer.getOutput();

		assertTrue(input != null);
		assertTrue(output != null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsZeroMaximum() {
		StreamBuffer.create(0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNegativeMinimum() {
		StreamBuffer.create(-1, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsMinimumGreaterThanMaximum() {
		StreamBuffer.create(3, 2);
	}
}
