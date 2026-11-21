package com.trivago.logging;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class CucableLogger_ArrowTest {

    @Test
    public void infoIsLoggedAtMatchingLevel() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "default");

        logger.info("message", CucableLogger.CucableLogLevel.DEFAULT);

        verify(log).info("message");
    }

    @Test
    public void infoIsNotLoggedAtNonMatchingLevel() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "compact");

        logger.info("message", CucableLogger.CucableLogLevel.DEFAULT);

        verifyNoInteractions(log);
    }

    @Test
    public void warnIsLoggedForMinimalLevel() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "minimal");

        logger.warn("warning");

        verify(log).warn("warning");
    }

    @Test
    public void warnIsNotLoggedWhenOff() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "off");

        logger.warn("warning");

        verifyNoInteractions(log);
    }

    @Test
    public void nullLevelUsesDefault() {
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
        logger.info("message", CucableLogger.CucableLogLevel.DEFAULT);

        verify(log).info("message");
    }

    @Test
    public void separatorUsesProvidedLevels() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "compact");

        logger.logInfoSeparator(CucableLogger.CucableLogLevel.COMPACT);

        verify(log).info("-------------------------------------");
    }

    @Test
    public void offLevelSuppressesInfo() {
        Log log = mock(Log.class);
        CucableLogger logger = new CucableLogger();
        logger.initialize(log, "off");

        logger.info("message", CucableLogger.CucableLogLevel.OFF);

        verify(log).info("message");
    }
}
