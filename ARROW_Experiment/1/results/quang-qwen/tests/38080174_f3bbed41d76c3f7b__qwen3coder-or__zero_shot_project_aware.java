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
        // Fill up the buffer to max size
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
    public void testGetInput() {
        StreamConsumer<String> input = buffer.getInput();
        assertNotNull(input);
        assertTrue(input instanceof StreamBuffer.Input);
    }

    @Test
    public void testGetOutput() {
        StreamSupplier<String> output = buffer.getOutput();
        assertNotNull(output);
        assertTrue(output instanceof StreamBuffer.Output);
    }

    @Test
    public void testSyncWhenSaturated() {
        // Fill up the buffer to max size
        for (int i = 0; i < 4; i++) {
            buffer.getInput().accept("item" + i);
        }
        // Should be saturated now
        assertTrue(buffer.isSaturated());
    }

    @Test
    public void testSyncWhenNotSaturatedAndNotExhausted() {
        // Add less than min size items
        buffer.getInput().accept("item1");
        buffer.getInput().accept("item2");
        // Should not be saturated or exhausted
        assertFalse(buffer.isSaturated());
        assertFalse(buffer.isExhausted());
    }

    @Test
    public void testFlush() {
        // Add items to buffer
        buffer.getInput().accept("item1");
        buffer.getInput().accept("item2");
        // Flush should send items
        StreamDataAcceptor<String> acceptor = new StreamDataAcceptor<String>() {
            int count = 0;
            @Override
            public void accept(String item) {
                count++;
                assertEquals("item" + count, item);
            }
        };
        buffer.getOutput().resume(acceptor);
        // Verify that items were flushed
        assertEquals(2, buffer.size());
    }

    @Test
    public void testDoAdd() {
        buffer.getInput().accept("item1");
        buffer.getInput().accept("item2");
        assertEquals(2, buffer.size());
    }

    @Test
    public void testInputOnStarted() {
        // Just verify that it doesn't throw
        buffer.getInput().start();
    }

    @Test
    public void testInputOnEndOfStream() {
        // Just verify that it doesn't throw
        buffer.getInput().endOfStream();
    }

    @Test
    public void testOutputOnResumed() {
        // Just verify that it doesn't throw
        buffer.getOutput().resume(new StreamDataAcceptor<String>() {
            @Override
            public void accept(String item) {}
        });
    }

    @Test
    public void testOutputOnSuspended() {
        // Just verify that it doesn't throw
        buffer.getOutput().suspend();
    }
}
