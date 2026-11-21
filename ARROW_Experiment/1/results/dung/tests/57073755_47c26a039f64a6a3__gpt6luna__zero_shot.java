package io.engineblock.cli;

import ch.qos.logback.classic.Level;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EBCLIOptions_ArrowTest {

    @Test
    public void emptyArgumentsRequestBasicHelpAndKeepDefaults() {
        EBCLIOptions options = new EBCLIOptions(new String[0]);

        assertTrue(options.wantsBasicHelp());
        assertFalse(options.wantsAdvancedHelp());
        assertEquals(10, options.getReportInterval());
        assertEquals("engineblock.", options.wantsMetricsPrefix());
        assertEquals("", options.getSessionName());
        assertEquals("logs", options.getLogsDirectory());
        assertEquals(0, options.getLogsMax());
        assertEquals("INFO", options.getLogsLevel());
        assertEquals(Level.WARN, options.wantsConsoleLogLevel());
    }

    @Test
    public void parsesVersionAndHelpOptions() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--version", "--version-coords", "--advanced-help", "--help", "topic"
        });

        assertTrue(options.isWantsVersionShort());
        assertTrue(options.wantsVersionCoords());
        assertTrue(options.wantsAdvancedHelp());
        assertTrue(options.wantsTopicalHelp());
        assertEquals("topic", options.wantsTopicalHelpFor());
        assertFalse(options.wantsBasicHelp());
    }

    @Test
    public void parsesActivityAndScriptCommands() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "activity", "rate=5", "size=10", "script", "sample.js", "key=value"
        });

        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertEquals(2, commands.size());
        assertEquals(EBCLIOptions.CmdType.run, commands.get(0).getCmdType());
        assertEquals("rate=5;size=10;", commands.get(0).getCmdSpec());
        assertEquals(EBCLIOptions.CmdType.script, commands.get(1).getCmdType());
        assertEquals("sample.js", commands.get(1).getCmdSpec());
        assertEquals(Collections.singletonMap("key", "value"), commands.get(1).getCmdArgs());
    }

    @Test
    public void parsesFragmentAndWaitCommands() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "fragment", "fragment", "part", "waitmillis", "waitmillis", "250"
        });

        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertEquals(2, commands.size());
        assertEquals(EBCLIOptions.CmdType.fragment, commands.get(0).getCmdType());
        assertEquals("part", commands.get(0).getCmdSpec());
        assertEquals(EBCLIOptions.CmdType.waitmillis, commands.get(1).getCmdType());
        assertEquals("250", commands.get(1).getCmdSpec());
    }

    @Test
    public void parsesDiscoveryAndReportingOptions() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--list-activity-types", "--list-input-types", "--list-output-types",
                "--report-interval", "5", "--report-csv-to", "report.csv",
                "--report-graphite-to", "localhost:2003", "--metrics-prefix", "custom."
        });

        assertTrue(options.wantsActivityTypes());
        assertTrue(options.wantsInputTypes());
        assertTrue(options.wantsMarkerTypes());
        assertEquals(5, options.getReportInterval());
        assertEquals("report.csv", options.wantsReportCsvTo());
        assertEquals("localhost:2003", options.wantsReportGraphiteTo());
        assertEquals("custom.", options.wantsMetricsPrefix());
    }

    @Test
    public void parsesMetricsActivityAndSessionOptions() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--list-metrics", "rate=5", "--session-name", "session1",
                "--logs-dir", "output", "--logs-max", "3", "--logs-level", "DEBUG"
        });

        assertEquals("rate=5;", options.wantsMetricsForActivity());
        assertEquals("session1", options.getSessionName());
        assertEquals("output", options.getLogsDirectory());
        assertEquals(3, options.getLogsMax());
        assertEquals("DEBUG", options.getLogsLevel());
    }

    @Test
    public void parsesLoggingAndProgressOptions() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "-vv", "--progress", "log:1m", "--with-logging-pattern", "%msg%n"
        });

        assertEquals(Level.DEBUG, options.wantsConsoleLogLevel());
        assertEquals("logonly:1m", options.getProgressSpec());
        assertEquals("%msg%n", options.getConsoleLoggingPattern());
    }

    @Test
    public void parsesProgressModeAndInterval() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"-vvv", "--progress", "console:1m"});

        assertEquals(Level.TRACE, options.wantsConsoleLogLevel());
        assertEquals("console:1m", options.getProgressSpec());
    }

    @Test
    public void parsesCycleLogOptions() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--export-cycle-log", "one", "two", "--import-cycle-log", "three", "four"
        });

        assertTrue(options.wantsToDumpCyclelog());
        assertTrue(options.wantsToImportCycleLog());
        assertArrayEquals(new String[]{"one", "two", "--import-cycle-log", "three", "four"},
                options.getCycleLogExporterOptions());
        assertArrayEquals(new String[0], options.getCyclelogImportOptions());
    }

    @Test
    public void parsesLogLevelOverrides() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--log-level-override", "alpha:DEBUG,beta:ERROR"
        });

        Map<String, Level> expected = new HashMap<>();
        expected.put("alpha", Level.DEBUG);
        expected.put("beta", Level.ERROR);
        assertEquals(expected, options.getLogLevelOverrides());
    }

    @Test
    public void parsesHistogramLoggerConfiguration() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--log-histograms", "data.log:.*:2m", "--log-histostats", "stats.log",
                "--classic-histograms", "classic.log"
        });

        assertEquals("data.log", options.getHistoLoggerConfigs().get(0).getFilename());
        assertEquals("stats.log", options.getStatsLoggerConfigs().get(0).getFilename());
        assertEquals("classic.log", options.getClassicHistoConfigs().get(0).getFilename());
    }

    @Test
    public void addsHistogramLoggerConfiguration() {
        EBCLIOptions options = new EBCLIOptions(new String[0]);
        options.setHistoLoggerConfigs("latency.*", "latency.log", "1m");

        assertEquals("latency.log", options.getHistoLoggerConfigs().get(0).getFilename());
    }

    @Test
    public void parsesBooleanOptionsAndShowScript() {
        EBCLIOptions options = new EBCLIOptions(new String[]{
                "--show-script", "--enable-chart", "--docker-metrics"
        });

        assertTrue(options.wantsShowScript());
        assertTrue(options.wantsEnableChart());
        assertTrue(options.wantsDockerMetrics());
    }

    @Test
    public void commandSpecRemovesMatchingQuotes() {
        EBCLIOptions.Cmd singleQuoted = new EBCLIOptions.Cmd(EBCLIOptions.CmdType.script, "'script.js'");
        EBCLIOptions.Cmd doubleQuoted = new EBCLIOptions.Cmd(EBCLIOptions.CmdType.script, "\"script.js\"");
        EBCLIOptions.Cmd unquoted = new EBCLIOptions.Cmd(EBCLIOptions.CmdType.script, "script.js");

        assertEquals("script.js", singleQuoted.getCmdSpec());
        assertEquals("script.js", doubleQuoted.getCmdSpec());
        assertEquals("script.js", unquoted.getCmdSpec());
    }

    @Test
    public void commandToStringIncludesTypeAndArguments() {
        Map<String, String> args = new HashMap<>();
        args.put("x", "1");
        EBCLIOptions.Cmd command = new EBCLIOptions.Cmd(EBCLIOptions.CmdType.script, "test.js", args);

        assertEquals("type:script;spec=test.js;cmdArgs={x=1}", command.toString());
    }

    @Test
    public void loggerConfigUsesDefaultsForMissingFields() {
        EBCLIOptions.LoggerConfig config = new EBCLIOptions.LoggerConfig("data.log");

        assertEquals("data.log", config.getFilename());
        assertEquals(".*", config.pattern);
        assertEquals("30 seconds", config.interval);
    }

    @Test
    public void loggerConfigParsesPatternAndInterval() {
        EBCLIOptions.LoggerConfig config = new EBCLIOptions.LoggerConfig("data.log:latency.*:2m");

        assertEquals("data.log", config.getFilename());
        assertEquals("latency.*", config.pattern);
        assertEquals("2m", config.interval);
    }

    @Test(expected = RuntimeException.class)
    public void rejectsEmptyLoggerFilename() {
        new EBCLIOptions.LoggerConfig("");
    }

    @Test(expected = RuntimeException.class)
    public void rejectsLoggerConfigurationWithTooManyFields() {
        new EBCLIOptions.LoggerConfig("a:b:c:d");
    }

    @Test(expected = InvalidParameterException.class)
    public void rejectsUnknownOption() {
        new EBCLIOptions(new String[]{"--unknown-option"});
    }

    @Test(expected = InvalidParameterException.class)
    public void rejectsScriptNameThatIsReserved() {
        new EBCLIOptions(new String[]{"script", "activity"});
    }

    @Test(expected = InvalidParameterException.class)
    public void rejectsAwaitAliasThatIsReserved() {
        new EBCLIOptions(new String[]{"await", "await", "activity"});
    }

    @Test(expected = NumberFormatException.class)
    public void rejectsNonNumericWaitDuration() {
        new EBCLIOptions(new String[]{"waitmillis", "waitmillis", "not-a-number"});
    }
}
