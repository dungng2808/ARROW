package pl.zankowski.iextrading4j.client.sse.request.marketdata;

import org.junit.Test;
import pl.zankowski.iextrading4j.client.sse.manager.SseRequest;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class TopsSseRequestBuilder_ArrowTest {

    @Test
    public void buildReturnsRequest() {
        TopsSseRequestBuilder builder = new TopsSseRequestBuilder();

        SseRequest<List<pl.zankowski.iextrading4j.api.marketdata.TOPS>> request = builder.build();

        assertNotNull(request);
    }
}
