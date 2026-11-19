package software.amazon.awssdk.http.nio.netty.internal.http2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.nio.netty.internal.NettyConfiguration;
import software.amazon.awssdk.metrics.MetricCollector;

public class HttpOrHttp2ChannelPool_ArrowTest {
    @Test
    public void acquireReturnsPromiseAndStartsDelegateAcquire() {
        EventLoopGroup group = mock(EventLoopGroup.class);
        EventLoop eventLoop = mock(EventLoop.class);
        ChannelPool delegate = mock(ChannelPool.class);
        NettyConfiguration configuration = mock(NettyConfiguration.class);
        Promise<Channel> promise = mock(Promise.class);
        Future<Channel> acquireFuture = mock(Future.class);

        when(group.next()).thenReturn(eventLoop);
        when(eventLoop.newPromise()).thenReturn(promise);
        when(delegate.acquire()).thenReturn(acquireFuture);

        HttpOrHttp2ChannelPool pool = new HttpOrHttp2ChannelPool(delegate, group, 1, configuration);
        Future<Channel> result = pool.acquire();

        assertEquals(promise, result);
        verify(delegate).acquire();
    }

    @Test
    public void acquireWithPromiseReturnsProvidedPromise() {
        EventLoopGroup group = mock(EventLoopGroup.class);
        EventLoop eventLoop = mock(EventLoop.class);
        ChannelPool delegate = mock(ChannelPool.class);
        NettyConfiguration configuration = mock(NettyConfiguration.class);
        Promise<Channel> initialPromise = mock(Promise.class);
        Promise<Channel> suppliedPromise = mock(Promise.class);
        Future<Channel> acquireFuture = mock(Future.class);

        when(group.next()).thenReturn(eventLoop);
        when(eventLoop.newPromise()).thenReturn(initialPromise);
        when(delegate.acquire()).thenReturn(acquireFuture);

        HttpOrHttp2ChannelPool pool = new HttpOrHttp2ChannelPool(delegate, group, 1, configuration);
        Future<Channel> result = pool.acquire(suppliedPromise);

        assertEquals(suppliedPromise, result);
        verify(delegate).acquire();
    }

    @Test
    public void releaseBeforeProtocolInitializationDelegatesRelease() {
        EventLoopGroup group = mock(EventLoopGroup.class);
        EventLoop eventLoop = mock(EventLoop.class);
        ChannelPool delegate = mock(ChannelPool.class);
        NettyConfiguration configuration = mock(NettyConfiguration.class);
        Promise<ChannelPool> protocolPromise = mock(Promise.class);
        Promise<Void> releasePromise = mock(Promise.class);
        Channel channel = mock(Channel.class);
        Future<Void> releaseFuture = mock(Future.class);

        when(group.next()).thenReturn(eventLoop);
        when(eventLoop.newPromise()).thenReturn(protocolPromise, releasePromise);
        when(delegate.release(channel, releasePromise)).thenReturn(releaseFuture);

        HttpOrHttp2ChannelPool pool = new HttpOrHttp2ChannelPool(delegate, group, 1, configuration);
        Future<Void> result = pool.release(channel);

        assertEquals(releasePromise, result);
        verify(delegate).release(channel, releasePromise);
    }

    @Test
    public void closeBeforeInitializationClosesDelegate() {
        EventLoopGroup group = mock(EventLoopGroup.class);
        EventLoop eventLoop = mock(EventLoop.class);
        ChannelPool delegate = mock(ChannelPool.class);
        NettyConfiguration configuration = mock(NettyConfiguration.class);
        Promise<ChannelPool> protocolPromise = mock(Promise.class);

        when(group.next()).thenReturn(eventLoop);
        when(eventLoop.newPromise()).thenReturn(protocolPromise);

        HttpOrHttp2ChannelPool pool = new HttpOrHttp2ChannelPool(delegate, group, 1, configuration);
        pool.close();

        verify(delegate).close();
    }

    @Test
    public void closeCanBeCalledMoreThanOnce() {
        EventLoopGroup group = mock(EventLoopGroup.class);
        EventLoop eventLoop = mock(EventLoop.class);
        ChannelPool delegate = mock(ChannelPool.class);
        NettyConfiguration configuration = mock(NettyConfiguration.class);
        Promise<ChannelPool> protocolPromise = mock(Promise.class);

        when(group.next()).thenReturn(eventLoop);
        when(eventLoop.newPromise()).thenReturn(protocolPromise);

        HttpOrHttp2ChannelPool pool = new HttpOrHttp2ChannelPool(delegate, group, 1, configuration);
        pool.close();
        pool.close();

        verify(delegate).close();
    }

    @Test
    public void collectMetricsReturnsIncompleteFutureUntilProtocolIsKnown() {
        EventLoopGroup group = mock(EventLoopGroup.class);
        EventLoop eventLoop = mock(EventLoop.class);
        ChannelPool delegate = mock(ChannelPool.class);
        NettyConfiguration configuration = mock(NettyConfiguration.class);
        Promise<ChannelPool> protocolPromise = mock(Promise.class);
        MetricCollector metrics = mock(MetricCollector.class);

        when(group.next()).thenReturn(eventLoop);
        when(eventLoop.newPromise()).thenReturn(protocolPromise);

        HttpOrHttp2ChannelPool pool = new HttpOrHttp2ChannelPool(delegate, group, 1, configuration);
        CompletableFuture<Void> result = pool.collectChannelPoolMetrics(metrics);

        assertNotNull(result);
        assertFalse(result.isDone());
    }
}
