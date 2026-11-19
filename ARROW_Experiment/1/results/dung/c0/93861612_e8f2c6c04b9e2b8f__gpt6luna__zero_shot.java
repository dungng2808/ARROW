package com.trivago.logging;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class CucableLogger_ArrowTest {

    @Test
    public void infoIsLoggedAtDefaultLevel() {
        Log logger = mock(Log.class);
        CucableLogger cucableLogger = new CucableLogger();
        cucableLogger.initialize(logger, null);

        cucableLogger.info("message", CucableLogger.CucableLogLevel.DEFAULT);

        verify(logger).info("message");
    }

    @Test
    public void infoIsNotLoggedWhenLevelDoesNotMatch() {
        Log logger = mock(Log.class);
        CucableLogger cucableLogger = new CucableLogger();
        cucableLogger.initialize(logger, "minimal");

        cucableLogger.info("message", CucableLogger.CucableLogLevel.DEFAULT);

        verifyNoInteractions(logger);
    }

    @Test
    public void infoIsLoggedWhenLevelMatches() {
        Log logger = mock(Log.class);
        CucableLogger cucableLogger = new CucableLogger();
        cucableLogger.initialize(logger, "compact");

        cucableLogger.info("message", CucableLogger.CucableLogLevel.COMPACT);

        verify(logger).info("message");
    }

    @Test
    public void warnIsLoggedAtMinimalLevel() {
        Log logger = mock(Log.class);
        CucableLogger cucableLogger = new CucableLogger();
        cucableLogger.initialize(logger, "minimal");

        cucableLogger.warn("warning");

        verify(logger).warn("warning");
    }

    @Test
    public void warnIsNotLoggedAtOffLevel() {
        Log logger = mock(Log.class);
        CucableLogger cucableLogger = new CucableLogger();
        cucableLogger.initialize(logger, "off");

        cucableLogger.warn("warning");

        verifyNoInteractions(logger);
    }

    @Test
    public void unknownLevelWarnsAndUsesDefault() {
        Log logger = mock(Log.class);
        CucableLogger cucableLogger = new CucableLogger();

        cucableLogger.initialize(logger, "unknown");

        verify(logger).warn("Log level unknown is unknown. Cucable will use 'default' logging.");
        cucableLogger.info("message", CucableLogger.CucableLogLevel.DEFAULT);
        verify(logger).info("message");
    }

    @Test
    public void separatorIsLoggedAtMatchingLevel() {
        Log logger = mock(Log.class);
        CucableLogger cucableLogger = new CucableLogger();
        cucableLogger.initialize(logger, "default");

        cucableLogger.logInfoSeparator(CucableLogger.CucableLogLevel.DEFAULT);

        verify(logger).info("-------------------------------------");
    }

    @Test
    public void emptyLevelListLogsInfo() {
        Log logger = mock(Log.class);
        CucableLogger cucableLogger = new CucableLogger();
        cucableLogger.initialize(logger, "off");

        cucableLogger.info("message", new CucableLogger.CucableLogLevel[0]);

        verify(logger).info("message");
    }
}
