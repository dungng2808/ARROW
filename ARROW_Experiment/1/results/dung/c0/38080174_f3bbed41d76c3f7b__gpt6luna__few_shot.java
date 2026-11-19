package io.datakernel.datastream.processor;

import io.datakernel.datastream.StreamConsumer;
import io.datakernel.datastream.StreamSupplier;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StreamBuffer_ArrowTest {

    @Test
    public void createStartsEmptyAndExhausted() {
        StreamBuffer<Integer> buffer = StreamBuffer.create(1, 3);

        assertTrue(buffer.isEmpty());
        assertEquals(0, buffer.size());
        assertFalse(buffer.isSaturated());
        assertTrue(buffer.isExhausted());
    }

    @Test
    public void createWithZeroMinimumIsExhaustedWhenEmpty() {
        StreamBuffer<String> buffer = StreamBuffer.create(0, 2);

        assertTrue(buffer.isEmpty());
        assertTrue(buffer.isExhausted());
        assertFalse(buffer.isSaturated());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsZeroMaximumSize() {
        StreamBuffer.create(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeMinimumSize() {
        StreamBuffer.create(-1, 2);
    }

    @Test
    public void exposesInputAndOutput() {
        StreamBuffer<Integer> buffer = StreamBuffer.create(0, 2);

        StreamConsumer<Integer> input = buffer.getInput();
        StreamSupplier<Integer> output = buffer.getOutput();

        assertTrue(input != null);
        assertTrue(output != null);
    }
}
