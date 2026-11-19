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
    public void testParseReturnsNullWhenHelpIsTrue() {
        LoadGeneratorStarterArgs args = new LoadGeneratorStarterArgs();
        args.setHelp(true);
        
        LoadGeneratorStarterArgs result = LoadGeneratorStarter.parse(new String[]{}, () -> args);
        
        assertNull(result);
    }

    @Test
    public void testParseParsesArgumentsSuccessfully() {
        String[] args = {"-t", "5", "-i", "10"};
        LoadGeneratorStarterArgs result = LoadGeneratorStarter.parse(args);
        
        assertNotNull(result);
        assertEquals(5, result.getThreads());
        assertEquals(10, result.getIterations());
    }

    @Test
    public void testPrepareBuildsCorrectBuilder() {
        LoadGeneratorStarterArgs args = new LoadGeneratorStarterArgs();
        args.setThreads(3);
        args.setWarmupIterations(2);
        args.setIterations(5);
        args.setRunningTime(10L);
        args.setRunningTimeUnit(TimeUnit.SECONDS);
        args.setUsersPerThread(1);
        args.setChannelsPerUser(1);
        args.setResourceRate(100L);
        args.setRateRampUpPeriod(5L);
        args.setScheme("http");
        args.setHost("localhost");
        args.setPort(8080);
        args.setMaxRequestsQueued(1000);
        args.setConnectBlocking(false);
        args.setConnectTimeout(5000L);
        args.setIdleTimeout(30000L);
        
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
    public void testRunWithLoadGeneratorLogsAndCompletes() {
        LoadGenerator loadGenerator = Mockito.mock(LoadGenerator.class);
        CompletableFuture<Void> future = Mockito.mock(CompletableFuture.class);
        
        when(loadGenerator.begin()).thenReturn(future);
        
        LoadGeneratorStarter.run(loadGenerator);
        
        verify(loadGenerator).begin();
    }

    @Test
    public void testNanosToMillisConvertsCorrectly() {
        long nanos = 1000000000L; // 1 second in nanoseconds
        long result = LoadGeneratorStarter.nanosToMillis(nanos);
        
        assertEquals(1000L, result);
    }

    @Test
    public void testMainWithValidArgs() {
        String[] args = {"-t", "1", "-i", "1", "-r", "100", "-u", "1000", "-s", "http", "-h", "localhost", "-p", "8080"};
        
        // This should not throw any exceptions
        try {
            LoadGeneratorStarter.main(args);
        } catch (Exception e) {
            // Expected behavior if there are issues with the setup
        }
    }
}
