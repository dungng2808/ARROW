package io.engineblock.cli;

import ch.qos.logback.classic.Level;
import org.testng.annotations.Test;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

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
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--version", "--version-coords", "--advanced-help", "--help", "activity"
        });

        assertTrue(options.isWantsVersionShort());
        assertTrue(options.wantsVersionCoords());
        assertTrue(options.wantsAdvancedHelp());
        assertTrue(options.wantsTopicalHelp());
        assertEquals(options.wantsTopicalHelpFor(), "activity");
    }

    @Test
    public void parsesActivityAndScriptCommands() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "activity", "name=sample", "count=2",
                "script", "example", "key=value"
        });

        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertEquals(commands.size(), 2);
        assertEquals(commands.get(0).getCmdType(), EBCLIOptions.CmdType.run);
        assertEquals(commands.get(0).getCmdSpec(), "name=sample;count=2;");
        assertEquals(commands.get(1).getCmdType(), EBCLIOptions.CmdType.script);
        assertEquals(commands.get(1).getCmdSpec(), "example");
        assertEquals(commands.get(1).getCmdArgs(), Collections.singletonMap("key", "value"));
    }

    @Test
    public void parsesAwaitStopAndWaitCommands() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "await", "job", "stop", "job", "waitmillis", "250"
        });

        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertEquals(commands.size(), 3);
        assertEquals(commands.get(0).getCmdType(), EBCLIOptions.CmdType.await);
        assertEquals(commands.get(0).getCmdSpec(), "job");
        assertEquals(commands.get(1).getCmdType(), EBCLIOptions.CmdType.stop);
        assertEquals(commands.get(1).getCmdSpec(), "job");
        assertEquals(commands.get(2).getCmdType(), EBCLIOptions.CmdType.waitmillis);
        assertEquals(commands.get(2).getCmdSpec(), "250");
    }

    @Test
    public void parsesConfigurationOptions() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--session-name", "session", "--logs-dir", "output",
                "--logs-max", "4", "--logs-level", "DEBUG",
                "--report-interval", "5", "--report-csv-to", "report.csv",
                "--report-graphite-to", "localhost:2003", "--metrics-prefix", "test."
        });

        assertEquals(options.getSessionName(), "session");
        assertEquals(options.getLogsDirectory(), "output");
        assertEquals(options.getLogsMax(), 4);
        assertEquals(options.getLogsLevel(), "DEBUG");
        assertEquals(options.getReportInterval(), 5);
        assertEquals(options.wantsReportCsvTo(), "report.csv");
        assertEquals(options.wantsReportGraphiteTo(), "localhost:2003");
        assertEquals(options.wantsMetricsPrefix(), "test.");
    }

    @Test
    public void parsesDiscoveryAndLoggingOptions() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--list-activity-types", "--list-input-types", "--list-output-types",
                "--show-script", "-vv", "--with-logging-pattern", "%msg%n",
                "--log-level-override", "one:DEBUG,two:ERROR"
        });

        assertTrue(options.wantsActivityTypes());
        assertTrue(options.wantsInputTypes());
        assertTrue(options.wantsMarkerTypes());
        assertTrue(options.wantsShowScript());
        assertEquals(options.wantsConsoleLogLevel(), Level.DEBUG);
        assertEquals(options.getConsoleLoggingPattern(), "%msg%n");
        Map<String, Level> expected = new HashMap<>();
        expected.put("one", Level.DEBUG);
        expected.put("two", Level.ERROR);
        assertEquals(options.getLogLevelOverrides(), expected);
    }

    @Test
    public void parsesMetricsActivityAndFeatureFlags() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--list-metrics", "name=sample", "--enable-chart", "--docker-metrics"
        });

        assertEquals(options.wantsMetricsForActivity(), "name=sample;");
        assertTrue(options.wantsEnableChart());
        assertTrue(options.wantsDockerMetrics());
    }

    @Test
    public void parsesCycleLogOptions() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--export-cycle-log", "one", "two", "--import-cycle-log", "three", "four"
        });

        assertTrue(options.wantsToDumpCyclelog());
        assertTrue(options.wantsToImportCycleLog());
        assertEquals(Arrays.asList(options.getCycleLogExporterOptions()), Arrays.asList("one", "two", "--import-cycle-log", "three", "four"));
        assertEquals(Arrays.asList(options.getCyclelogImportOptions()), new String[0]);
    }

    @Test
    public void parsesHistogramLoggerConfigurations() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--log-histograms", "data.log:latency:2m",
                "--log-histostats", "stats.log:.*:1m",
                "--classic-histograms", "classic.log"
        });

        List<EBCLIOptions.LoggerConfig> histograms = options.getHistoLoggerConfigs();
        List<EBCLIOptions.LoggerConfig> stats = options.getStatsLoggerConfigs();
        List<EBCLIOptions.LoggerConfig> classic = options.getClassicHistoConfigs();

        assertEquals(histograms.size(), 1);
        assertEquals(histograms.get(0).getFilename(), "data.log");
        assertEquals(histograms.get(0).pattern, "latency");
        assertEquals(histograms.get(0).interval, "2m");
        assertEquals(stats.get(0).getFilename(), "stats.log");
        assertEquals(classic.get(0).getFilename(), "classic.log");
    }

    @Test
    public void setHistogramLoggerConfigAddsFormattedConfiguration() {
        EBCLIOptions options = new EBCLIOptions(new String[0]);
        options.setHistoLoggerConfigs("latency", "data.log", "2m");

        EBCLIOptions.LoggerConfig config = options.getHistoLoggerConfigs().get(0);
        assertEquals(config.getFilename(), "data.log");
        assertEquals(config.pattern, "latency");
        assertEquals(config.interval, "2m");
    }

    @Test
    public void progressSpecDefaultsAndCanBeConfigured() {
        EBCLIOptions defaults = new EBCLIOptions(new String[0]);
        assertEquals(defaults.getProgressSpec(), "console:1m");

        EBCLIOptions configured = new EBCLIOptions(new String[]{"--progress", "log:5s"});
        assertEquals(configured.getProgressSpec(), "logonly:5s");
    }

    @Test
    public void consoleProgressIsSuppressedAtInfoLevel() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--progress", "console:10s", "-v"});

        assertEquals(options.getProgressSpec(), "logonly:10s");
    }

    @Test
    public void parsesConsoleLoggingLevels() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"-vvv"});

        assertEquals(options.wantsConsoleLogLevel(), Level.TRACE);
    }

    @Test
    public void commandSpecRemovesMatchingQuotes() {
        EBCLIOptions.Cmd singleQuoted = new EBCLIOptions.Cmd(EBCLIOptions.CmdType.script, "'script name'");
        EBCLIOptions.Cmd doubleQuoted = new EBCLIOptions.Cmd(EBCLIOptions.CmdType.script, "\"script name\"");

        assertEquals(singleQuoted.getCmdSpec(), "script name");
        assertEquals(doubleQuoted.getCmdSpec(), "script name");
    }

    @Test
    public void commandStringIncludesTypeAndArguments() {
        Map<String, String> args = new HashMap<>();
        args.put("key", "value");
        EBCLIOptions.Cmd command = new EBCLIOptions.Cmd(EBCLIOptions.CmdType.script, "example", args);

        assertEquals(command.toString(), "type:script;spec=example;cmdArgs={key=value}");
    }

    @Test
    public void emptyCycleLogOptionsAreNotRequested() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--export-cycle-log"});

        assertFalse(options.wantsToDumpCyclelog());
        assertFalse(options.wantsToImportCycleLog());
        assertEquals(options.getCycleLogExporterOptions().length, 0);
    }

    @Test
    public void missingRequiredOptionValueThrowsInvalidParameterException() {
        try {
            new EBCLIOptions(new String[]{"--session-name"});
        } catch (InvalidParameterException expected) {
            return;
        }
        throw new AssertionError("Expected InvalidParameterException");
    }

    @Test
    public void unrecognizedOptionThrowsInvalidParameterException() {
        try {
            new EBCLIOptions(new String[]{"--not-an-option"});
        } catch (InvalidParameterException expected) {
            return;
        }
        throw new AssertionError("Expected InvalidParameterException");
    }

    @Test
    public void defaultReportTargetsAreNull() {
        EBCLIOptions options = new EBCLIOptions(new String[0]);

        assertNull(options.wantsReportCsvTo());
        assertNull(options.wantsReportGraphiteTo());
        assertNull(options.wantsMetricsForActivity());
    }
}
