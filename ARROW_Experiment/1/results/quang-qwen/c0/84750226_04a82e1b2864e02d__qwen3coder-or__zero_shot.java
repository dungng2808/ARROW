package pl.zankowski.iextrading4j.client.sse.request.marketdata;

import org.junit.Test;
import pl.zankowski.iextrading4j.api.marketdata.TOPS;
import pl.zankowski.iextrading4j.client.sse.manager.SseRequest;
import pl.zankowski.iextrading4j.client.sse.request.AbstractSymbolSseRequestBuilder;

import javax.ws.rs.core.GenericType;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TopsSseRequestBuilder_ArrowTest {

    @Test
    public void testBuild() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertNotNull(request.getResponse());
        assertEquals(new GenericType<List<TOPS>>() {}.getType(), request.getResponse().getGenericType());
    }

    @Test
    public void testBuildWithSymbol() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol("AAPL");
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertNotNull(request.getResponse());
        assertEquals(new GenericType<List<TOPS>>() {}.getType(), request.getResponse().getGenericType());
    }

    @Test
    public void testBuildWithNoSnapshot() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withNoSnapshot(true);
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertNotNull(request.getResponse());
        assertEquals(new GenericType<List<TOPS>>() {}.getType(), request.getResponse().getGenericType());
    }

    @Test
    public void testBuildWithSymbolAndNoSnapshot() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol("GOOGL").withNoSnapshot(true);
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertNotNull(request.getResponse());
        assertEquals(new GenericType<List<TOPS>>() {}.getType(), request.getResponse().getGenericType());
    }
}
