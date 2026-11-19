package software.amazon.awssdk.http.nio.netty.internal.http2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;
import org.junit.Test;
import software.amazon.awssdk.http.nio.netty.internal.NettyConfiguration;
import software.amazon.awssdk.metrics.MetricCollector;

public class HttpOrHttp2ChannelPool_ArrowTest {

    @Test
    public void acquireReturnsFuture() {
        HttpOrHttp2ChannelPool pool = new HttpOrHttp2ChannelPool(null, null, 1, null);
        try {
            Future<Channel> result = pool.acquire();
            assertNotNull(result);
        } finally {
            pool.close();
        }
    }

    @Test
    public void releaseReturnsFuture() {
        HttpOrHttp2ChannelPool pool = new HttpOrHttp2ChannelPool(null, null, 1, null);
        try {
            Future<Void> result = pool.release(null);
            assertNotNull(result);
        } finally {
            pool.close();
        }
    }

    @Test
    public void collectMetricsReturnsCompletableFuture() {
        HttpOrHttp2ChannelPool pool = new HttpOrHttp2ChannelPool(null, null, 1, null);
        CompletableFuture<Void> result = pool.collectChannelPoolMetrics((MetricCollector) null);
        assertNotNull(result);
        assertTrue(!result.isDone());
    }
}
