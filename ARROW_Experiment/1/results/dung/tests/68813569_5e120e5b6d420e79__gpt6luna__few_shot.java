package org.netcrusher.core.filter;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class LoggingFilter_ArrowTest {

    @Test
    public void transformAcceptsEmptyBuffer() {
        LoggingFilter filter = new LoggingFilter(
                new InetSocketAddress("localhost", 1234),
                "org.netcrusher.core.filter.LoggingFilter_ArrowTest",
                LoggingFilter.Level.INFO);

        filter.transform(ByteBuffer.allocate(0));
        assertEquals(0, ByteBuffer.allocate(0).remaining());
    }

    @Test
    public void transformAcceptsArrayBackedBuffer() {
        LoggingFilter filter = new LoggingFilter(
                new InetSocketAddress("localhost", 1234),
                "org.netcrusher.core.filter.LoggingFilter_ArrowTest",
                LoggingFilter.Level.INFO);

        filter.transform(ByteBuffer.wrap(new byte[] {0x00, 0x0f, (byte) 0xff}));
        assertEquals(3, ByteBuffer.wrap(new byte[] {0x00, 0x0f, (byte) 0xff}).remaining());
    }

    @Test
    public void transformAcceptsDirectBuffer() {
        LoggingFilter filter = new LoggingFilter(
                new InetSocketAddress("localhost", 1234),
                "org.netcrusher.core.filter.LoggingFilter_ArrowTest",
                LoggingFilter.Level.INFO);

        ByteBuffer buffer = ByteBuffer.allocateDirect(2);
        buffer.put(new byte[] {0x01, (byte) 0xab});
        buffer.flip();

        filter.transform(buffer);
        assertEquals(2, buffer.remaining());
    }
}
