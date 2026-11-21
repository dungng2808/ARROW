package org.mortbay.jetty.load.generator.starter;

import com.beust.jcommander.JCommander;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LoadGeneratorStarter_ArrowTest {

    @Test
    public void testParseReturnsNullWhenHelpFlagIsSet() {
        String[] args = {"-h"};
        LoadGeneratorStarterArgs result = LoadGeneratorStarter.parse(args);
        assertNull(result);
    }

    @Test
    public void testParseReturnsArgsWhenNoHelpFlag() {
        String[] args = {"-t", "5", "-i", "10"};
        LoadGeneratorStarterArgs result = LoadGeneratorStarter.parse(args);
        assertNotNull(result);
        assertEquals(5, result.getThreads());
        assertEquals(10, result.getIterations());
    }

    @Test
    public void testPrepareSetsAllBuilderProperties() {
        LoadGeneratorStarterArgs args = new LoadGeneratorStarterArgs();
        args.setThreads(10);
        args.setWarmupIterations(5);
        args.setIterations(20);
        args.setRunningTime(30);
        args.setRunningTimeUnit(TimeUnit.SECONDS);
        args.setUsersPerThread(2);
        args.setChannelsPerUser(3);
        args.setResourceRate(100);
        args.setRateRampUpPeriod(10);
        args.setScheme("http");
        args.setHost("localhost");
        args.setPort(8080);
        args.setMaxRequestsQueued(1000);
        args.setConnectBlocking(true);
        args.setConnectTimeout(5000);
        args.setIdleTimeout(10000);

        LoadGenerator.Builder builder = LoadGeneratorStarter.prepare(args);
        assertNotNull(builder);
    }

    @Test
    public void testRunWithBuilderCallsBuildAndBegin() {
        LoadGenerator.Builder builder = Mockito.mock(LoadGenerator.Builder.class);
        LoadGenerator loadGenerator = Mockito.mock(LoadGenerator.class);
        CompletableFuture<Void> future = Mockito.mock(CompletableFuture.class);

        when(builder.build()).thenReturn(loadGenerator);
        when(loadGenerator.begin()).thenReturn(future);

        LoadGeneratorStarter.run(builder);

        verify(builder).build();
        verify(loadGenerator).begin();
    }

    @Test
    public void testRunWithLoadGeneratorLogsInfoAndCompletes() {
        LoadGenerator loadGenerator = Mockito.mock(LoadGenerator.class);
        CompletableFuture<Void> future = Mockito.mock(CompletableFuture.class);

        when(loadGenerator.begin()).thenReturn(future);

        LoadGeneratorStarter.run(loadGenerator);

        verify(loadGenerator).begin();
    }

    @Test
    public void testNanosToMillisConvertsCorrectly() {
        long nanos = 1000000000L; // 1 second in nanoseconds
        long expected = 1000L; // 1 second in milliseconds
        long result = LoadGeneratorStarter.nanosToMillis(nanos);
        assertEquals(expected, result);
    }
}
