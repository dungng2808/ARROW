package com.homeadvisor.robusto;

import com.homeadvisor.robusto.cache.CommandCache;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
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

        assertEquals("ApiCommand", command.getCommandKey().name());
    }

    @Test
    public void testBuilderWithCustomCommandGroup() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withCommandGroup("CustomGroup")
                .build();

        assertEquals("CustomGroup", command.getCommandKey().name());
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
        Object cacheKey = "test-key";

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withCommandCache(cache, cacheKey)
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
    public void testShouldUseCacheWhenCacheKeyIsNull() {
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
    public void testShouldUseCacheWhenCacheDisabled() {
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
        RuntimeException rootCause = new RuntimeException("Root cause");
        Exception wrapper = new Exception("Wrapper", rootCause);

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .build();

        String result = command.unwrapException(wrapper);
        assertEquals("Root cause", result);
    }

    @Test
    public void testUnwrapExceptionWithNoCause() {
        Exception ex = new Exception("Simple exception");

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .build();

        String result = command.unwrapException(ex);
        assertEquals("Simple exception", result);
    }

    @Test
    public void testBuilderWithZeroRetries() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withNumberOfRetries(0)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithNegativeRetries() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withNumberOfRetries(-1)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithCustomRetryPolicy() {
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<>();
        exceptionMap.put(RuntimeException.class, false);

        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withExceptionRetryPolicy(RuntimeException.class, false)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testGetCommandKey() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withCommandGroup("TestGroup")
                .build();

        HystrixCommandKey key = command.getCommandKey();
        assertEquals("TestGroup", key.name());
    }

    @Test
    public void testGetCommandGroup() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withCommandGroup("TestGroup")
                .build();

        HystrixCommandGroupKey group = command.getCommandGroup();
        assertEquals("TestGroup", group.name());
    }

    @Test
    public void testGetThreadPoolKey() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(mock(UriProvider.class))
                .withRemoteServiceCallback(mock(RemoteServiceCallback.class))
                .withCommandGroup("TestGroup")
                .build();

        HystrixThreadPoolKey threadPoolKey = command.getThreadPoolKey();
        assertEquals("TestGroup", threadPoolKey.name());
    }
}
