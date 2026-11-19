package pl.zankowski.iextrading4j.client.sse.request.marketdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.ws.rs.core.GenericType;

import org.junit.Test;

import pl.zankowski.iextrading4j.api.marketdata.TOPS;
import pl.zankowski.iextrading4j.client.sse.manager.SseRequest;

public class TopsSseRequestBuilder_ArrowTest {

    @Test
    public void testBuild() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertNotNull(request.getResponse());
        assertEquals(new GenericType<List<TOPS>>() {}, request.getResponse());
    }

    @Test
    public void testBuildWithSymbol() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol("AAPL");
        
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertNotNull(request.getResponse());
        assertEquals(new GenericType<List<TOPS>>() {}, request.getResponse());
    }

    @Test
    public void testBuildWithNoSnapshot() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withNoSnapshot(true);
        
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertNotNull(request.getResponse());
        assertEquals(new GenericType<List<TOPS>>() {}, request.getResponse());
    }

    @Test
    public void testBuildWithSymbolAndNoSnapshot() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol("GOOGL").withNoSnapshot(true);
        
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertNotNull(request.getResponse());
        assertEquals(new GenericType<List<TOPS>>() {}, request.getResponse());
    }
}
