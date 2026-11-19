package com.trivago.logging;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CucableLogger_ArrowTest {

    private CucableLogger cucableLogger;
    private Log mockLog;

    @Before
    public void setUp() {
        cucableLogger = new CucableLogger();
        mockLog = mock(Log.class);
    }

    @Test
    public void testInitializeWithNullLogLevel() {
        cucableLogger.initialize(mockLog, null);
        verifyNoInteractions(mockLog);
    }

    @Test
    public void testInitializeWithValidLogLevel() {
        cucableLogger.initialize(mockLog, "default");
        verifyNoInteractions(mockLog);
    }

    @Test
    public void testInitializeWithInvalidLogLevel() {
        cucableLogger.initialize(mockLog, "invalid");
        verifyNoInteractions(mockLog);
    }

    @Test
    public void testLogInfoSeparator() {
        cucableLogger.initialize(mockLog, "default");
        cucableLogger.logInfoSeparator(CucableLogger.CucableLogLevel.DEFAULT);
        verify(mockLog).info("-------------------------------------");
    }

    @Test
    public void testInfoWithMatchingLogLevel() {
        cucableLogger.initialize(mockLog, "default");
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.DEFAULT);
        verify(mockLog).info("test message");
    }

    @Test
    public void testInfoWithNonMatchingLogLevel() {
        cucableLogger.initialize(mockLog, "default");
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.COMPACT);
        verifyNoInteractions(mockLog);
    }

    @Test
    public void testWarnAlwaysLogs() {
        cucableLogger.initialize(mockLog, "default");
        cucableLogger.warn("warning message");
        verify(mockLog).warn("warning message");
    }

    @Test
    public void testLogWithNullCurrentLogLevel() {
        cucableLogger.initialize(mockLog, null);
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.DEFAULT);
        verifyNoInteractions(mockLog);
    }

    @Test
    public void testLogWithNullLogLevels() {
        cucableLogger.initialize(mockLog, "default");
        cucableLogger.info("test message", (CucableLogger.CucableLogLevel[]) null);
        verifyNoInteractions(mockLog);
    }

    @Test
    public void testLogWithEmptyLogLevels() {
        cucableLogger.initialize(mockLog, "default");
        cucableLogger.info("test message");
        verifyNoInteractions(mockLog);
    }

    @Test
    public void testLogWithMatchingLogLevelInArray() {
        cucableLogger.initialize(mockLog, "compact");
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT);
        verify(mockLog).info("test message");
    }

    @Test
    public void testLogWithNonMatchingLogLevelInArray() {
        cucableLogger.initialize(mockLog, "compact");
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.MINIMAL);
        verifyNoInteractions(mockLog);
    }
}
