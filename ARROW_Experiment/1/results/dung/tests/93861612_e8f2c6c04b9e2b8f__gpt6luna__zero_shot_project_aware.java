package com.trivago.logging;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class CucableLogger_ArrowTest {

    @Test
    public void infoLogsWhenCurrentLevelIsIncluded() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "compact");

        logger.info("message", CucableLogger.CucableLogLevel.COMPACT);

        verify(log).info("message");
    }

    @Test
    public void infoDoesNotLogWhenCurrentLevelIsNotIncluded() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "compact");

        logger.info("message", CucableLogger.CucableLogLevel.DEFAULT);

        verifyNoInteractions(log);
    }

    @Test
    public void defaultLevelIsUsedWhenLevelIsNull() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, null);

        logger.info("message", CucableLogger.CucableLogLevel.DEFAULT);

        verify(log).info("message");
    }

    @Test
    public void unknownLevelWarnsAndUsesDefault() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "unknown");

        verify(log).warn("Log level unknown is unknown. Cucable will use 'default' logging.");
    }

    @Test
    public void warnLogsAtDefaultLevel() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "default");

        logger.warn("warning");

        verify(log).warn("warning");
    }

    @Test
    public void warnDoesNotLogWhenLevelIsOff() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "off");

        logger.warn("warning");

        verifyNoInteractions(log);
    }

    @Test
    public void separatorLogsAtIncludedLevel() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "minimal");

        logger.logInfoSeparator(CucableLogger.CucableLogLevel.MINIMAL);

        verify(log).info("-------------------------------------");
    }

    @Test
    public void separatorDoesNotLogAtExcludedLevel() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "minimal");

        logger.logInfoSeparator(CucableLogger.CucableLogLevel.DEFAULT);

        verifyNoInteractions(log);
    }
}
