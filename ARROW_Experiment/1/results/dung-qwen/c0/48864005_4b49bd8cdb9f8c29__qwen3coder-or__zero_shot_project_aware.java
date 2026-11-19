package com.homeadvisor.robusto;

import com.homeadvisor.robusto.cache.CommandCache;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.retry.RetryTemplate;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class ApiCommand_ArrowTest {

    private UriProvider<String> mockUriProvider;
    private RemoteServiceCallback<String> mockRemoteServiceCallback;
    private CommandCache<String, String, String> mockCommandCache;
    private ApiCommand.Builder<String> builder;

    @Before
    public void setUp() {
        mockUriProvider = mock(UriProvider.class);
        mockRemoteServiceCallback = mock(RemoteServiceCallback.class);
        mockCommandCache = mock(CommandCache.class);
        builder = ApiCommand.builder();
    }

    @Test
    public void testBuilderWithRequiredFields() {
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .build();

        assertNotNull(command);
        assertEquals(mockUriProvider, command.uriProvider);
        assertEquals(mockRemoteServiceCallback, command.remoteServiceCallback);
        assertNotNull(command.retryTemplate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullUriProvider() {
        builder
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullRemoteServiceCallback() {
        builder
                .withUriProvider(mockUriProvider)
                .build();
    }

    @Test
    public void testBuilderWithNegativeRetries() {
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withNumberOfRetries(-1)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithZeroRetries() {
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withNumberOfRetries(0)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithCommandGroup() {
        String groupName = "TestGroup";
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withCommandGroup(groupName)
                .build();

        assertNotNull(command);
        assertEquals(groupName, command.getCommandKey().name());
    }

    @Test
    public void testBuilderWithHystrixProperties() {
        HystrixCommandProperties.Setter props = HystrixCommandProperties.Setter()
                .withExecutionTimeoutInMilliseconds(5000)
                .withCircuitBreakerEnabled(false);

        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withHystrixCommandProperties(props)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithHystrixThreadProperties() {
        HystrixThreadPoolProperties.Setter threadProps = HystrixThreadPoolProperties.Setter()
                .withCoreSize(10);

        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withHystrixThreadProperties(threadProps)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithBackoffPolicy() {
        BackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withBackoffPolicy(backOffPolicy)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithCommandCache() {
        Object cacheKey = "testKey";
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withCommandCache(mockCommandCache, cacheKey)
                .build();

        assertNotNull(command);
        assertEquals(mockCommandCache, command.commandCache);
        assertEquals(cacheKey, command.cacheKey);
    }

    @Test
    public void testBuilderWithExceptionRetryPolicy() {
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withExceptionRetryPolicy(RuntimeException.class, false)
                .build();

        assertNotNull(command);
    }

    @Test
    public void testShouldUseCacheWhenCacheIsNull() {
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .build();

        assertFalse(command.shouldUseCache());
    }

    @Test
    public void testShouldUseCacheWhenCacheKeyIsNull() {
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withCommandCache(mockCommandCache, null)
                .build();

        assertFalse(command.shouldUseCache());
    }

    @Test
    public void testShouldUseCacheWhenCacheIsDisabled() {
        when(mockCommandCache.getConfig()).thenReturn(new CommandCache.Config() {
            @Override
            public boolean isEnabled() {
                return false;
            }
        });

        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withCommandCache(mockCommandCache, "testKey")
                .build();

        assertFalse(command.shouldUseCache());
    }

    @Test
    public void testShouldUseCacheWhenAllConditionsMet() {
        when(mockCommandCache.getConfig()).thenReturn(new CommandCache.Config() {
            @Override
            public boolean isEnabled() {
                return true;
            }
        });

        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withCommandCache(mockCommandCache, "testKey")
                .build();

        assertTrue(command.shouldUseCache());
    }

    @Test
    public void testUnwrapException() {
        RuntimeException rootCause = new RuntimeException("Root cause");
        Exception wrapper = new Exception("Wrapper", rootCause);

        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .build();

        String result = command.unwrapException(wrapper);
        assertEquals("Root cause", result);
    }

    @Test
    public void testRunWithoutCache() throws Exception {
        String expectedResult = "testResult";
        when(mockRemoteServiceCallback.run()).thenReturn(expectedResult);

        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .build();

        String result = command.run();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testRunWithCacheHit() throws Exception {
        String expectedResult = "cachedResult";
        when(mockCommandCache.getCache(anyObject())).thenReturn(Optional.of(expectedResult));

        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withCommandCache(mockCommandCache, "testKey")
                .build();

        when(mockCommandCache.getConfig()).thenReturn(new CommandCache.Config() {
            @Override
            public boolean isEnabled() {
                return true;
            }
        });

        String result = command.run();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testRunWithCacheMiss() throws Exception {
        String expectedResult = "remoteResult";
        when(mockCommandCache.getCache(anyObject())).thenReturn(Optional.empty());
        when(mockRemoteServiceCallback.run()).thenReturn(expectedResult);

        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withCommandCache(mockCommandCache, "testKey")
                .build();

        when(mockCommandCache.getConfig()).thenReturn(new CommandCache.Config() {
            @Override
            public boolean isEnabled() {
                return true;
            }
        });

        String result = command.run();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testRunWithCachePutFailure() throws Exception {
        String expectedResult = "remoteResult";
        when(mockCommandCache.getCache(anyObject())).thenReturn(Optional.empty());
        when(mockRemoteServiceCallback.run()).thenReturn(expectedResult);
        doThrow(new RuntimeException("Cache put failed")).when(mockCommandCache).putCache(anyObject(), anyObject());

        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withCommandCache(mockCommandCache, "testKey")
                .build();

        when(mockCommandCache.getConfig()).thenReturn(new CommandCache.Config() {
            @Override
            public boolean isEnabled() {
                return true;
            }
        });

        String result = command.run();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testBuilderWithDefaultRetrySettings() {
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .build();

        RetryTemplate retryTemplate = command.retryTemplate;
        assertNotNull(retryTemplate);

        // Check that retry policy is set correctly
        SimpleRetryPolicy retryPolicy = (SimpleRetryPolicy) retryTemplate.getRetryPolicy();
        assertNotNull(retryPolicy);
        assertEquals(3, retryPolicy.getMaxAttempts());
    }

    @Test
    public void testBuilderWithCustomRetrySettings() {
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withNumberOfRetries(5)
                .build();

        RetryTemplate retryTemplate = command.retryTemplate;
        assertNotNull(retryTemplate);

        SimpleRetryPolicy retryPolicy = (SimpleRetryPolicy) retryTemplate.getRetryPolicy();
        assertNotNull(retryPolicy);
        assertEquals(5, retryPolicy.getMaxAttempts());
    }

    @Test
    public void testBuilderWithRetryListener() {
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withRetryListener(new org.springframework.retry.RetryListener() {
                    @Override
                    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                        return true;
                    }

                    @Override
                    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                    }

                    @Override
                    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                    }
                })
                .build();

        assertNotNull(command);
    }

    @Test
    public void testBuilderWithDeprecatedMethods() {
        ApiCommand<String> command = builder
                .withUriProvider(mockUriProvider)
                .withRemoteServiceCallback(mockRemoteServiceCallback)
                .withCommandTimeoutInMilliseconds(10000)
                .withNumberOfThreads(5)
                .withRollingWindowMetricLengthMs(15000)
                .withCircuitBreakerSleepWindow(3000)
                .withCircuitBreakerEnabled(false)
                .build();

        assertNotNull(command);
    }
}
