package io.engineblock.cli;

import ch.qos.logback.classic.Level;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class EBCLIOptions_ArrowTest {

    @Test
    public void testParseEmptyArgs() {
        EBCLIOptions options = new EBCLIOptions(new String[]{});
        assertTrue(options.wantsBasicHelp());
    }

    @Test
    public void testParseHelpFlag() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--help"});
        assertTrue(options.wantsBasicHelp());
    }

    @Test
    public void testParseHelpWithTopic() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--help", "activity"});
        assertTrue(options.wantsTopicalHelp());
        assertEquals("activity", options.wantsTopicalHelpFor());
    }

    @Test
    public void testParseVersionShort() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--version"});
        assertTrue(options.isWantsVersionShort());
    }

    @Test
    public void testParseVersionCoords() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--version-coords"});
        assertTrue(options.wantsVersionCoords());
    }

    @Test
    public void testParseActivityTypes() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--list-activity-types"});
        assertTrue(options.wantsActivityTypes());
    }

    @Test
    public void testParseInputTypes() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--list-input-types"});
        assertTrue(options.wantsInputTypes());
    }

    @Test
    public void testParseOutputTypes() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--list-output-types"});
        assertTrue(options.wantsMarkerTypes());
    }

    @Test
    public void testParseSessionName() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--session-name", "test-session"});
        assertEquals("test-session", options.getSessionName());
    }

    @Test
    public void testParseLogsDir() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--logs-dir", "/tmp/logs"});
        assertEquals("/tmp/logs", options.getLogsDirectory());
    }

    @Test
    public void testParseLogsMax() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--logs-max", "5"});
        assertEquals(5, options.getLogsMax());
    }

    @Test
    public void testParseLogsLevel() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--logs-level", "DEBUG"});
        assertEquals("DEBUG", options.getLogsLevel());
    }

    @Test
    public void testParseReportInterval() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--report-interval", "30"});
        assertEquals(30, options.getReportInterval());
    }

    @Test
    public void testParseMetricsPrefix() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--metrics-prefix", "myapp."});
        assertEquals("myapp.", options.wantsMetricsPrefix());
    }

    @Test
    public void testParseReportGraphiteTo() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--report-graphite-to", "localhost:2003"});
        assertEquals("localhost:2003", options.wantsReportGraphiteTo());
    }

    @Test
    public void testParseReportCsvTo() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--report-csv-to", "/tmp/report.csv"});
        assertEquals("/tmp/report.csv", options.wantsReportCsvTo());
    }

    @Test
    public void testParseProgressIndicator() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--progress", "console:1m"});
        assertEquals("console:1m", options.getProgressSpec());
    }

    @Test
    public void testParseConsoleLoggingPattern() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--with-logging-pattern", "%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"});
        assertEquals("%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n", options.getConsoleLoggingPattern());
    }

    @Test
    public void testParseLogLevelOverride() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--log-level-override", "com.example:DEBUG"});
        Map<String, Level> overrides = options.getLogLevelOverrides();
        assertNotNull(overrides);
        assertTrue(overrides.containsKey("com.example"));
        assertEquals(Level.DEBUG, overrides.get("com.example"));
    }

    @Test
    public void testParseShowScript() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--show-script"});
        assertTrue(options.wantsShowScript());
    }

    @Test
    public void testParseEnableChart() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--enable-chart"});
        assertTrue(options.wantsEnableChart());
    }

    @Test
    public void testParseDockerMetrics() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--docker-metrics"});
        assertTrue(options.wantsDockerMetrics());
    }

    @Test
    public void testParseDebugConsoleLogging() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"-vv"});
        assertEquals(Level.DEBUG, options.wantsConsoleLogLevel());
    }

    @Test
    public void testParseInfoConsoleLogging() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"-v"});
        assertEquals(Level.INFO, options.wantsConsoleLogLevel());
    }

    @Test
    public void testParseTraceConsoleLogging() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"-vvv"});
        assertEquals(Level.TRACE, options.wantsConsoleLogLevel());
    }

    @Test
    public void testParseScriptCommand() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"script", "test.js"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertEquals(1, commands.size());
        assertEquals(EBCLIOptions.CmdType.script, commands.get(0).getCmdType());
        assertEquals("test.js", commands.get(0).getCmdSpec());
    }

    @Test
    public void testParseActivityCommand() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"run", "activity1", "param1=value1"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertEquals(1, commands.size());
        assertEquals(EBCLIOptions.CmdType.run, commands.get(0).getCmdType());
        assertEquals("param1=value1;", commands.get(0).getCmdSpec());
    }

    @Test
    public void testParseFragmentCommand() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"fragment", "test_fragment"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertEquals(1, commands.size());
        assertEquals(EBCLIOptions.CmdType.fragment, commands.get(0).getCmdType());
        assertEquals("test_fragment", commands.get(0).getCmdSpec());
    }

    @Test
    public void testParseAwaitCommand() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"await", "activity1"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertEquals(1, commands.size());
        assertEquals(EBCLIOptions.CmdType.await, commands.get(0).getCmdType());
        assertEquals("activity1", commands.get(0).getCmdSpec());
    }

    @Test
    public void testParseStopCommand() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"stop", "activity1"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertEquals(1, commands.size());
        assertEquals(EBCLIOptions.CmdType.stop, commands.get(0).getCmdType());
        assertEquals("activity1", commands.get(0).getCmdSpec());
    }

    @Test
    public void testParseWaitMillisCommand() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"waitmillis", "1000"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertEquals(1, commands.size());
        assertEquals(EBCLIOptions.CmdType.waitmillis, commands.get(0).getCmdType());
        assertEquals("1000", commands.get(0).getCmdSpec());
    }

    @Test
    public void testParseMetricsCommand() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--list-metrics", "activity1"});
        assertEquals("activity1", options.wantsMetricsForActivity());
    }

    @Test
    public void testParseLogHistograms() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--log-histograms", "file.log"});
        List<EBCLIOptions.LoggerConfig> configs = options.getHistoLoggerConfigs();
        assertEquals(1, configs.size());
        assertEquals("file.log", configs.get(0).getFilename());
    }

    @Test
    public void testParseLogStats() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--log-histostats", "stats.log"});
        List<EBCLIOptions.LoggerConfig> configs = options.getStatsLoggerConfigs();
        assertEquals(1, configs.size());
        assertEquals("stats.log", configs.get(0).getFilename());
    }

    @Test
    public void testParseClassicHistograms() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--classic-histograms", "classic.log"});
        List<EBCLIOptions.LoggerConfig> configs = options.getClassicHistoConfigs();
        assertEquals(1, configs.size());
        assertEquals("classic.log", configs.get(0).getFilename());
    }

    @Test
    public void testParseDumpCyclelog() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--export-cycle-log", "option1", "option2"});
        assertTrue(options.wantsToDumpCyclelog());
        assertEquals(2, options.getCycleLogExporterOptions().length);
    }

    @Test
    public void testParseImportCycleLog() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--import-cycle-log", "input1", "input2"});
        assertTrue(options.wantsToImportCycleLog());
        assertEquals(2, options.getCyclelogImportOptions().length);
    }

    @Test
    public void testParseAdvancedHelp() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--advanced-help"});
        assertTrue(options.wantsAdvancedHelp());
    }

    @Test
    public void testParseUnknownOption() {
        try {
            new EBCLIOptions(new String[]{"--unknown-option"});
            fail("Should have thrown InvalidParameterException");
        } catch (InvalidParameterException e) {
            // Expected
        }
    }

    @Test
    public void testParseReservedWordAsActivityName() {
        try {
            new EBCLIOptions(new String[]{"run", "activity"});
            fail("Should have thrown InvalidParameterException");
        } catch (InvalidParameterException e) {
            // Expected
        }
    }

    @Test
    public void testParseActivityNameWithEquals() {
        try {
            new EBCLIOptions(new String[]{"run", "activity=invalid"});
            fail("Should have thrown InvalidParameterException");
        } catch (InvalidParameterException e) {
            // Expected
        }
    }
}
