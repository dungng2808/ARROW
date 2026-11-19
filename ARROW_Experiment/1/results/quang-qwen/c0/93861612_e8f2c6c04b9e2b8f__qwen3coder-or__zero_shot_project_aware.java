package com.trivago.logging;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class CucableLogger_ArrowTest {

    private CucableLogger cucableLogger;
    private Log mockLog;
    
    @Before
    public void setUp() {
        cucableLogger = new CucableLogger();
        mockLog = Mockito.mock(Log.class);
    }
    
    @Test
    public void testInitializeWithNullLogLevel() {
        cucableLogger.initialize(mockLog, null);
        verifyZeroInteractions(mockLog);
    }
    
    @Test
    public void testInitializeWithValidLogLevel() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        verifyZeroInteractions(mockLog);
    }
    
    @Test
    public void testInitializeWithInvalidLogLevel() {
        cucableLogger.initialize(mockLog, "INVALID");
        verifyZeroInteractions(mockLog);
    }
    
    @Test
    public void testLogInfoSeparator() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.logInfoSeparator(CucableLogger.CucableLogLevel.DEFAULT);
        verify(mockLog).info("-------------------------------------");
    }
    
    @Test
    public void testInfoWithMatchingLogLevel() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.DEFAULT);
        verify(mockLog).info("test message");
    }
    
    @Test
    public void testInfoWithNonMatchingLogLevel() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.COMPACT);
        verifyZeroInteractions(mockLog);
    }
    
    @Test
    public void testWarn() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.warn("warning message");
        verify(mockLog).warn("warning message");
    }
    
    @Test
    public void testLogWithNullCurrentLogLevel() {
        cucableLogger.initialize(mockLog, null);
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.DEFAULT);
        verifyZeroInteractions(mockLog);
    }
    
    @Test
    public void testLogWithNullLogLevels() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.info("test message", (CucableLogger.CucableLogLevel[]) null);
        verifyZeroInteractions(mockLog);
    }
    
    @Test
    public void testLogWithEmptyLogLevels() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.info("test message");
        verifyZeroInteractions(mockLog);
    }
    
    @Test
    public void testLogWithNoMatchingLogLevels() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.COMPACT, CucableLogger.CucableLogLevel.MINIMAL);
        verifyZeroInteractions(mockLog);
    }
    
    @Test
    public void testLogWithMatchingLogLevels() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT);
        verify(mockLog).info("test message");
    }
    
    @Test
    public void testLogWithWarnLevel() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.warn("warning message");
        verify(mockLog).warn("warning message");
    }
    
    @Test
    public void testInitializeWithAllLogLevels() {
        for (CucableLogger.CucableLogLevel level : CucableLogger.CucableLogLevel.values()) {
            cucableLogger.initialize(mockLog, level.name());
            verifyZeroInteractions(mockLog);
        }
    }
    
    @Test
    public void testLogWithOffLogLevel() {
        cucableLogger.initialize(mockLog, "OFF");
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.DEFAULT);
        verifyZeroInteractions(mockLog);
    }
    
    @Test
    public void testLogWithCompactLogLevel() {
        cucableLogger.initialize(mockLog, "COMPACT");
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.COMPACT);
        verify(mockLog).info("test message");
    }
    
    @Test
    public void testLogWithMinimalLogLevel() {
        cucableLogger.initialize(mockLog, "MINIMAL");
        cucableLogger.info("test message", CucableLogger.CucableLogLevel.MINIMAL);
        verify(mockLog).info("test message");
    }
}
