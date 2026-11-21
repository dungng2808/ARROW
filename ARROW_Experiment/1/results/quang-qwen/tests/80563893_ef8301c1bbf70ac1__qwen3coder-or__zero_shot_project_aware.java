package org.mortbay.jetty.load.generator.starter;

import com.beust.jcommander.JCommander;
import org.junit.Test;
import org.mortbay.jetty.load.generator.LoadGenerator;
import org.mortbay.jetty.load.generator.listeners.report.GlobalSummaryListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LoadGeneratorStarter_ArrowTest {

    @Test
    public void testParseReturnsNullWhenHelpIsTrue() {
        LoadGeneratorStarterArgs args = new LoadGeneratorStarterArgs();
        args.setHelp(true);
        
        LoadGeneratorStarterArgs result = LoadGeneratorStarter.parse(new String[]{"-h"}, () -> args);
        assertNull(result);
    }

    @Test
    public void testParseReturnsArgsWhenHelpIsFalse() {
        LoadGeneratorStarterArgs args = new LoadGeneratorStarterArgs();
        args.setHelp(false);
        args.setThreads(10);
        
        LoadGeneratorStarterArgs result = LoadGeneratorStarter.parse(new String[]{"-t", "10"}, () -> args);
        assertNotNull(result);
        assertEquals(10, result.getThreads());
    }

    @Test
    public void testPrepareCreatesBuilderWithCorrectValues() {
        LoadGeneratorStarterArgs args = new LoadGeneratorStarterArgs();
        args.setThreads(5);
        args.setWarmupIterations(10);
        args.setIterations(20);
        args.setRunningTime(30L);
        args.setRunningTimeUnit(TimeUnit.SECONDS);
        args.setUsersPerThread(2);
        args.setChannelsPerUser(3);
        args.setResourceRate(100L);
        args.setRateRampUpPeriod(5L);
        args.setScheme("http");
        args.setHost("localhost");
        args.setPort(8080);
        args.setMaxRequestsQueued(1000);
        args.setConnectBlocking(true);
        args.setConnectTimeout(5000L);
        args.setIdleTimeout(10000L);
        
        LoadGenerator.Builder builder = LoadGeneratorStarter.prepare(args);
        assertNotNull(builder);
    }

    @Test
    public void testRunWithBuilderCallsBuildAndBegin() {
        LoadGenerator.Builder mockBuilder = mock(LoadGenerator.Builder.class);
        LoadGenerator mockLoadGenerator = mock(LoadGenerator.class);
        CompletableFuture<Void> mockFuture = mock(CompletableFuture.class);
        
        when(mockBuilder.build()).thenReturn(mockLoadGenerator);
        when(mockLoadGenerator.begin()).thenReturn(mockFuture);
        
        LoadGeneratorStarter.run(mockBuilder);
        
        verify(mockBuilder).build();
        verify(mockLoadGenerator).begin();
    }

    @Test
    public void testRunWithLoadGeneratorLogsInfoAndCompletes() {
        LoadGenerator mockLoadGenerator = mock(LoadGenerator.class);
        CompletableFuture<Void> mockFuture = mock(CompletableFuture.class);
        
        when(mockLoadGenerator.begin()).thenReturn(mockFuture);
        
        LoadGeneratorStarter.run(mockLoadGenerator);
        
        verify(mockLoadGenerator).begin();
    }

    @Test
    public void testNanosToMillisConvertsCorrectly() {
        long nanos = 1000000000L; // 1 second in nanoseconds
        long expected = 1000L; // 1 second in milliseconds
        
        long result = LoadGeneratorStarter.nanosToMillis(nanos);
        assertEquals(expected, result);
    }
}
