package io.datakernel.datastream.processor;

import io.datakernel.datastream.StreamConsumer;
import io.datakernel.datastream.StreamSupplier;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StreamBuffer_ArrowTest {
	@Test
	public void createWithZeroMinimumStartsEmptyAndExhausted() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 2);

		assertTrue(buffer.isEmpty());
		assertEquals(0, buffer.size());
		assertTrue(buffer.isExhausted());
		assertFalse(buffer.isSaturated());
	}

	@Test
	public void createWithPositiveMinimumStartsExhausted() {
		StreamBuffer<String> buffer = StreamBuffer.create(2, 4);

		assertTrue(buffer.isEmpty());
		assertEquals(0, buffer.size());
		assertTrue(buffer.isExhausted());
		assertFalse(buffer.isSaturated());
	}

	@Test
	public void exposesInputAndOutput() {
		StreamBuffer<String> buffer = StreamBuffer.create(0, 1);

		StreamConsumer<String> input = buffer.getInput();
		StreamSupplier<String> output = buffer.getOutput();

		assertNotNull(input);
		assertNotNull(output);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsZeroMaximumSize() {
		StreamBuffer.create(0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNegativeMinimumSize() {
		StreamBuffer.create(-1, 2);
	}
}
