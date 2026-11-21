package org.mortbay.jetty.load.generator.starter;

import com.beust.jcommander.JCommander;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LoadGeneratorStarter_ArrowTest {

    @Before
    public void setUp() {
        // Reset logger to avoid interference between tests
        Log.setLog(null);
    }

    @Test
    public void testParseWithValidArgs() {
        String[] args = {"-t", "5", "-i", "10"};
        LoadGeneratorStarterArgs result = LoadGeneratorStarter.parse(args);
        assertNotNull(result);
        assertEquals(5, result.getThreads());
        assertEquals(10, result.getIterations());
    }

    @Test
    public void testParseWithHelpFlag() {
        String[] args = {"-h"};
        LoadGeneratorStarterArgs result = LoadGeneratorStarter.parse(args);
        assertNull(result);
    }

    @Test
    public void testParseWithUnknownOptions() {
        String[] args = {"-unknown", "value", "-t", "3"};
        LoadGeneratorStarterArgs result = LoadGeneratorStarter.parse(args);
        assertNotNull(result);
        assertEquals(3, result.getThreads());
    }

    @Test
    public void testPrepare() {
        LoadGeneratorStarterArgs args = new LoadGeneratorStarterArgs();
        args.setThreads(2);
        args.setIterations(5);
        args.setWarmupIterations(1);
        args.setRunningTime(10L);
        args.setRunningTimeUnit(TimeUnit.SECONDS);
        args.setUsersPerThread(3);
        args.setChannelsPerUser(4);
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
    public void testRunWithBuilder() {
        LoadGenerator.Builder builder = Mockito.mock(LoadGenerator.Builder.class);
        LoadGenerator loadGenerator = Mockito.mock(LoadGenerator.class);
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        
        when(builder.build()).thenReturn(loadGenerator);
        when(loadGenerator.begin()).thenReturn(future);
        
        LoadGeneratorStarter.run(builder);
        
        verify(builder).build();
        verify(loadGenerator).begin();
    }

    @Test
    public void testRunWithLoadGenerator() {
        LoadGenerator loadGenerator = Mockito.mock(LoadGenerator.class);
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        
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

    @Test
    public void testDisplayGlobalSummaryListener() {
        // This test mainly verifies that the method doesn't throw exceptions
        GlobalSummaryListener listener = Mockito.mock(GlobalSummaryListener.class);
        CollectorInformations collectorInfo = Mockito.mock(CollectorInformations.class);
        
        when(listener.getLatencyTimeHistogram()).thenReturn(Mockito.mock(org.HdrHistogram.Histogram.class));
        when(listener.getRequestCommitTotal()).thenReturn(100L);
        when(listener.getResponses1xx()).thenReturn(10L);
        when(listener.getResponses2xx()).thenReturn(50L);
        when(listener.getResponses3xx()).thenReturn(20L);
        when(listener.getResponses4xx()).thenReturn(15L);
        when(listener.getResponses5xx()).thenReturn(5L);
        
        // Mock the histogram interval histogram
        org.HdrHistogram.Histogram histogram = Mockito.mock(org.HdrHistogram.Histogram.class);
        when(listener.getLatencyTimeHistogram().getIntervalHistogram()).thenReturn(histogram);
        when(histogram.getTotalCount()).thenReturn(100L);
        when(histogram.getMaxValue()).thenReturn(1000000000L); // 1 second
        when(histogram.getMinValue()).thenReturn(1000000L); // 1 millisecond
        when(histogram.getMean()).thenReturn(500000000.0); // 0.5 seconds
        when(histogram.getValueAtPercentile(50.0)).thenReturn(500000000L); // 0.5 seconds
        when(histogram.getValueAtPercentile(90.0)).thenReturn(900000000L); // 0.9 seconds
        when(histogram.getStdDeviation()).thenReturn(200000000.0); // 0.2 seconds
        when(histogram.getStartTimeStamp()).thenReturn(1000L);
        when(histogram.getEndTimeStamp()).thenReturn(2000L);
        
        // Mock the CollectorInformations constructor
        try (MockedStatic<CollectorInformations> mockedCollector = Mockito.mockStatic(CollectorInformations.class)) {
            mockedCollector.when(() -> new CollectorInformations(any(org.HdrHistogram.Histogram.class)))
                    .thenReturn(collectorInfo);
            
            // Mock SimpleDateFormat
            try (MockedStatic<SimpleDateFormat> mockedFormat = Mockito.mockStatic(SimpleDateFormat.class)) {
                SimpleDateFormat format = Mockito.mock(SimpleDateFormat.class);
                mockedFormat.when(() -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z"))
                        .thenReturn(format);
                when(format.format(any(Long.class))).thenReturn("test");
                
                // Mock logger to avoid output during test
                Logger logger = Mockito.mock(Logger.class);
                try (MockedStatic<Log> mockedLog = Mockito.mockStatic(Log.class)) {
                    mockedLog.when(() -> Log.getLogger(LoadGeneratorStarter.class))
                            .thenReturn(logger);
                    
                    LoadGeneratorStarter.displayGlobalSummaryListener(listener);
                }
            }
        }
    }
}
