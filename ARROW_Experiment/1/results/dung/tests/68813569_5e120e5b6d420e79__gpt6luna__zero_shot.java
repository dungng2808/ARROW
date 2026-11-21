package org.netcrusher.core.filter;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class LoggingFilter_ArrowTest {

    @Test
    public void transformDoesNotChangeArrayBackedBufferPosition() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0x01, (byte) 0xAB, 0x00 });
        buffer.position(1);
        buffer.limit(2);

        LoggingFilter filter = new LoggingFilter(
                new InetSocketAddress("localhost", 1234),
                "org.netcrusher.core.filter.LoggingFilter_ArrowTest",
                LoggingFilter.Level.INFO);

        filter.transform(buffer);

        assertEquals(1, buffer.position());
        assertEquals(2, buffer.limit());
    }

    @Test
    public void transformDoesNotChangeDirectBufferPosition() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(2);
        buffer.put((byte) 0x12);
        buffer.put((byte) 0xFE);
        buffer.flip();
        buffer.position(1);

        LoggingFilter filter = new LoggingFilter(
                new InetSocketAddress("localhost", 1234),
                "org.netcrusher.core.filter.LoggingFilter_ArrowTest",
                LoggingFilter.Level.INFO);

        filter.transform(buffer);

        assertEquals(1, buffer.position());
        assertEquals(2, buffer.limit());
    }

    @Test
    public void transformAcceptsEmptyBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(0);
        LoggingFilter filter = new LoggingFilter(
                new InetSocketAddress("localhost", 1234),
                "org.netcrusher.core.filter.LoggingFilter_ArrowTest",
                LoggingFilter.Level.INFO);

        filter.transform(buffer);

        assertEquals(0, buffer.position());
        assertEquals(0, buffer.limit());
    }
}
