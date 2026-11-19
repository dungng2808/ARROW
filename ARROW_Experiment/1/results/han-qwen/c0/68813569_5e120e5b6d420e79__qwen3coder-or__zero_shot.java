package org.netcrusher.core.filter;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import static org.mockito.Mockito.*;

public class LoggingFilter_ArrowTest {

    @Test
    public void testTransformWithEmptyBuffer() {
        InetSocketAddress address = new InetSocketAddress("localhost", 8080);
        Logger logger = Mockito.mock(Logger.class);
        Level level = Level.INFO;
        
        // Mock the logger to return true for isInfoEnabled
        when(logger.isInfoEnabled()).thenReturn(true);
        
        // Create a LoggingFilter instance with mocked logger
        LoggingFilter filter = new LoggingFilter(address, "testLogger", level) {
            @Override
            protected Logger getLogger() {
                return logger;
            }
        };
        
        // Create an empty ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(0);
        
        // Call transform method
        filter.transform(buffer);
        
        // Verify that logger.info was called with correct parameters
        verify(logger).info("<{}> ({}): {}", address, 0, "");
    }

    @Test
    public void testTransformWithNonEmptyBuffer() {
        InetSocketAddress address = new InetSocketAddress("localhost", 8080);
        Logger logger = Mockito.mock(Logger.class);
        Level level = Level.DEBUG;
        
        // Mock the logger to return true for isDebugEnabled
        when(logger.isDebugEnabled()).thenReturn(true);
        
        // Create a LoggingFilter instance with mocked logger
        LoggingFilter filter = new LoggingFilter(address, "testLogger", level) {
            @Override
            protected Logger getLogger() {
                return logger;
            }
        };
        
        // Create a ByteBuffer with some data
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{(byte) 0x48, (byte) 0x65, (byte) 0x6C, (byte) 0x6C, (byte) 0x6F});
        
        // Call transform method
        filter.transform(buffer);
        
        // Verify that logger.debug was called with correct parameters
        verify(logger).debug("<{}> ({}): {}", address, 5, "48656c6c6f");
    }

    @Test
    public void testTransformWithNonDirectBuffer() {
        InetSocketAddress address = new InetSocketAddress("localhost", 8080);
        Logger logger = Mockito.mock(Logger.class);
        Level level = Level.TRACE;
        
        // Mock the logger to return true for isTraceEnabled
        when(logger.isTraceEnabled()).thenReturn(true);
        
        // Create a LoggingFilter instance with mocked logger
        LoggingFilter filter = new LoggingFilter(address, "testLogger", level) {
            @Override
            protected Logger getLogger() {
                return logger;
            }
        };
        
        // Create a ByteBuffer with some data
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put(new byte[]{(byte) 0x41, (byte) 0x42, (byte) 0x43, (byte) 0x44, (byte) 0x45});
        buffer.flip();
        
        // Call transform method
        filter.transform(buffer);
        
        // Verify that logger.trace was called with correct parameters
        verify(logger).trace("<{}> ({}): {}", address, 5, "4142434445");
    }

    @Test
    public void testTransformWhenLogDisabled() {
        InetSocketAddress address = new InetSocketAddress("localhost", 8080);
        Logger logger = Mockito.mock(Logger.class);
        Level level = Level.WARN;
        
        // Mock the logger to return false for isWarnEnabled
        when(logger.isWarnEnabled()).thenReturn(false);
        
        // Create a LoggingFilter instance with mocked logger
        LoggingFilter filter = new LoggingFilter(address, "testLogger", level) {
            @Override
            protected Logger getLogger() {
                return logger;
            }
        };
        
        // Create a ByteBuffer with some data
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{(byte) 0x48, (byte) 0x65, (byte) 0x6C, (byte) 0x6C, (byte) 0x6F});
        
        // Call transform method
        filter.transform(buffer);
        
        // Verify that logger.warn was never called
        verify(logger, never()).warn(anyString(), any(), anyInt(), any());
    }

    @Test
    public void testTransformWithNullLogger() {
        InetSocketAddress address = new InetSocketAddress("localhost", 8080);
        Level level = Level.ERROR;
        
        // Create a LoggingFilter instance with null logger
        LoggingFilter filter = new LoggingFilter(address, "testLogger", level) {
            @Override
            protected Logger getLogger() {
                return null;
            }
        };
        
        // Create a ByteBuffer with some data
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{(byte) 0x48, (byte) 0x65, (byte) 0x6C, (byte) 0x6C, (byte) 0x6F});
        
        // Call transform method - should not throw exception
        filter.transform(buffer);
    }

    // Helper method to access the logger field for testing purposes
    private Logger getLogger() {
        return LoggerFactory.getLogger("testLogger");
    }
}
