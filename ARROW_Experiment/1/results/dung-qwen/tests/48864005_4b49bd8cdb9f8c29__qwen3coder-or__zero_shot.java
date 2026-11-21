package com.homeadvisor.robusto;

import com.homeadvisor.robusto.cache.CommandCache;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.junit.Test;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ApiCommand_ArrowTest {

    @Test
    public void testBuilderWithRequiredFields() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .build();

        assertNotNull(command);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullUriProvider() {
        ApiCommand.<String>builder()
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullRemoteServiceCallback() {
        ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .build();
    }

    @Test
    public void testBuilderWithDefaultCommandGroup() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .build();

        assertEquals("ApiCommand", command.getClass().getMethod("getCommandKey").invoke(command).toString());
    }

    @Test
    public void testBuilderWithCustomCommandGroup() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withCommandGroup("CustomGroup")
                .build();

        assertEquals("CustomGroup", command.getClass().getMethod("getCommandKey").invoke(command).toString());
    }

    @Test
    public void testBuilderWithCustomHystrixProperties() {
        HystrixCommandProperties.Setter props = HystrixCommandProperties.Setter()
                .withExecutionTimeoutInMilliseconds(5000)
                .withCircuitBreakerEnabled(false);

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withHystrixCommandProperties(props)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithCustomThreadPoolProperties() {
        HystrixThreadPoolProperties.Setter props = HystrixThreadPoolProperties.Setter()
                .withCoreSize(10);

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withHystrixThreadProperties(props)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithRetrySettings() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withNumberOfRetries(5)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithBackoffPolicy() {
        BackOffPolicy backoffPolicy = mock(BackOffPolicy.class);

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withBackoffPolicy(backoffPolicy)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithRetryListener() {
        RetryListener listener = mock(RetryListener.class);

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withRetryListener(listener)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithCommandCache() {
        CommandCache cache = mock(CommandCache.class);

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withCommandCache(cache, "test-key")
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithExceptionRetryPolicy() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withExceptionRetryPolicy(RuntimeException.class, false)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithDeprecatedTimeoutSetting() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withCommandTimeoutInMilliseconds(10000)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithDeprecatedThreadSetting() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withNumberOfThreads(20)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithDeprecatedRollingWindowSetting() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withRollingWindowMetricLengthMs(20000)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithDeprecatedCircuitBreakerSetting() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withCircuitBreakerEnabled(false)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testShouldUseCacheWhenCacheIsNull() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .build();

        assertFalse(command.shouldUseCache());
    }

    @Test
    public void testShouldUseCacheWhenCacheIsNotNullButKeyIsNull() {
        CommandCache cache = mock(CommandCache.class);
        when(cache.getConfig()).thenReturn(mock(CommandCache.Config.class));
        when(cache.getConfig().isEnabled()).thenReturn(true);

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withCommandCache(cache, null)
                .build();

        assertFalse(command.shouldUseCache());
    }

    @Test
    public void testShouldUseCacheWhenCacheAndKeyAreNotNullButDisabled() {
        CommandCache cache = mock(CommandCache.class);
        when(cache.getConfig()).thenReturn(mock(CommandCache.Config.class));
        when(cache.getConfig().isEnabled()).thenReturn(false);

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withCommandCache(cache, "key")
                .build();

        assertFalse(command.shouldUseCache());
    }

    @Test
    public void testShouldUseCacheWhenAllConditionsMet() {
        CommandCache cache = mock(CommandCache.class);
        when(cache.getConfig()).thenReturn(mock(CommandCache.Config.class));
        when(cache.getConfig().isEnabled()).thenReturn(true);

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withCommandCache(cache, "key")
                .build();

        assertTrue(command.shouldUseCache());
    }

    @Test
    public void testUnwrapException() {
        Exception ex = new Exception("Test message");
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .build();

        try {
            command.getClass().getMethod("unwrapException", Throwable.class).invoke(command, ex);
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testRunWithNoCache() throws Exception {
        UriProvider<String> uriProvider = mock(UriProvider.class);
        RemoteServiceCallback<String> callback = mock(RemoteServiceCallback.class);
        when(callback.run()).thenReturn("result");

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(uriProvider)
                .withRemoteServiceCallback(callback)
                .build();

        String result = command.run();
        assertEquals("result", result);
    }

    @Test
    public void testRunWithCacheHit() throws Exception {
        CommandCache<String, String, String> cache = mock(CommandCache.class);
        when(cache.getConfig()).thenReturn(mock(CommandCache.Config.class));
        when(cache.getConfig().isEnabled()).thenReturn(true);
        when(cache.getCache("key")).thenReturn(Optional.of("cached-result"));

        UriProvider<String> uriProvider = mock(UriProvider.class);
        RemoteServiceCallback<String> callback = mock(RemoteServiceCallback.class);

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(uriProvider)
                .withRemoteServiceCallback(callback)
                .withCommandCache(cache, "key")
                .build();

        String result = command.run();
        assertEquals("cached-result", result);
    }

    @Test
    public void testRunWithCacheMiss() throws Exception {
        CommandCache<String, String, String> cache = mock(CommandCache.class);
        when(cache.getConfig()).thenReturn(mock(CommandCache.Config.class));
        when(cache.getConfig().isEnabled()).thenReturn(true);
        when(cache.getCache("key")).thenReturn(Optional.empty());
        when(cache.putCache("key", "remote-result")).thenReturn(null);

        UriProvider<String> uriProvider = mock(UriProvider.class);
        RemoteServiceCallback<String> callback = mock(RemoteServiceCallback.class);
        when(uriProvider.execute(callback)).thenReturn("remote-result");

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(uriProvider)
                .withRemoteServiceCallback(callback)
                .withCommandCache(cache, "key")
                .build();

        String result = command.run();
        assertEquals("remote-result", result);
    }

    @Test
    public void testRunWithCachePutFailure() throws Exception {
        CommandCache<String, String, String> cache = mock(CommandCache.class);
        when(cache.getConfig()).thenReturn(mock(CommandCache.Config.class));
        when(cache.getConfig().isEnabled()).thenReturn(true);
        when(cache.getCache("key")).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Cache put failed")).when(cache).putCache("key", "remote-result");

        UriProvider<String> uriProvider = mock(UriProvider.class);
        RemoteServiceCallback<String> callback = mock(RemoteServiceCallback.class);
        when(uriProvider.execute(callback)).thenReturn("remote-result");

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(uriProvider)
                .withRemoteServiceCallback(callback)
                .withCommandCache(cache, "key")
                .build();

        String result = command.run();
        assertEquals("remote-result", result);
    }

    @Test
    public void testApiCommandLoggerOpen() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .build();

        ApiCommand.ApiCommandLogger logger = command.new ApiCommandLogger(command);
        assertTrue(logger.open(mock(RetryContext.class), mock(RetryCallback.class)));
    }

    @Test
    public void testApiCommandLoggerClose() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .build();

        ApiCommand.ApiCommandLogger logger = command.new ApiCommandLogger(command);
        logger.close(mock(RetryContext.class), mock(RetryCallback.class), null);
    }

    @Test
    public void testApiCommandLoggerOnError() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .build();

        ApiCommand.ApiCommandLogger logger = command.new ApiCommandLogger(command);
        logger.onError(mock(RetryContext.class), mock(RetryCallback.class), new RuntimeException("Test"));
    }
}
