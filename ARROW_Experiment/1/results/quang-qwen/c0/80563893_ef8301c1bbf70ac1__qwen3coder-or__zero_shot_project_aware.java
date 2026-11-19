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
        String[] args = {"-h"};
        LoadGeneratorStarterArgs result = LoadGeneratorStarter.parse(args);
        assertNull(result);
    }

    @Test
    public void testParseParsesArgumentsSuccessfully() {
        String[] args = {"-t", "5", "-i", "100"};
        LoadGeneratorStarterArgs result = LoadGeneratorStarter.parse(args);
        assertNotNull(result);
        assertEquals(5, result.getThreads());
        assertEquals(100, result.getIterations());
    }

    @Test
    public void testPrepareBuildsCorrectBuilder() {
        LoadGeneratorStarterArgs args = new LoadGeneratorStarterArgs();
        args.setThreads(3);
        args.setWarmupIterations(10);
        args.setIterations(50);
        args.setRunningTime(2);
        args.setRunningTimeUnit(TimeUnit.MINUTES);
        args.setUsersPerThread(2);
        args.setChannelsPerUser(1);
        args.setResourceRate(100);
        args.setRateRampUpPeriod(5);
        args.setScheme("http");
        args.setHost("localhost");
        args.setPort(8080);
        args.setMaxRequestsQueued(1000);
        args.setConnectBlocking(false);
        args.setConnectTimeout(5000);
        args.setIdleTimeout(30000);

        LoadGenerator.Builder builder = LoadGeneratorStarter.prepare(args);
        assertNotNull(builder);
    }

    @Test
    public void testRunWithBuilder() {
        LoadGenerator.Builder builder = mock(LoadGenerator.Builder.class);
        LoadGenerator loadGenerator = mock(LoadGenerator.class);
        CompletableFuture<Void> future = mock(CompletableFuture.class);

        when(builder.build()).thenReturn(loadGenerator);
        when(loadGenerator.begin()).thenReturn(future);

        LoadGeneratorStarter.run(builder);

        verify(builder).build();
        verify(loadGenerator).begin();
    }

    @Test
    public void testRunWithLoadGenerator() {
        LoadGenerator loadGenerator = mock(LoadGenerator.class);
        CompletableFuture<Void> future = mock(CompletableFuture.class);

        when(loadGenerator.begin()).thenReturn(future);

        LoadGeneratorStarter.run(loadGenerator);

        verify(loadGenerator).begin();
    }

    @Test
    public void testNanosToMillis() {
        long nanos = 1000000000L; // 1 second in nanoseconds
        long expected = 1000L; // 1 second in milliseconds
        long result = LoadGeneratorStarter.nanosToMillis(nanos);
        assertEquals(expected, result);
    }
}
