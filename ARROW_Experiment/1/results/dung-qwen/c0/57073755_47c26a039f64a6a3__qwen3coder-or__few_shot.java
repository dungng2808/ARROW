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
    public void testParseHelp() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--help"});
        assertTrue(options.wantsBasicHelp());
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
        EBCLIOptions options = new EBCLIOptions(new String[]{"--report-interval", "20"});
        assertEquals(20, options.getReportInterval());
    }

    @Test
    public void testParseMetricsPrefix() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--metrics-prefix", "custom."});
        assertEquals("custom.", options.wantsMetricsPrefix());
    }

    @Test
    public void testParseProgressIndicator() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--progress", "console:2m"});
        assertNotNull(options.getProgressSpec());
    }

    @Test
    public void testParseConsoleLogLevelInfo() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"-v"});
        assertEquals(Level.INFO, options.wantsConsoleLogLevel());
    }

    @Test
    public void testParseConsoleLogLevelDebug() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"-vv"});
        assertEquals(Level.DEBUG, options.wantsConsoleLogLevel());
    }

    @Test
    public void testParseConsoleLogLevelTrace() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"-vvv"});
        assertEquals(Level.TRACE, options.wantsConsoleLogLevel());
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
    public void testParseShowScript() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--show-script"});
        assertTrue(options.wantsShowScript());
    }

    @Test
    public void testParseAdvancedHelp() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--advanced-help"});
        assertTrue(options.wantsAdvancedHelp());
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
    public void testParseLogLevelOverride() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--log-level-override", "root:DEBUG"});
        Map<String, Level> overrides = options.getLogLevelOverrides();
        assertTrue(overrides.containsKey("root"));
    }

    @Test
    public void testParseWithLoggingPattern() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--with-logging-pattern", "%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"});
        assertEquals("%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n", options.getConsoleLoggingPattern());
    }

    @Test
    public void testParseScript() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"script", "test.js"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertFalse(commands.isEmpty());
        assertEquals(EBCLIOptions.CmdType.script, commands.get(0).getCmdType());
    }

    @Test
    public void testParseActivityRun() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"run", "test-activity"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertFalse(commands.isEmpty());
        assertEquals(EBCLIOptions.CmdType.run, commands.get(0).getCmdType());
    }

    @Test
    public void testParseActivityStart() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"start", "test-activity"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertFalse(commands.isEmpty());
        assertEquals(EBCLIOptions.CmdType.start, commands.get(0).getCmdType());
    }

    @Test
    public void testParseMetrics() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--list-metrics", "test-activity"});
        assertEquals("test-activity", options.wantsMetricsForActivity());
    }

    @Test
    public void testParseAwaitActivity() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"await", "test-activity"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertFalse(commands.isEmpty());
        assertEquals(EBCLIOptions.CmdType.await, commands.get(0).getCmdType());
    }

    @Test
    public void testParseStopActivity() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"stop", "test-activity"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertFalse(commands.isEmpty());
        assertEquals(EBCLIOptions.CmdType.stop, commands.get(0).getCmdType());
    }

    @Test
    public void testParseWaitMillis() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"waitmillis", "1000"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertFalse(commands.isEmpty());
        assertEquals(EBCLIOptions.CmdType.waitmillis, commands.get(0).getCmdType());
    }

    @Test
    public void testParseFragment() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"fragment", "some_fragment"});
        List<EBCLIOptions.Cmd> commands = options.getCommands();
        assertFalse(commands.isEmpty());
        assertEquals(EBCLIOptions.CmdType.fragment, commands.get(0).getCmdType());
    }

    @Test
    public void testParseDumpCyclelog() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--export-cycle-log", "option1", "option2"});
        assertTrue(options.wantsToDumpCyclelog());
        assertEquals(2, options.getCycleLogExporterOptions().length);
    }

    @Test
    public void testParseImportCycleLog() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--import-cycle-log", "option1", "option2"});
        assertTrue(options.wantsToImportCycleLog());
        assertEquals(2, options.getCyclelogImportOptions().length);
    }

    @Test
    public void testParseLogHistograms() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--log-histograms", "file.log"});
        List<EBCLIOptions.LoggerConfig> configs = options.getHistoLoggerConfigs();
        assertFalse(configs.isEmpty());
    }

    @Test
    public void testParseLogStats() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--log-histostats", "file.log"});
        List<EBCLIOptions.LoggerConfig> configs = options.getStatsLoggerConfigs();
        assertFalse(configs.isEmpty());
    }

    @Test
    public void testParseClassicHistograms() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--classic-histograms", "file.log"});
        List<EBCLIOptions.LoggerConfig> configs = options.getClassicHistoConfigs();
        assertFalse(configs.isEmpty());
    }

    @Test
    public void testParseTopicalHelp() {
        EBCLIOptions options = new EBCLIOptions(new String[]{"--help", "topic"});
        assertTrue(options.wantsTopicalHelp());
        assertEquals("topic", options.wantsTopicalHelpFor());
    }

    @Test
    public void testParseReservedWord() {
        try {
            new EBCLIOptions(new String[]{"activity", "test"});
            fail("Should have thrown InvalidParameterException");
        } catch (InvalidParameterException e) {
            // Expected
        }
    }

    @Test
    public void testParseUnrecognizedOption() {
        try {
            new EBCLIOptions(new String[]{"--unrecognized-option"});
            fail("Should have thrown InvalidParameterException");
        } catch (InvalidParameterException e) {
            // Expected
        }
    }
}
