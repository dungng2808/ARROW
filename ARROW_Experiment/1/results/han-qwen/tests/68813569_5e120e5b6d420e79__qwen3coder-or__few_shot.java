package org.netcrusher.core.filter;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import static org.mockito.Mockito.*;

public class LoggingFilter_ArrowTest {

    @Mock
    private Logger mockLogger;

    private LoggingFilter loggingFilter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        InetSocketAddress clientAddress = new InetSocketAddress("localhost", 8080);
        loggingFilter = new LoggingFilter(clientAddress, "testLogger", LoggingFilter.Level.INFO);
        
        // Replace the logger with our mock
        // Since we can't directly access the logger field, we'll test through behavior
    }

    @Test
    public void testTransformWithEmptyBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(0);
        // This should not cause any issues and should not log anything
        // We can't easily test the actual logging behavior without a real logger
        // but we can verify the method doesn't throw
        loggingFilter.transform(buffer);
    }

    @Test
    public void testTransformWithByteArrayBuffer() {
        byte[] data = {(byte) 0xFF, (byte) 0x00, (byte) 0x7F};
        ByteBuffer buffer = ByteBuffer.wrap(data);
        // Similar to above, we're testing that it doesn't crash
        loggingFilter.transform(buffer);
    }

    @Test
    public void testTransformWithDirectBuffer() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(3);
        buffer.put((byte) 0xAB);
        buffer.put((byte) 0xCD);
        buffer.put((byte) 0xEF);
        buffer.flip();
        // Testing direct buffer handling
        loggingFilter.transform(buffer);
    }

    @Test
    public void testLevelEnumValues() {
        LoggingFilter.Level[] levels = LoggingFilter.Level.values();
        assertEquals(5, levels.length);
        assertEquals(LoggingFilter.Level.TRACE, levels[0]);
        assertEquals(LoggingFilter.Level.DEBUG, levels[1]);
        assertEquals(LoggingFilter.Level.INFO, levels[2]);
        assertEquals(LoggingFilter.Level.WARN, levels[3]);
        assertEquals(LoggingFilter.Level.ERROR, levels[4]);
    }

    @Test
    public void testHexTableCreation() {
        String[] hexTable = LoggingFilter.createHexTable();
        assertNotNull(hexTable);
        assertEquals(256, hexTable.length);
        assertEquals("00", hexTable[0]);
        assertEquals("ff", hexTable[255]);
        assertEquals("7f", hexTable[127]);
    }
}
