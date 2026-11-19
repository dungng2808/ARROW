package io.engineblock.cli;

import ch.qos.logback.classic.Level;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EBCLIOptions_ArrowTest {

    @Test
    public void emptyArgumentsRequestBasicHelp() {
        EBCLIOptions options = new EBCLIOptions(new String[0]);

        assertTrue(options.wantsBasicHelp());
        assertFalse(options.wantsAdvancedHelp());
        assertTrue(options.getCommands().isEmpty());
    }

    @Test
    public void parsesVersionAndHelpOptions() {
        EBCLIOptions options = new EBCLIOptions(new String[] {"--version", "--version-coords", "--advanced-help"});

        assertTrue(options.isWantsVersionShort());
        assertTrue(options.wantsVersionCoords());
        assertTrue(options.wantsAdvancedHelp());
    }

    @Test
    public void parsesTopicalHelp() {
        EBCLIOptions options = new EBCLIOptions(new String[] {"--help", "activity"});

        assertTrue(options.wantsTopicalHelp());
        assertEquals("activity", options.wantsTopicalHelpFor());
    }

    @Test
    public void parsesActivityDefinition() {
        EBCLIOptions options = new EBCLIOptions(new String[] {"run", "name=test", "count=2"});

        assertEquals(1, options.getCommands().size());
        EBCLIOptions.Cmd command = options.getCommands().get(0);
        assertEquals(EBCLIOptions.CmdType.run, command.getCmdType());
        assertEquals("name=test;count=2;", command.getCmdSpec());
    }

    @Test
    public void parsesScriptAndParameters() {
        EBCLIOptions options = new EBCLIOptions(new String[] {"script", "example.js", "key=value"});

        assertEquals(1, options.getCommands().size());
        EBCLIOptions.Cmd command = options.getCommands().get(0);
        assertEquals(EBCLIOptions.CmdType.script, command.getCmdType());
        assertEquals("example.js", command.getCmdSpec());
        Map<String, String> expected = new HashMap<>();
        expected.put("key", "value");
        assertEquals(expected, command.getCmdArgs());
    }

    @Test
    public void parsesExecutionAndLoggingOptions() {
        EBCLIOptions options = new EBCLIOptions(new String[] {
                "--session-name", "session",
                "--logs-dir", "output",
                "--logs-max", "4",
                "--logs-level", "DEBUG",
                "--report-interval", "25",
                "--report-csv-to", "report.csv",
                "--report-graphite-to", "localhost:2003",
                "--metrics-prefix", "custom.",
                "--enable-chart",
                "--docker-metrics",
                "-v"
        });

        assertEquals("session", options.getSessionName());
        assertEquals("output", options.getLogsDirectory());
        assertEquals(4, options.getLogsMax());
        assertEquals("DEBUG", options.getLogsLevel());
        assertEquals(25, options.getReportInterval());
        assertEquals("report.csv", options.wantsReportCsvTo());
        assertEquals("localhost:2003", options.wantsReportGraphiteTo());
        assertEquals("custom.", options.wantsMetricsPrefix());
        assertTrue(options.wantsEnableChart());
        assertTrue(options.wantsDockerMetrics());
        assertEquals(Level.INFO, options.wantsConsoleLogLevel());
    }

    @Test
    public void parsesDiscoveryOptions() {
        EBCLIOptions options = new EBCLIOptions(new String[] {
                "--list-activity-types", "--list-input-types", "--list-output-types"
        });

        assertTrue(options.wantsActivityTypes());
        assertTrue(options.wantsInputTypes());
        assertTrue(options.wantsMarkerTypes());
    }

    @Test
    public void parsesCycleLogOptions() {
        EBCLIOptions options = new EBCLIOptions(new String[] {
                "--export-cycle-log", "one", "two",
                "--import-cycle-log", "three"
        });

        assertTrue(options.wantsToDumpCyclelog());
        assertTrue(options.wantsToImportCycleLog());
        assertArrayEquals(new String[] {"one", "two", "--import-cycle-log", "three"},
                options.getCycleLogExporterOptions());
        assertArrayEquals(new String[0], options.getCyclelogImportOptions());
    }

    @Test
    public void parsesCycleLogImportOptionsWhenLast() {
        EBCLIOptions options = new EBCLIOptions(new String[] {"--import-cycle-log", "one", "two"});

        assertTrue(options.wantsToImportCycleLog());
        assertArrayEquals(new String[] {"one", "two"}, options.getCyclelogImportOptions());
        assertFalse(options.wantsToDumpCyclelog());
    }

    @Test
    public void parsesProgressIndicator() {
        EBCLIOptions options = new EBCLIOptions(new String[] {"--progress", "log:1m"});

        assertEquals("logonly:1m", options.getProgressSpec());
    }

    @Test
    public void parsesConsoleProgressIndicatorAtDefaultLogLevel() {
        EBCLIOptions options = new EBCLIOptions(new String[] {"--progress", "console:1m"});

        assertEquals("logonly:1m", options.getProgressSpec());
    }

    @Test
    public void parsesLogLevelOverrides() {
        EBCLIOptions options = new EBCLIOptions(new String[] {"--log-level-override", "one:DEBUG,two:ERROR"});

        Map<String, Level> expected = new HashMap<>();
        expected.put("one", Level.DEBUG);
        expected.put("two", Level.ERROR);
        assertEquals(expected, options.getLogLevelOverrides());
    }

    @Test
    public void parsesHistogramLoggerConfiguration() {
        EBCLIOptions options = new EBCLIOptions(new String[] {"--log-histograms", "data.log:.*:2m"});

        assertEquals(1, options.getHistoLoggerConfigs().size());
        EBCLIOptions.LoggerConfig config = options.getHistoLoggerConfigs().get(0);
        assertEquals("data.log", config.getFilename());
        assertEquals(".*", config.pattern);
        assertEquals("2m", config.interval);
    }

    @Test
    public void parsesAwaitCommand() {
        EBCLIOptions options = new EBCLIOptions(new String[] {"await", "worker"});

        assertEquals(1, options.getCommands().size());
        assertEquals(EBCLIOptions.CmdType.await, options.getCommands().get(0).getCmdType());
        assertEquals("worker", options.getCommands().get(0).getCmdSpec());
    }

    @Test
    public void parsesWaitMillisCommand() {
        EBCLIOptions options = new EBCLIOptions(new String[] {"waitmillis", "100"});

        assertEquals(1, options.getCommands().size());
        assertEquals(EBCLIOptions.CmdType.waitmillis, options.getCommands().get(0).getCmdType());
        assertEquals("100", options.getCommands().get(0).getCmdSpec());
    }

    @Test(expected = InvalidParameterException.class)
    public void rejectsUnknownOption() {
        new EBCLIOptions(new String[] {"--unknown-option"});
    }

    @Test(expected = InvalidParameterException.class)
    public void rejectsReservedScriptName() {
        new EBCLIOptions(new String[] {"script", "activity"});
    }

    @Test
    public void commandSpecRemovesMatchingQuotes() {
        EBCLIOptions.Cmd command = new EBCLIOptions.Cmd(EBCLIOptions.CmdType.run, "'quoted value'");

        assertEquals("quoted value", command.getCmdSpec());
    }

    @Test
    public void defaultOptionsAreExposed() {
        EBCLIOptions options = new EBCLIOptions(new String[] {"--help"});

        assertEquals(10, options.getReportInterval());
        assertEquals("engineblock.", options.wantsMetricsPrefix());
        assertEquals("", options.getSessionName());
        assertEquals("logs", options.getLogsDirectory());
        assertEquals(0, options.getLogsMax());
        assertEquals("INFO", options.getLogsLevel());
        assertEquals("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n",
                options.getConsoleLoggingPattern());
        assertNull(options.wantsReportCsvTo());
        assertNull(options.wantsReportGraphiteTo());
    }
}
