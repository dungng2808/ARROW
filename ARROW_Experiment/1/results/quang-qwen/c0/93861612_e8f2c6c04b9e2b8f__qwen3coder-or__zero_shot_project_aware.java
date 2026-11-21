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
        verifyNoMoreInteractions(mockLog);
    }
    
    @Test
    public void testInitializeWithValidLogLevel() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        verifyNoMoreInteractions(mockLog);
    }
    
    @Test
    public void testInitializeWithInvalidLogLevel() {
        cucableLogger.initialize(mockLog, "INVALID");
        verifyNoMoreInteractions(mockLog);
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
        verifyNoMoreInteractions(mockLog);
    }
    
    @Test
    public void testWarn() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.warn("warning message");
        verify(mockLog).warn("warning message");
    }
    
    @Test
    public void testLogWithInfoAndMatchingLogLevel() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.log(CucableLogger.LogLevel.INFO, "test message", CucableLogger.CucableLogLevel.DEFAULT);
        verify(mockLog).info("test message");
    }
    
    @Test
    public void testLogWithInfoAndNonMatchingLogLevel() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.log(CucableLogger.LogLevel.INFO, "test message", CucableLogger.CucableLogLevel.COMPACT);
        verifyNoMoreInteractions(mockLog);
    }
    
    @Test
    public void testLogWithWarn() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.log(CucableLogger.LogLevel.WARN, "test message", CucableLogger.CucableLogLevel.DEFAULT);
        verify(mockLog).warn("test message");
    }
    
    @Test
    public void testLogWithNullCurrentLogLevel() {
        cucableLogger.log(CucableLogger.LogLevel.INFO, "test message", CucableLogger.CucableLogLevel.DEFAULT);
        verifyNoMoreInteractions(mockLog);
    }
    
    @Test
    public void testLogWithNullLogLevels() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.log(CucableLogger.LogLevel.INFO, "test message", null);
        verifyNoMoreInteractions(mockLog);
    }
    
    @Test
    public void testLogWithEmptyLogLevels() {
        cucableLogger.initialize(mockLog, "DEFAULT");
        cucableLogger.log(CucableLogger.LogLevel.INFO, "test message");
        verifyNoMoreInteractions(mockLog);
    }
}
