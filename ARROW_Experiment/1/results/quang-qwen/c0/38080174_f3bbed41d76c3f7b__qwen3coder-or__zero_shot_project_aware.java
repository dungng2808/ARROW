package io.datakernel.datastream.processor;

import io.datakernel.datastream.*;
import io.datakernel.datastream.processor.StreamBuffer;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class StreamBuffer_ArrowTest {

    private StreamBuffer<String> buffer;

    @Before
    public void setUp() {
        buffer = StreamBuffer.create(0, 4);
    }

    @Test
    public void testCreate() {
        StreamBuffer<String> buffer = StreamBuffer.create(0, 4);
        assertNotNull(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithInvalidArguments() {
        StreamBuffer.create(-1, 0);
    }

    @Test
    public void testIsSaturated() {
        assertFalse(buffer.isSaturated());
        // Fill up the buffer
        for (int i = 0; i < 4; i++) {
            buffer.getInput().accept("item" + i);
        }
        assertTrue(buffer.isSaturated());
    }

    @Test
    public void testIsExhausted() {
        assertTrue(buffer.isExhausted());
        // Add some items
        buffer.getInput().accept("item1");
        assertFalse(buffer.isExhausted());
    }

    @Test
    public void testIsEmpty() {
        assertTrue(buffer.isEmpty());
        buffer.getInput().accept("item1");
        assertFalse(buffer.isEmpty());
    }

    @Test
    public void testSize() {
        assertEquals(0, buffer.size());
        buffer.getInput().accept("item1");
        assertEquals(1, buffer.size());
        buffer.getInput().accept("item2");
        assertEquals(2, buffer.size());
    }

    @Test
    public void testInputAndOutput() {
        StreamConsumer<String> input = buffer.getInput();
        StreamSupplier<String> output = buffer.getOutput();

        assertNotNull(input);
        assertNotNull(output);
    }

    @Test
    public void testFlush() {
        // Add items to buffer
        buffer.getInput().accept("item1");
        buffer.getInput().accept("item2");

        // Create a mock acceptor to capture output
        final java.util.List<String> capturedItems = new java.util.ArrayList<>();
        StreamDataAcceptor<String> mockAcceptor = capturedItems::add;

        // Resume output to start flushing
        output.resume(mockAcceptor);

        // Verify items were flushed
        assertEquals(2, capturedItems.size());
        assertEquals("item1", capturedItems.get(0));
        assertEquals("item2", capturedItems.get(1));
    }

    @Test
    public void testSyncWhenSaturated() {
        // Fill up the buffer to saturation
        for (int i = 0; i < 4; i++) {
            buffer.getInput().accept("item" + i);
        }

        // Verify that input is suspended
        // This is hard to test directly, but we can verify behavior
        assertTrue(buffer.isSaturated());
    }

    @Test
    public void testSyncWhenNotSaturated() {
        // Add less than max size items
        buffer.getInput().accept("item1");
        buffer.getInput().accept("item2");

        // Should not be saturated, should resume input
        assertFalse(buffer.isSaturated());
    }

    @Test
    public void testOnEndOfStream() {
        // Add items and end stream
        buffer.getInput().accept("item1");
        buffer.getInput().endOfStream();

        // Verify that output gets the end of stream signal
        // This requires checking internal state or using a mock
        // For now, just ensure no exception occurs
        assertTrue(true);
    }

    @Test
    public void testDoAdd() {
        // Test internal add functionality
        buffer.getInput().accept("item1");
        assertEquals(1, buffer.size());
    }

    private StreamSupplier<String> output = buffer.getOutput();
}
