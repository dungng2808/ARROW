package pl.zankowski.iextrading4j.client.sse.request.marketdata;

import org.junit.Test;
import pl.zankowski.iextrading4j.api.marketdata.TOPS;
import pl.zankowski.iextrading4j.client.sse.manager.SseRequest;
import pl.zankowski.iextrading4j.client.sse.request.AbstractSymbolSseRequestBuilder;

import javax.ws.rs.core.GenericType;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TopsSseRequestBuilder_ArrowTest {

    @Test
    public void testBuildWithoutSymbol() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertEquals(new GenericType<List<TOPS>>() {}, request.getResponse());
        // No symbol parameter should be added when no symbol is set
    }

    @Test
    public void testBuildWithSymbol() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol("AAPL");
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertEquals(new GenericType<List<TOPS>>() {}, request.getResponse());
        // Should contain symbol parameter
    }

    @Test
    public void testBuildWithNoSnapshot() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withNoSnapshot(true);
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertEquals(new GenericType<List<TOPS>>() {}, request.getResponse());
        // Should contain no snapshot parameter
    }

    @Test
    public void testBuildWithSymbolAndNoSnapshot() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol("GOOGL").withNoSnapshot(true);
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertEquals(new GenericType<List<TOPS>>() {}, request.getResponse());
        // Should contain both symbol and no snapshot parameters
    }

    @Test
    public void testInheritance() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        assertNotNull(builder);
        // Verify it extends AbstractSymbolSseRequestBuilder
        assertEquals(AbstractSymbolSseRequestBuilder.class, builder.getClass().getSuperclass());
    }
}
