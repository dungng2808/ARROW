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
        builder.withSymbol("AAPL");
        builder.withNoSnapshot(true);
        
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertEquals("AAPL", request.getQueryParams().get("symbols"));
        assertEquals("true", request.getQueryParams().get("noSnapshot"));
    }
    
    @Test
    public void testBuildWithoutSnapshot() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol("GOOGL");
        builder.withNoSnapshot(false);
        
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertEquals("GOOGL", request.getQueryParams().get("symbols"));
        assertEquals("false", request.getQueryParams().get("noSnapshot"));
    }
    
    @Test
    public void testBuildWithNullSymbol() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol(null);
        builder.withNoSnapshot(true);
        
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        assertEquals("/tops", request.getPath());
        assertEquals(null, request.getQueryParams().get("symbols"));
        assertEquals("true", request.getQueryParams().get("noSnapshot"));
    }
    
    @Test
    public void testGenericType() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol("MSFT");
        builder.withNoSnapshot(false);
        
        SseRequest<List<TOPS>> request = builder.build();
        
        assertNotNull(request);
        GenericType<List<TOPS>> genericType = request.getResponse();
        assertNotNull(genericType);
    }
}
