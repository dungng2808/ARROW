package org.netcrusher.core.filter;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class LoggingFilter_ArrowTest {

    @Test
    public void transformAcceptsArrayBackedBuffer() {
        LoggingFilter filter = new LoggingFilter(
                new InetSocketAddress("localhost", 1234),
                "logging-filter-array-test",
                LoggingFilter.Level.INFO);

        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0x00, 0x0f, (byte) 0xff });
        filter.transform(buffer);

        assertEquals(0, buffer.position());
    }

    @Test
    public void transformAcceptsDirectBuffer() {
        LoggingFilter filter = new LoggingFilter(
                new InetSocketAddress("localhost", 1234),
                "logging-filter-direct-test",
                LoggingFilter.Level.INFO);

        ByteBuffer buffer = ByteBuffer.allocateDirect(2);
        buffer.put((byte) 0x12);
        buffer.put((byte) 0x34);
        buffer.flip();

        filter.transform(buffer);

        assertEquals(0, buffer.position());
    }

    @Test
    public void transformAcceptsEmptyBuffer() {
        LoggingFilter filter = new LoggingFilter(
                new InetSocketAddress("localhost", 1234),
                "logging-filter-empty-test",
                LoggingFilter.Level.INFO);

        ByteBuffer buffer = ByteBuffer.allocate(0);
        filter.transform(buffer);

        assertEquals(0, buffer.remaining());
    }
}
