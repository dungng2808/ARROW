package software.amazon.awssdk.http.nio.netty.internal.http2;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.http.Protocol;
import software.amazon.awssdk.http.nio.netty.internal.NettyConfiguration;
import software.amazon.awssdk.http.nio.netty.internal.SdkChannelPool;
import software.amazon.awssdk.http.nio.netty.internal.utils.BetterFixedChannelPool;
import software.amazon.awssdk.metrics.MetricCollector;

public class HttpOrHttp2ChannelPool_ArrowTest {

    private ChannelPool delegatePool;
    private EventLoopGroup eventLoopGroup;
    private EventLoop eventLoop;
    private NettyConfiguration configuration;
    private HttpOrHttp2ChannelPool channelPool;

    @Before
    public void setup() {
        delegatePool = mock(ChannelPool.class);
        eventLoopGroup = mock(EventLoopGroup.class);
        eventLoop = mock(EventLoop.class);
        configuration = mock(NettyConfiguration.class);

        when(eventLoopGroup.next()).thenReturn(eventLoop);
        when(configuration.connectionAcquireTimeoutMillis()).thenReturn(1000L);
        when(configuration.maxPendingConnectionAcquires()).thenReturn(10);
        when(configuration.reapIdleConnections()).thenReturn(false);
        when(configuration.idleTimeoutMillis()).thenReturn(60000L);

        channelPool = new HttpOrHttp2ChannelPool(delegatePool, eventLoopGroup, 5, configuration);
    }

    @Test
    public void testAcquire_SuccessWithHttp1_1() throws Exception {
        Promise<Channel> promise = mock(Promise.class);
        Channel channel = mock(Channel.class);
        Future<Channel> channelFuture = mock(Future.class);
        Promise<ChannelPool> protocolPromise = mock(Promise.class);
        BetterFixedChannelPool fixedPool = mock(BetterFixedChannelPool.class);

        when(delegatePool.acquire()).thenReturn(channelFuture);
        when(channelFuture.getNow()).thenReturn(channel);
        when(channel.attr(any())).thenReturn(mock(io.netty.util.Attribute.class));
        when(channel.attr(any()).get()).thenReturn(mock(java.util.concurrent.CompletableFuture.class));

        // Mock the protocol future to return HTTP/1.1
        java.util.concurrent.CompletableFuture<Protocol> protocolFuture = new java.util.concurrent.CompletableFuture<>();
        protocolFuture.complete(Protocol.HTTP1_1);
        when(channel.attr(any()).get()).thenReturn(protocolFuture);

        when(eventLoop.newPromise()).thenReturn(protocolPromise);
        when(protocolPromise.addListener(any())).thenAnswer(invocation -> {
            ((io.netty.util.concurrent.GenericFutureListener) invocation.getArgument(0))
                .operationComplete(protocolPromise);
            return null;
        });

        channelPool.acquire(promise);

        verify(protocolPromise).addListener(any());
    }

    @Test
    public void testAcquire_SuccessWithHttp2() throws Exception {
        Promise<Channel> promise = mock(Promise.class);
        Channel channel = mock(Channel.class);
        Future<Channel> channelFuture = mock(Future.class);
        Promise<ChannelPool> protocolPromise = mock(Promise.class);
        BetterFixedChannelPool fixedPool = mock(BetterFixedChannelPool.class);

        when(delegatePool.acquire()).thenReturn(channelFuture);
        when(channelFuture.getNow()).thenReturn(channel);
        when(channel.attr(any())).thenReturn(mock(io.netty.util.Attribute.class));
        when(channel.attr(any()).get()).thenReturn(mock(java.util.concurrent.CompletableFuture.class));

        // Mock the protocol future to return HTTP/2
        java.util.concurrent.CompletableFuture<Protocol> protocolFuture = new java.util.concurrent.CompletableFuture<>();
        protocolFuture.complete(Protocol.HTTP_2);
        when(channel.attr(any()).get()).thenReturn(protocolFuture);

        when(eventLoop.newPromise()).thenReturn(protocolPromise);
        when(protocolPromise.addListener(any())).thenAnswer(invocation -> {
            ((io.netty.util.concurrent.GenericFutureListener) invocation.getArgument(0))
                .operationComplete(protocolPromise);
            return null;
        });

        channelPool.acquire(promise);

        verify(protocolPromise).addListener(any());
    }

    @Test
    public void testAcquire_ProtocolFailure() throws Exception {
        Promise<Channel> promise = mock(Promise.class);
        Future<Channel> channelFuture = mock(Future.class);
        Promise<ChannelPool> protocolPromise = mock(Promise.class);

        when(delegatePool.acquire()).thenReturn(channelFuture);
        when(channelFuture.isSuccess()).thenReturn(false);
        when(channelFuture.cause()).thenReturn(new RuntimeException("Protocol negotiation failed"));

        when(eventLoop.newPromise()).thenReturn(protocolPromise);
        when(protocolPromise.addListener(any())).thenAnswer(invocation -> {
            ((io.netty.util.concurrent.GenericFutureListener) invocation.getArgument(0))
                .operationComplete(protocolPromise);
            return null;
        });

        channelPool.acquire(promise);

        verify(promise).setFailure(any(Exception.class));
    }

    @Test
    public void testRelease_WhenProtocolNotInitialized() throws Exception {
        Channel channel = mock(Channel.class);
        Promise<Void> promise = mock(Promise.class);

        channelPool.release(channel, promise);

        verify(delegatePool).release(channel, promise);
    }

    @Test
    public void testRelease_WhenProtocolInitialized() throws Exception {
        Channel channel = mock(Channel.class);
        Promise<Void> promise = mock(Promise.class);
        BetterFixedChannelPool fixedPool = mock(BetterFixedChannelPool.class);

        // Initialize protocol
        when(eventLoop.newPromise()).thenReturn(mock(Promise.class));
        channelPool.getClass().getDeclaredMethod("initializeProtocol").invoke(channelPool);

        // Set up protocolImpl
        java.lang.reflect.Field field = channelPool.getClass().getDeclaredField("protocolImpl");
        field.setAccessible(true);
        field.set(channelPool, fixedPool);

        channelPool.release(channel, promise);

        verify(fixedPool).release(channel, promise);
    }

    @Test
    public void testClose_WhenClosed() throws Exception {
        channelPool.close();

        // Verify that close was called on the delegate pool
        verify(delegatePool).close();
    }

    @Test
    public void testCollectChannelPoolMetrics_Success() throws Exception {
        Promise<ChannelPool> protocolPromise = mock(Promise.class);
        BetterFixedChannelPool fixedPool = mock(BetterFixedChannelPool.class);
        CompletableFuture<Void> metricFuture = new CompletableFuture<>();

        when(eventLoop.newPromise()).thenReturn(protocolPromise);
        when(protocolPromise.addListener(any())).thenAnswer(invocation -> {
            ((io.netty.util.concurrent.GenericFutureListener) invocation.getArgument(0))
                .operationComplete(protocolPromise);
            return null;
        });
        when(protocolPromise.isSuccess()).thenReturn(true);
        when(protocolPromise.getNow()).thenReturn(fixedPool);
        when(fixedPool.collectChannelPoolMetrics(any(MetricCollector.class))).thenReturn(metricFuture);

        CompletableFuture<Void> result = channelPool.collectChannelPoolMetrics(mock(MetricCollector.class));

        verify(fixedPool).collectChannelPoolMetrics(any(MetricCollector.class));
        verify(protocolPromise).addListener(any());
    }

    @Test
    public void testCollectChannelPoolMetrics_Failure() throws Exception {
        Promise<ChannelPool> protocolPromise = mock(Promise.class);

        when(eventLoop.newPromise()).thenReturn(protocolPromise);
        when(protocolPromise.addListener(any())).thenAnswer(invocation -> {
            ((io.netty.util.concurrent.GenericFutureListener) invocation.getArgument(0))
                .operationComplete(protocolPromise);
            return null;
        });
        when(protocolPromise.isSuccess()).thenReturn(false);
        when(protocolPromise.cause()).thenReturn(new RuntimeException("Failed to get pool"));

        CompletableFuture<Void> result = channelPool.collectChannelPoolMetrics(mock(MetricCollector.class));

        verify(protocolPromise).addListener(any());
    }
}
